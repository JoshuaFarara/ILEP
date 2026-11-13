package controllers;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Random;

import controllers.AppState.Screen;
import javafx.scene.Parent;
import models.ImageLibrary;
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

	// Fixed backgrounds (issue #17); bundled with the app in src/assets/images
	private static final String ATTENDANCE_BACKGROUND = "/assets/images/eduChemBuildingLake.jpg";
	private static final String CLASS_SESSION_BACKGROUND = "/assets/images/attendance-background.png";
	private static final String FALLBACK_BACKGROUND = "/assets/images/sweetHeartCircleFront.jpg";

	private final AppState state;
	private final MainView mainView;
	private final MenuBarView menuBar;

	private final HomeController homeController;
	private final RosterManagerController rosterManagerController;
	private final AttendanceController attendanceController;
	private final ClassSessionController classSessionController;
	private final SettingsController settingsController;

	// Chosen at random once per run, from the images folder (issue #17)
	private final String homeBackgroundUrl;

	public AppController(File imagesFolder) {
		// Model: load rosters once; every controller shares them through AppState
		RosterManager rosterManager = new RosterManager();
		new RosterLoader().loadAll(rosterManager);
		state = new AppState(rosterManager);

		// Views owned directly by this controller
		menuBar = new MenuBarView();
		mainView = new MainView(menuBar);

		// A few random images per run instead of every image in the folder, so startup stays fast
		List<String> homeImageUrls = new ImageLibrary(imagesFolder)
				.pickRandomImageUrls(HomeController.MAXIMUM_HOME_IMAGES, new Random());
		homeBackgroundUrl = homeImageUrls.isEmpty() ? bundledImageUrl(FALLBACK_BACKGROUND) : homeImageUrls.get(0);

		// One controller per screen
		homeController = new HomeController(homeImageUrls);
		rosterManagerController = new RosterManagerController(state);
		attendanceController = new AttendanceController(state);
		classSessionController = new ClassSessionController(state);
		// Applies the saved display settings (dark mode, text size, font, color) right away
		settingsController = new SettingsController(state, mainView::applyDisplaySettings);

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
		menuBar.setOnClassSessionClicked(() -> state.navigateTo(Screen.CLASS_SESSION));
		menuBar.setOnSettingsClicked(() -> state.navigateTo(Screen.SETTINGS));
	}

	private void show(Screen screen) {
		mainView.setBackgroundImage(backgroundFor(screen));
		switch (screen) {
		case HOME -> mainView.setContent(homeController.getView());
		case ROSTER_MANAGER -> mainView.setContent(rosterManagerController.getView());
		case ATTENDANCE -> mainView.setContent(attendanceController.getView());
		case CLASS_SESSION -> mainView.setContent(classSessionController.getView());
		case SETTINGS -> mainView.setContent(settingsController.getView());
		}
	}

	/** Home, Roster Manager and Settings share this run's random image; Attendance and Class Session have their own. */
	private String backgroundFor(Screen screen) {
		return switch (screen) {
		case HOME, ROSTER_MANAGER, SETTINGS -> homeBackgroundUrl;
		case ATTENDANCE -> bundledImageUrl(ATTENDANCE_BACKGROUND);
		case CLASS_SESSION -> bundledImageUrl(CLASS_SESSION_BACKGROUND);
		};
	}

	/** URL of an image bundled with the app, or null if it is missing (the screen then has no background). */
	private static String bundledImageUrl(String resourcePath) {
		URL resource = AppController.class.getResource(resourcePath);
		if (resource == null) {
			System.err.println("Background image not found: " + resourcePath);
			return null;
		}
		return resource.toExternalForm();
	}
}
