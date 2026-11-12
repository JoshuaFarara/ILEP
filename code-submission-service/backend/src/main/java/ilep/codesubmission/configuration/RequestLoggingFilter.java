package ilep.codesubmission.configuration;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Logs one line per API request: method, route template, status and duration.
 *
 * It logs the route template (for example {@code /api/v1/courses/{courseReference}/roster}), never
 * the actual path, query string, headers or body. That keeps Class I data and secrets out of logs
 * (DSCS: application logs are Class II and must contain no Class I values).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().startsWith("/api/");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		long startNanoseconds = System.nanoTime();
		try {
			filterChain.doFilter(request, response);
		} finally {
			Object routeTemplate = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
			long durationMilliseconds = (System.nanoTime() - startNanoseconds) / 1_000_000;
			logger.info("{} {} -> {} in {} ms", request.getMethod(),
					routeTemplate != null ? routeTemplate : "(unmatched route)",
					response.getStatus(), durationMilliseconds);
		}
	}
}
