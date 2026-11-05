package models;

/**
 * The larger activities a class session can run with its present students.
 * Add a value here and it appears in the Class Session "Activities" menu.
 */
public enum ClassActivity {
	SLIDE_ACTIVITIES("Slide Activities"),
	QUIZ("Quiz"),
	CODE_SUBMISSION("Code Submission"),
	PRESENTATION("Presentation");

	private final String label;

	ClassActivity(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return label;
	}
}
