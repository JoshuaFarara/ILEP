package controllers;

import java.util.List;

import javafx.scene.Parent;
import views.HomeView;

/** Shows the home screen's images (chosen by {@link AppController}) in the {@link HomeView}. */
public class HomeController {

	/** At most this many images appear on Home, however many the images folder holds (issue #17). */
	public static final int MAXIMUM_HOME_IMAGES = 5;

	private final HomeView view = new HomeView();

	public HomeController(List<String> homeImageUrls) {
		view.setImages(homeImageUrls.subList(0, Math.min(MAXIMUM_HOME_IMAGES, homeImageUrls.size())));
	}

	public Parent getView() {
		return view;
	}
}
