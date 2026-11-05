package app;

import java.io.File;

import controllers.AppController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point. Only creates the window; AppController builds everything that
 * goes in it (models, AppState, views, and the controllers that connect them).
 */
public class App extends Application {
	final int WINDOW_WIDTH = 805;
	final int WINDOW_HEIGHT = 600;
//	final static File IMAGES_FOLDER = new File("C:\\Users\\jfarara\\Documents\\Github\\ILEP\\src\\assets\\images"); //laptop
	final static File IMAGES_FOLDER = new File("H:\\git\\ILEP\\src\\assets\\images"); //office desktop

	@Override // Override the start method in the Application class
	public void start(Stage primaryStage) {
		System.out.println("Application starting...");

		AppController appController = new AppController(IMAGES_FOLDER);

		Scene scene = new Scene(appController.getView(), WINDOW_WIDTH, WINDOW_HEIGHT);
		scene.getStylesheets().add("src/views/stylesheet.css");
		primaryStage.setTitle("Interactive Learning and Engagement Platform"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage
	}

	public static void main(String[] args) {
		launch(args);
	}
}
