package ilep.codesubmission.coderun;

/** Compiles and runs untrusted student Java code in isolation. */
public interface JavaCodeRunner {

	CodeRunResult compileAndRun(String sourceCode, String standardInput);
}
