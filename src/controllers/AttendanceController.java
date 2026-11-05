package controllers;

import java.io.IOException;
import java.util.List;

import javafx.scene.Parent;
import models.Attendance;
import models.Roster;
import models.Student;
import views.AttendanceView;

/**
 * Handles the Attendance screen. Shows the roster picker when there is no
 * roll call in {@link AppState}, otherwise shows the roll call. All changes
 * to the {@link Attendance} model happen here, never in the view.
 */
public class AttendanceController {

	private final AppState state;
	private final AttendanceView view = new AttendanceView();

	public AttendanceController(AppState state) {
		this.state = state;

		view.setOnStartRollCall(this::handleStartRollCall);
		view.setOnMarkChanged(this::handleMarkChanged);
		view.setOnMarkAll(this::handleMarkAll);
		view.setOnSave(this::handleSave);
		view.setOnChangeRosters(this::handleChangeRosters);
		view.setOnStartClassSession(() -> state.navigateTo(AppState.Screen.CLASS_SESSION));

		// Re-render whenever a roll call is started or ended anywhere in the app
		state.attendanceSessionProperty().addListener((obs, oldSession, newSession) -> render(newSession));
		render(state.getAttendanceSession());
	}

	public Parent getView() {
		return view;
	}

	// ------------------------------------------------------------ rendering
	private void render(Attendance session) {
		if (session == null) {
			view.showRosterPicker(state.getRosters());
			return;
		}
		view.showRollCall(session.getDate(), session.getRosters(), session.getStudents(),
				session::isPresent, session::getRosterFor);
		updateCounts(session);
	}

	private void updateCounts(Attendance session) {
		view.setCounts(session.getPresentStudents().size(), session.getAbsentStudents().size(),
				session.getStudents().size());
	}

	// ------------------------------------------------------------ view events
	private void handleStartRollCall(List<Roster> rosters) {
		if (rosters == null || rosters.isEmpty()) {
			return;
		}
		state.startAttendanceSession(rosters); // listener renders the roll call
	}

	private void handleMarkChanged(Student student, boolean present) {
		Attendance session = state.getAttendanceSession();
		if (session == null) {
			return;
		}
		session.mark(student, present);
		updateCounts(session);
	}

	private void handleMarkAll(boolean present) {
		Attendance session = state.getAttendanceSession();
		if (session == null) {
			return;
		}
		session.markAll(present);
		view.setMarks(session::isPresent);
		updateCounts(session);
	}

	private void handleSave() {
		Attendance session = state.getAttendanceSession();
		if (session == null) {
			return;
		}
		try {
			session.saveAttendance();
			view.showSaveSuccess(session.getDate(), session.getPresentStudents().size(),
					session.getAbsentStudents().size());
		} catch (IOException ex) {
			view.showSaveError(ex.getMessage());
		}
	}

	private void handleChangeRosters() {
		state.endAttendanceSession(); // listener shows the roster picker
	}
}
