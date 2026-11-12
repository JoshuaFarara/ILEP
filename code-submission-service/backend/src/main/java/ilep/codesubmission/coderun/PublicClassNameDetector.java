package ilep.codesubmission.coderun;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds the class to run, so the source file can be named to match it (Java requires a public
 * class {@code Foo} to live in {@code Foo.java}).
 *
 * Order: the public top-level class; otherwise the first class that declares {@code main};
 * otherwise {@code Main}. Comments and string literals are removed first so they can't mislead it.
 */
public final class PublicClassNameDetector {

	public static final String defaultClassName = "Main";

	private static final Pattern javaIdentifier = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");
	private static final Pattern publicClassDeclaration = Pattern.compile(
			"\\bpublic\\s+(?:(?:final|abstract|strictfp|sealed|non-sealed)\\s+)*(?:class|record|enum|interface)\\s+([A-Za-z_$][A-Za-z0-9_$]*)");
	private static final Pattern anyClassDeclaration = Pattern.compile(
			"\\b(?:class|record|enum|interface)\\s+([A-Za-z_$][A-Za-z0-9_$]*)");
	private static final Pattern mainMethodDeclaration = Pattern.compile(
			"\\bstatic\\s+(?:final\\s+)?void\\s+main\\s*\\(");

	private PublicClassNameDetector() {
	}

	public static String detectClassName(String sourceCode) {
		String codeWithoutCommentsOrStrings = removeCommentsAndStringLiterals(sourceCode);

		Matcher publicClassMatcher = publicClassDeclaration.matcher(codeWithoutCommentsOrStrings);
		if (publicClassMatcher.find()) {
			return validatedOrDefault(publicClassMatcher.group(1));
		}

		Matcher mainMatcher = mainMethodDeclaration.matcher(codeWithoutCommentsOrStrings);
		if (mainMatcher.find()) {
			String codeBeforeMain = codeWithoutCommentsOrStrings.substring(0, mainMatcher.start());
			Matcher classMatcher = anyClassDeclaration.matcher(codeBeforeMain);
			String nearestClassName = null;
			while (classMatcher.find()) {
				nearestClassName = classMatcher.group(1);
			}
			if (nearestClassName != null) {
				return validatedOrDefault(nearestClassName);
			}
		}
		return defaultClassName;
	}

	private static String validatedOrDefault(String className) {
		return javaIdentifier.matcher(className).matches() && className.length() <= 100 ? className : defaultClassName;
	}

	static String removeCommentsAndStringLiterals(String sourceCode) {
		return sourceCode
				.replaceAll("(?s)\"\"\".*?\"\"\"", "\"\"")
				.replaceAll("\"(?:\\\\.|[^\"\\\\])*\"", "\"\"")
				.replaceAll("'(?:\\\\.|[^'\\\\])'", "' '")
				.replaceAll("(?s)/\\*.*?\\*/", " ")
				.replaceAll("//[^\\n]*", " ");
	}
}
