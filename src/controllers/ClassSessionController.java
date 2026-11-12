package controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import controllers.AppState.Screen;
import javafx.scene.Parent;
import models.Activities;
import models.Attendance;
import models.ClassActivity;
import models.Student;
import views.ClassSessionView;

/**
 * Runs the Class Session screen: the present students from the current roll
 * call in {@link AppState}, plus the activities and student tools.
 */
public class ClassSessionController {

	private final AppState state;
	private final ClassSessionView view = new ClassSessionView();
	private final Activities activities = new Activities();
	private final CodeSubmissionController codeSubmissionController = new CodeSubmissionController();

	// Present students in the order shown (changes when shuffled)
	private List<Student> students = new ArrayList<>();
	private Student lastPicked;

	public ClassSessionController(AppState state) {
		this.state = state;

		view.setOnTakeAttendance(() -> state.navigateTo(Screen.ATTENDANCE));
		view.setOnActivitySelected(this::handleActivity);
		view.setOnShuffle(this::handleShuffle);
		view.setOnRandomStudent(this::handleRandomStudent);
		view.setOnRandomGroups(this::handleRandomGroups);

		// Attendance marks can change on the Attendance screen, so refresh each time this screen opens
		state.currentScreenProperty().addListener((obs, oldScreen, newScreen) -> {
			// Code Submission's live updates run only while its panel is on screen
			codeSubmissionController.stop();
			if (newScreen == Screen.CLASS_SESSION) refresh();
		});
		state.attendanceSessionProperty().addListener((obs, oldSession, newSession) -> {
			codeSubmissionController.stop();
			students = new ArrayList<>();
			refresh();
		});
		refresh();
	}

	public Parent getView() {
		return view;
	}

	private void refresh() {
		Attendance session = state.getAttendanceSession();
		if (session == null) {
			students = new ArrayList<>();
			view.showNoSession();
			return;
		}
		List<Student> present = session.getPresentStudents();
		// Keep a shuffled order as long as the same students are present
		if (!new HashSet<>(students).equals(new HashSet<>(present))) {
			students = new ArrayList<>(present);
			lastPicked = null;
		}
		view.showSession(session.getRosters(), students);
	}

	// ------------------------------------------------------------ view events
	private void handleActivity(ClassActivity activity) {
		codeSubmissionController.stop();
		if (activity == ClassActivity.CODE_SUBMISSION) {
			// Uses everyone on the selected rosters, not only the present students
			codeSubmissionController.open(state.getAttendanceSession());
			view.showPanel(codeSubmissionController.getView());
			return;
		}
		view.showActivity(activity, students.size());
	}

	private void handleShuffle() {
		codeSubmissionController.stop();
		students = activities.shuffle(students);
		view.setStudents(students);
		view.showMessage("Students shuffled.");
	}

	private void handleRandomStudent() {
		codeSubmissionController.stop();
		lastPicked = activities.pickRandom(students, lastPicked);
		if (lastPicked != null) {
			view.showPickedStudent(lastPicked);
		}
	}

	private void handleRandomGroups(int groupSize) {
		codeSubmissionController.stop();
		view.showGroups(activities.randomGroups(students, groupSize));
	}
}
