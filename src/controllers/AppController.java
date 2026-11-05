package controllers;

import java.io.File;

import controllers.AppState.Screen;
import javafx.scene.Parent;
import models.RosterLoader;
import models.RosterManager;
import views.MainView;
import views.MenuBarView;

/**
 * Top-level controller. Loads the models, creates the shared {@link AppState},
 * builds one controller per screen, and swaps the main window's content when
 * {@link AppState#currentScreenProperty()} changes.
 */
public class AppController {

	private final AppState state;
	private final MainView mainView;
	private final MenuBarView menuBar;

	private final HomeController homeController;
	private final RosterManagerController rosterManagerController;
	private final AttendanceController attendanceController;

	public AppController(File imagesFolder) {
		// Model: load rosters once; every controller shares them through AppState
		RosterManager rosterManager = new RosterManager();
		new RosterLoader().loadAll(rosterManager);
		state = new AppState(rosterManager);

		// Views owned directly by this controller
		menuBar = new MenuBarView();
		mainView = new MainView(menuBar);

		// One controller per screen
		homeController = new HomeController(imagesFolder);
		rosterManagerController = new RosterManagerController(state);
		attendanceController = new AttendanceController(state);

		wireMenu();
		state.currentScreenProperty().addListener((obs, oldScreen, newScreen) -> show(newScreen));
		show(state.getCurrentScreen());
	}

	/** Root node for the Scene. */
	public Parent getView() {
		return mainView;
	}

	private void wireMenu() {
		menuBar.setOnHomeClicked(() -> state.navigateTo(Screen.HOME));
		menuBar.setOnRosterClicked(() -> state.navigateTo(Screen.ROSTER_MANAGER));
		menuBar.setOnAttendanceClicked(() -> state.navigateTo(Screen.ATTENDANCE));
	}

	private void show(Screen screen) {
		switch (screen) {
		case HOME -> mainView.setContent(homeController.getView());
		case ROSTER_MANAGER -> mainView.setContent(rosterManagerController.getView());
		case ATTENDANCE -> mainView.setContent(attendanceController.getView());
		}
	}
}
