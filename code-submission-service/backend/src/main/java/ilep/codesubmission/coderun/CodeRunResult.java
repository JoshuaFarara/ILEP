package ilep.codesubmission.coderun;

/**
 * The outcome of compiling and running one student program.
 *
 * @param compileSucceeded     false when javac reported errors (they are in standardError)
 * @param exitCode             the program's exit code; null if it did not run or was stopped
 * @param timedOut             true if the program exceeded its time limit and was stopped
 * @param outputTruncated      true if output went past the size limit and was cut off
 */
public record CodeRunResult(
		boolean compileSucceeded,
		String standardOutput,
		String standardError,
		Integer exitCode,
		boolean timedOut,
		boolean outputTruncated,
		long durationMilliseconds) {
}
