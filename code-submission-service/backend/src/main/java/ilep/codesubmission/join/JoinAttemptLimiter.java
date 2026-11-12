package ilep.codesubmission.join;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ilep.codesubmission.configuration.CodeSubmissionProperties;

/**
 * Limits failed join attempts per join code and per client address within a sliding window, so
 * nobody can guess Eagle IDs against a join code. Only failures count, so a full class joining at
 * once is never blocked. In memory: limits reset when the service restarts.
 */
@Component
public class JoinAttemptLimiter {

	private static final int maximumTrackedKeysBeforeCleanup = 10_000;

	private final Map<String, Deque<Instant>> failuresByJoinCode = new ConcurrentHashMap<>();
	private final Map<String, Deque<Instant>> failuresByClientAddress = new ConcurrentHashMap<>();
	private final CodeSubmissionProperties.Join joinProperties;
	private final Clock clock;

	@Autowired
	public JoinAttemptLimiter(CodeSubmissionProperties properties) {
		this(properties, Clock.systemUTC());
	}

	JoinAttemptLimiter(CodeSubmissionProperties properties, Clock clock) {
		this.joinProperties = properties.join();
		this.clock = clock;
	}

	public boolean isBlocked(String joinCode, String clientAddress) {
		return recentFailureCount(failuresByJoinCode, joinCode) >= joinProperties.maximumFailuresPerJoinCode()
				|| recentFailureCount(failuresByClientAddress, clientAddress) >= joinProperties.maximumFailuresPerClientAddress();
	}

	public void recordFailure(String joinCode, String clientAddress) {
		Instant now = clock.instant();
		addFailure(failuresByJoinCode, joinCode, now);
		addFailure(failuresByClientAddress, clientAddress, now);
		removeIdleKeysIfLarge(failuresByJoinCode);
		removeIdleKeysIfLarge(failuresByClientAddress);
	}

	/** Keeps memory bounded when many different (often made-up) join codes are tried. */
	private void removeIdleKeysIfLarge(Map<String, Deque<Instant>> failuresByKey) {
		if (failuresByKey.size() < maximumTrackedKeysBeforeCleanup) {
			return;
		}
		failuresByKey.entrySet().removeIf(entry -> {
			synchronized (entry.getValue()) {
				removeExpired(entry.getValue());
				return entry.getValue().isEmpty();
			}
		});
	}

	private int recentFailureCount(Map<String, Deque<Instant>> failuresByKey, String key) {
		Deque<Instant> failures = failuresByKey.get(key);
		if (failures == null) {
			return 0;
		}
		synchronized (failures) {
			removeExpired(failures);
			return failures.size();
		}
	}

	private void addFailure(Map<String, Deque<Instant>> failuresByKey, String key, Instant now) {
		Deque<Instant> failures = failuresByKey.computeIfAbsent(key, ignored -> new ArrayDeque<>());
		synchronized (failures) {
			removeExpired(failures);
			failures.addLast(now);
		}
	}

	private void removeExpired(Deque<Instant> failures) {
		Instant windowStart = clock.instant().minus(joinProperties.failureWindow());
		while (!failures.isEmpty() && failures.peekFirst().isBefore(windowStart)) {
			failures.removeFirst();
		}
	}
}
