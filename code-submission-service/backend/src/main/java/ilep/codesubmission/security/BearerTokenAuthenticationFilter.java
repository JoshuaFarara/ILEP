package ilep.codesubmission.security;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Reads {@code Authorization: Bearer <token>} and authenticates either an instructor (API key) or a
 * student (session token). Only SHA-256 hashes of either secret are stored in the database.
 */
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

	public static final String studentTokenPrefix = "student_";
	public static final String instructorRole = "ROLE_INSTRUCTOR";
	public static final String studentRole = "ROLE_STUDENT";

	private final JdbcClient jdbcClient;

	public BearerTokenAuthenticationFilter(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authorizationHeader = request.getHeader("Authorization");
		if (authorizationHeader != null && authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
			String bearerToken = authorizationHeader.substring(7).trim();
			if (!bearerToken.isEmpty()) {
				authenticate(bearerToken);
			}
		}
		filterChain.doFilter(request, response);
	}

	private void authenticate(String bearerToken) {
		byte[] tokenHash = SecretHashing.sha256(bearerToken);
		if (bearerToken.startsWith(studentTokenPrefix)) {
			findStudentWorkSession(tokenHash).ifPresent(studentWorkSessionId -> setAuthentication(
					new StudentPrincipal(studentWorkSessionId), studentRole));
		} else {
			findInstructor(tokenHash).ifPresent(instructorId -> setAuthentication(
					new InstructorPrincipal(instructorId), instructorRole));
		}
	}

	private Optional<UUID> findStudentWorkSession(byte[] tokenHash) {
		return jdbcClient.sql("""
				select student_work_session_id from student_session_tokens
				where token_hash = :tokenHash and expires_at > :now
				""")
				.param("tokenHash", tokenHash)
				.param("now", OffsetDateTime.now())
				.query(UUID.class)
				.optional();
	}

	private Optional<Long> findInstructor(byte[] apiKeyHash) {
		return jdbcClient.sql("select instructor_id from instructors where api_key_hash = :apiKeyHash")
				.param("apiKeyHash", apiKeyHash)
				.query(Long.class)
				.optional();
	}

	private static void setAuthentication(Object principal, String role) {
		var authentication = new UsernamePasswordAuthenticationToken(principal, null,
				List.of(new SimpleGrantedAuthority(role)));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
