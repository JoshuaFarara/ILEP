package ilep.codesubmission.security;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Who may call what:
 * <ul>
 * <li>{@code /api/v1/courses/**}, {@code /api/v1/assignments/**}: instructor API key.</li>
 * <li>{@code /api/v1/student/**}: student session token from {@code /api/v1/join}.</li>
 * <li>{@code /api/v1/join}: open, rate-limited.</li>
 * <li>Everything else under {@code /api}: denied. Static editor files: open.</li>
 * </ul>
 * Stateless: no cookies, so cross-site request forgery protection is not needed for the API.
 */
@Configuration
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JdbcClient jdbcClient) throws Exception {
		httpSecurity
				.csrf(csrf -> csrf.disable())
				.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.httpBasic(httpBasic -> httpBasic.disable())
				.formLogin(formLogin -> formLogin.disable())
				.logout(logout -> logout.disable())
				.headers(headers -> headers
						.contentSecurityPolicy(contentSecurityPolicy -> contentSecurityPolicy.policyDirectives(
								"default-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; "
										+ "connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'"))
						.referrerPolicy(referrerPolicy -> referrerPolicy.policy(ReferrerPolicy.NO_REFERRER)))
				.addFilterBefore(new BearerTokenAuthenticationFilter(jdbcClient), AnonymousAuthenticationFilter.class)
				.authorizeHttpRequests(authorization -> authorization
						.requestMatchers("/api/v1/join").permitAll()
						.requestMatchers("/api/v1/courses/**", "/api/v1/assignments/**").hasRole("INSTRUCTOR")
						.requestMatchers("/api/v1/student/**").hasRole("STUDENT")
						.requestMatchers("/api/**").denyAll()
						.anyRequest().permitAll())
				.exceptionHandling(exceptionHandling -> exceptionHandling
						.authenticationEntryPoint((request, response, exception) -> writeError(response,
								HttpStatus.UNAUTHORIZED, "unauthorized", "Missing or invalid credentials."))
						.accessDeniedHandler((request, response, exception) -> writeError(response,
								HttpStatus.FORBIDDEN, "forbidden", "You do not have access to this resource.")));
		return httpSecurity.build();
	}

	private static void writeError(HttpServletResponse response, HttpStatus status, String errorCode, String message)
			throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write("{\"error\":\"" + errorCode + "\",\"message\":\"" + message + "\"}");
	}
}
