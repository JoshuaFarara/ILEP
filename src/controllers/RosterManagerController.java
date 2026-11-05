package controllers;

import java.util.List;

import controllers.AppState.Screen;
import javafx.scene.Parent;
import models.Roster;
import views.RosterManagerView;

/**
 * Handles the Roster Manager screen: keeps the roster selection in
 * {@link AppState} and starts a roll call when "Take Attendance" is clicked.
 */
public class RosterManagerController {

	private final AppState state;
	private final RosterManagerView view = new RosterManagerView();

	public RosterManagerController(AppState state) {
		this.state = state;

		view.setRosters(state.getRosters());
		view.setOnRosterFocused(view::showRosterDetail);
		view.setOnSelectionChanged(this::handleSelectionChanged);
		view.setOnTakeAttendance(this::handleTakeAttendance);

		handleSelectionChanged(List.of());
	}

	public Parent getView() {
		return view;
	}

	private void handleSelectionChanged(List<Roster> selected) {
		state.setSelectedRosters(selected);

		int students = 0;
		for (Roster r : selected) {
			students += r.getStudents().size();
		}
		view.setSelectionSummary(selected.size(), students);
		view.setTakeAttendanceEnabled(!selected.isEmpty());
	}

	private void handleTakeAttendance() {
		List<Roster> selected = state.getSelectedRosters();
		if (selected.isEmpty()) {
			return;
		}
		state.startAttendanceSession(selected);
		state.navigateTo(Screen.ATTENDANCE);
	}
}
