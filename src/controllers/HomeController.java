package controllers;

import java.io.File;

import javafx.scene.Parent;
import models.ImageLibrary;
import views.HomeView;

/** Finds the home screen images (model) and hands them to the {@link HomeView}. */
public class HomeController {

	private final HomeView view = new HomeView();

	public HomeController(File imagesFolder) {
		view.setImages(new ImageLibrary(imagesFolder).getImageUrls());
	}

	public Parent getView() {
		return view;
	}
}
