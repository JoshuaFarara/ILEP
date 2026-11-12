package ilep.codesubmission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Code Submission service: a browser-based Java editor for ILEP's Code Submission activity.
 *
 * Data handling follows docs/code-submission/data-classification.md. Student work is Class I
 * (Confidential). The service stores no student names, emails, Eagle IDs or CRNs.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class CodeSubmissionApplication {

	public static void main(String[] arguments) {
		SpringApplication.run(CodeSubmissionApplication.class, arguments);
	}
}
