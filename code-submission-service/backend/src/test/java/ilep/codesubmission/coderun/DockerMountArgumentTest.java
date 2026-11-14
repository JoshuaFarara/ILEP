package ilep.codesubmission.coderun;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** The sandbox's bind-mount argument must work for Windows paths too (no Docker needed). */
class DockerMountArgumentTest {

	@Test
	void linuxPath() {
		assertThat(DockerJavaCodeRunner.bindMountReadOnly("/tmp/code-run-123", "/submission"))
				.isEqualTo("type=bind,source=/tmp/code-run-123,target=/submission,readonly");
	}

	@Test
	void windowsPathWithDriveLetterAndSpaces() {
		assertThat(DockerJavaCodeRunner.bindMountReadOnly(
				"C:\\Users\\Student One\\AppData\\Local\\Temp\\code-run-123", "/submission"))
				.isEqualTo("type=bind,source=C:\\Users\\Student One\\AppData\\Local\\Temp\\code-run-123,"
						+ "target=/submission,readonly");
	}

	@Test
	void pathWithCommaIsQuoted() {
		assertThat(DockerJavaCodeRunner.bindMountReadOnly("C:\\Temp\\a,b", "/submission"))
				.isEqualTo("type=bind,\"source=C:\\Temp\\a,b\",target=/submission,readonly");
	}
}
