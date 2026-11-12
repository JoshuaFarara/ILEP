package ilep.codesubmission.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import ilep.codesubmission.configuration.CodeSubmissionProperties;

/**
 * Development convenience: if {@code INSTRUCTOR_API_KEY} is set, registers an instructor with that
 * key at startup. Only the key's hash is stored, and the key itself is never logged.
 */
@Component
public class DevelopmentInstructorRegistration implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(DevelopmentInstructorRegistration.class);

	private final JdbcClient jdbcClient;
	private final CodeSubmissionProperties properties;

	public DevelopmentInstructorRegistration(JdbcClient jdbcClient, CodeSubmissionProperties properties) {
		this.jdbcClient = jdbcClient;
		this.properties = properties;
	}

	@Override
	public void run(ApplicationArguments arguments) {
		String developmentApiKey = properties.instructor().developmentApiKey();
		if (developmentApiKey == null || developmentApiKey.isBlank()) {
			return;
		}
		if (developmentApiKey.startsWith(BearerTokenAuthenticationFilter.studentTokenPrefix)) {
			throw new IllegalStateException("INSTRUCTOR_API_KEY must not start with the student token prefix.");
		}
		int insertedRows = jdbcClient.sql("""
				insert into instructors (display_label, api_key_hash) values ('Development instructor', :apiKeyHash)
				on conflict (api_key_hash) do nothing
				""")
				.param("apiKeyHash", SecretHashing.sha256(developmentApiKey))
				.update();
		if (insertedRows > 0) {
			logger.info("Registered the development instructor from INSTRUCTOR_API_KEY.");
		}
	}
}
