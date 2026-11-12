package controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.util.Duration;
import models.Attendance;
import models.CodeSubmissionClient;
import models.CodeSubmissionClient.CodeSubmissionException;
import models.CodeSubmissionClient.NewAssignment;
import models.CodeSubmissionClient.StudentSubmissionStatus;
import models.CodeSubmissionCourseStore;
import models.CodeSubmissionCourseStore.CodeSubmissionAssignment;
import models.CodeSubmissionCourseStore.Course;
import models.CodeSubmissionSettings;
import models.Student;
import views.CodeSubmissionView;
import views.CodeSubmissionView.AssignmentForm;
import views.CodeSubmissionView.StudentStatusRow;

/**
 * Runs the Code Submission activity inside Class Session: set up the service, sync the selected
 * rosters, create an assignment, show the join code and each student's live status, and close it.
 *
 * Every call to the service runs on a background thread (javafx.concurrent.Task), so the window
 * never freezes; results come back on the JavaFX thread. Student names are matched to pseudonyms
 * here, on this computer: the service only ever sees the pseudonyms.
 */
public class CodeSubmissionController {

	private static final Duration statusRefreshInterval = Duration.seconds(10);
	private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("h:mm:ss a");

	private final CodeSubmissionView view = new CodeSubmissionView();
	private final Path settingsFile;
	private final Path coursesFile;
	private final ExecutorService backgroundWork = Executors.newSingleThreadExecutor(runnable -> {
		Thread thread = new Thread(runnable, "code-submission-requests");
		thread.setDaemon(true);
		return thread;
	});
	private final Timeline statusPolling = new Timeline(new KeyFrame(statusRefreshInterval, event -> refreshStatus()));

	private CodeSubmissionSettings settings;
	private CodeSubmissionCourseStore courseStore;
	private Attendance attendance;
	private Course course;
	private boolean statusRequestInProgress;

	public CodeSubmissionController() {
		this(CodeSubmissionSettings.DEFAULT_SETTINGS_FILE, CodeSubmissionCourseStore.DEFAULT_COURSES_FILE);
	}

	/** The two local files can be redirected, e.g. to a temporary folder for testing. */
	public CodeSubmissionController(Path settingsFile, Path coursesFile) {
		this.settingsFile = settingsFile;
		this.coursesFile = coursesFile;
		statusPolling.setCycleCount(Timeline.INDEFINITE);

		view.setOnSaveSettings(this::handleSaveSettings);
		view.setOnCreateAssignment(this::handleCreateAssignment);
		view.setOnRefresh(this::refreshStatus);
		view.setOnCloseAssignment(this::handleCloseAssignment);
		view.setOnNewAssignment(this::handleNewAssignment);
		view.setOnEditSettings(this::showSettings);
	}

	public Parent getView() {
		return view;
	}

	/** Called when the instructor picks Activities → Code Submission for the current class session. */
	public void open(Attendance attendanceSession) {
		stop();
		view.clearError();
		this.attendance = attendanceSession;
		if (attendance == null || attendance.getRosters().isEmpty()) {
			view.showMessage("Take attendance first: Code Submission uses the rosters selected for this class session.");
			return;
		}
		try {
			settings = CodeSubmissionSettings.load(settingsFile);
			courseStore = CodeSubmissionCourseStore.load(coursesFile);
		} catch (IOException | RuntimeException exception) {
			view.showMessage("Code Submission could not read its files in src/appData.");
			view.showError("Could not read local Code Submission files: " + exception.getMessage());
			return;
		}
		if (!settings.isComplete()) {
			showSettings();
			return;
		}
		course = courseStore.courseFor(attendance.getRosters());
		showCurrentState();
	}

	/** Stops live updates; called whenever the panel is hidden. */
	public void stop() {
		statusPolling.stop();
	}

	// ------------------------------------------------------------ states
	private void showCurrentState() {
		Optional<CodeSubmissionAssignment> currentAssignment = course.getCurrentAssignment();
		if (currentAssignment.isEmpty()) {
			view.showAssignmentForm(course.getLabel(), attendance.getStudents().size());
			return;
		}
		CodeSubmissionAssignment assignment = currentAssignment.get();
		view.showAssignment(course.getLabel(), assignment.title(), assignment.joinCode(), assignment.joinUrl(), false);
		showStatus(List.of());
		refreshStatus();
		statusPolling.play();
	}

	private void showSettings() {
		stop();
		if (settings == null) {
			settings = new CodeSubmissionSettings(settingsFile);
		}
		view.showSettingsForm(settings.getServiceUrl(), !settings.getInstructorApiKey().isBlank());
	}

	// ------------------------------------------------------------ view events
	private void handleSaveSettings(String serviceUrl, String instructorApiKey) {
		settings.setServiceUrl(serviceUrl);
		if (!instructorApiKey.isBlank()) {
			settings.setInstructorApiKey(instructorApiKey);
		}
		if (!settings.getServiceUrl().matches("^https?://.+")) {
			view.showError("The service address must start with https:// (or http:// for a local test server).");
			return;
		}
		if (!settings.isComplete()) {
			view.showError("Enter both the service address and the API key.");
			return;
		}
		try {
			settings.save();
		} catch (IOException exception) {
			view.showError("Could not save settings: " + exception.getMessage());
			return;
		}
		view.clearError();
		open(attendance);
	}

	private void handleCreateAssignment(AssignmentForm form) {
		if (form.title() == null || form.title().isBlank()) {
			view.showError("Give the assignment a title.");
			return;
		}
		OffsetDateTime dueAt;
		try {
			dueAt = dueDateTime(form.dueDate(), form.dueTime());
		} catch (DateTimeParseException exception) {
			view.showError("Write the due time as hours:minutes, e.g. 23:59.");
			return;
		}
		if (dueAt != null && dueAt.isBefore(OffsetDateTime.now())) {
			view.showError("The due date is in the past.");
			return;
		}
		view.clearError();

		NewAssignment newAssignment = new NewAssignment(form.title().trim(), form.instructions(), form.starterCode(),
				form.artificialIntelligenceUse(), form.artificialIntelligenceNote(), dueAt);
		CodeSubmissionClient client = new CodeSubmissionClient(settings);
		List<Student> rosterStudents = new ArrayList<>(attendance.getStudents());

		runInBackground("Syncing roster and creating the assignment…", () -> {
			CodeSubmissionClient.RosterSynchronizationResult syncResult = client.synchronizeRoster(course, rosterStudents);
			return new CreatedAssignmentResult(syncResult, client.createAssignment(course, newAssignment));
		}, result -> {
			course.setCurrentAssignment(result.assignment());
			saveCourseStore();
			showCurrentState();
			if (result.syncResult().skippedStudentCount() > 0) {
				view.showError(result.syncResult().skippedStudentCount()
						+ " student(s) have no Eagle ID on the roster and cannot join.");
			}
		}, this::saveCourseStore);
	}

	private record CreatedAssignmentResult(CodeSubmissionClient.RosterSynchronizationResult syncResult,
			CodeSubmissionAssignment assignment) {
	}

	private void handleCloseAssignment() {
		Optional<CodeSubmissionAssignment> currentAssignment = course.getCurrentAssignment();
		if (currentAssignment.isEmpty()) {
			return;
		}
		CodeSubmissionAssignment assignment = currentAssignment.get();
		CodeSubmissionClient client = new CodeSubmissionClient(settings);
		stop();
		runInBackground("Closing the assignment…", () -> {
			client.closeAssignment(assignment.assignmentId());
			return client.summarizeAssignment(assignment.assignmentId());
		}, finalStatuses -> {
			course.clearCurrentAssignment();
			saveCourseStore();
			view.showAssignment(course.getLabel(), assignment.title(), assignment.joinCode(), assignment.joinUrl(), true);
			showStatus(finalStatuses);
		}, null);
	}

	private void handleNewAssignment() {
		view.clearError();
		showCurrentState();
	}

	/** Asks the service for the latest status; quietly skipped if a request is still in progress. */
	private void refreshStatus() {
		Optional<CodeSubmissionAssignment> currentAssignment = course == null ? Optional.empty() : course.getCurrentAssignment();
		if (currentAssignment.isEmpty() || statusRequestInProgress) {
			return;
		}
		String assignmentId = currentAssignment.get().assignmentId();
		CodeSubmissionClient client = new CodeSubmissionClient(settings);
		statusRequestInProgress = true;
		Task<List<StudentSubmissionStatus>> task = new Task<>() {
			@Override
			protected List<StudentSubmissionStatus> call() throws Exception {
				return client.summarizeAssignment(assignmentId);
			}
		};
		task.setOnSucceeded(event -> {
			statusRequestInProgress = false;
			view.clearError();
			showStatus(task.getValue());
		});
		task.setOnFailed(event -> {
			statusRequestInProgress = false;
			view.showError(messageFor(task.getException()) + " Retrying every 10 seconds.");
		});
		backgroundWork.submit(task);
	}

	// ------------------------------------------------------------ helpers
	/** Turns pseudonymous statuses into named rows, using this computer's mapping only. */
	private void showStatus(List<StudentSubmissionStatus> statuses) {
		List<Student> rosterStudents = attendance.getStudents();
		record NamedStatus(Optional<Student> student, StudentSubmissionStatus status) {
		}
		List<NamedStatus> namedStatuses = new ArrayList<>();
		for (StudentSubmissionStatus status : statuses) {
			namedStatuses.add(new NamedStatus(course.studentFor(status.studentReference(), rosterStudents), status));
		}
		// Last name, then first name, like the rosters; unknown students go last
		namedStatuses.sort(Comparator.comparing((NamedStatus named) -> named.student().isEmpty())
				.thenComparing(named -> named.student().map(Student::getLastName).orElse(""), String.CASE_INSENSITIVE_ORDER)
				.thenComparing(named -> named.student().map(Student::getFirstName).orElse(""), String.CASE_INSENSITIVE_ORDER));

		List<StudentStatusRow> rows = new ArrayList<>();
		int notStartedCount = 0;
		int workingCount = 0;
		int submittedCount = 0;
		for (NamedStatus named : namedStatuses) {
			StudentSubmissionStatus status = named.status();
			switch (status.status()) {
				case "submitted" -> submittedCount++;
				case "inProgress" -> workingCount++;
				default -> notStartedCount++;
			}
			rows.add(new StudentStatusRow(
					named.student().map(CodeSubmissionController::displayName).orElse("(not on today's rosters)"),
					named.student().map(attendance::isPresent).orElse(false),
					statusLabel(status), lastSavedText(status), status.runCount()));
		}
		view.showStatusRows(rows, notStartedCount, workingCount, submittedCount, LocalTime.now().format(timeFormat));
	}

	/**
	 * Runs {@code work} on the background thread with the busy indicator showing, then calls
	 * {@code onSuccess} on the JavaFX thread. On failure, shows the error and calls {@code onFailure}
	 * if given.
	 */
	private <Result> void runInBackground(String busyMessage, Callable<Result> work, Consumer<Result> onSuccess,
			Runnable onFailure) {
		view.setBusy(true, busyMessage);
		Task<Result> task = new Task<>() {
			@Override
			protected Result call() throws Exception {
				return work.call();
			}
		};
		task.setOnSucceeded(event -> {
			view.setBusy(false, null);
			onSuccess.accept(task.getValue());
		});
		task.setOnFailed(event -> {
			view.setBusy(false, null);
			view.showError(messageFor(task.getException()));
			if (onFailure != null) {
				onFailure.run();
			}
		});
		backgroundWork.submit(task);
	}

	private void saveCourseStore() {
		try {
			courseStore.save();
		} catch (IOException exception) {
			view.showError("Could not save the local Code Submission record: " + exception.getMessage());
		}
	}

	private static String messageFor(Throwable exception) {
		if (exception instanceof CodeSubmissionException codeSubmissionException) {
			return codeSubmissionException.getMessage();
		}
		return "Something went wrong talking to the Code Submission service.";
	}

	private static OffsetDateTime dueDateTime(LocalDate dueDate, String dueTime) {
		if (dueDate == null) {
			return null;
		}
		LocalTime time = dueTime == null || dueTime.isBlank() ? LocalTime.of(23, 59) : LocalTime.parse(dueTime.trim());
		return dueDate.atTime(time).atZone(ZoneId.systemDefault()).toOffsetDateTime();
	}

	private static String statusLabel(StudentSubmissionStatus status) {
		return switch (status.status()) {
			case "submitted" -> "Submitted";
			case "inProgress" -> "Working";
			default -> "Not started";
		};
	}

	private static String lastSavedText(StudentSubmissionStatus status) {
		OffsetDateTime lastSaved = status.lastSavedAt() != null ? status.lastSavedAt() : status.startedAt();
		if (lastSaved == null) {
			return "";
		}
		return lastSaved.atZoneSameInstant(ZoneId.systemDefault()).toLocalTime().format(timeFormat);
	}

	/** "First Last", the same as the Class Session student list. */
	private static String displayName(Student student) {
		return student.getFirstName() + " " + student.getLastName();
	}
}
