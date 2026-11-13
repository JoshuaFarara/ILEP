package controllers;

import controllers.AppState.Screen;
import javafx.scene.Parent;
import views.HomeView;

/**
 * Runs the Home screen: the clock and greeting live in {@link HomeView}; this controller gives it
 * the featured image (chosen by {@link AppController}) and wires the quick-action buttons.
 */
public class HomeController {

	private final HomeView view = new HomeView();

	/** @param featuredImageUrl the image to show on Home, or null for none */
	public HomeController(AppState state, String featuredImageUrl) {
		view.setFeaturedImage(featuredImageUrl);
		view.setOnTakeAttendance(() -> state.navigateTo(Screen.ATTENDANCE));
		view.setOnStartClassSession(() -> state.navigateTo(Screen.CLASS_SESSION));
		view.setOnOpenRosterManager(() -> state.navigateTo(Screen.ROSTER_MANAGER));
	}

	public Parent getView() {
		return view;
	}
}
