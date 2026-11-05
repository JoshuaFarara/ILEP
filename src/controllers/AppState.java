package controllers;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Attendance;
import models.Roster;
import models.RosterManager;

/**
 * Shared, observable state for the running application (GitHub issue #5).
 *
 * One instance is created by {@link AppController} and handed to every
 * controller. Controllers never talk to each other directly: they read and
 * change AppState, and listen to it for changes made elsewhere.
 *
 * Views never see AppState. Only controllers do.
 */
public class AppState {

	/** The screens the main window can show in its center area. */
	public enum Screen {
		HOME, ROSTER_MANAGER, ATTENDANCE
	}

	private final RosterManager rosterManager;

	// Rosters currently selected in the Roster Manager
	private final ObservableList<Roster> selectedRosters = FXCollections.observableArrayList();
	private final ObservableList<Roster> selectedRostersReadOnly = FXCollections
			.unmodifiableObservableList(selectedRosters);

	// The roll call in progress, or null when none has been started
	private final ObjectProperty<Attendance> attendanceSession = new SimpleObjectProperty<>();

	// Which screen the main window is showing
	private final ObjectProperty<Screen> currentScreen = new SimpleObjectProperty<>(Screen.HOME);

	public AppState(RosterManager rosterManager) {
		this.rosterManager = rosterManager;
	}

	// ------------------------------------------------------------ rosters
	public RosterManager getRosterManager() {
		return rosterManager;
	}

	/** Every loaded roster. */
	public List<Roster> getRosters() {
		return rosterManager.getRosters();
	}

	public ObservableList<Roster> getSelectedRosters() {
		return selectedRostersReadOnly;
	}

	public void setSelectedRosters(List<Roster> rosters) {
		selectedRosters.setAll(rosters == null ? List.of() : rosters);
	}

	// ------------------------------------------------------------ attendance
	public Attendance getAttendanceSession() {
		return attendanceSession.get();
	}

	public ReadOnlyObjectProperty<Attendance> attendanceSessionProperty() {
		return attendanceSession;
	}

	/** Starts a new roll call for the given rosters, replacing any current one. */
	public Attendance startAttendanceSession(List<Roster> rosters) {
		Attendance session = new Attendance(new ArrayList<>(rosters));
		attendanceSession.set(session);
		return session;
	}

	/** Discards the current roll call (e.g. to pick different rosters). */
	public void endAttendanceSession() {
		attendanceSession.set(null);
	}

	// ------------------------------------------------------------ navigation
	public Screen getCurrentScreen() {
		return currentScreen.get();
	}

	public ReadOnlyObjectProperty<Screen> currentScreenProperty() {
		return currentScreen;
	}

	public void navigateTo(Screen screen) {
		currentScreen.set(screen);
	}
}
