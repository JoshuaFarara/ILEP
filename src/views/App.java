package views;


import javafx.application.Application;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.RosterLoader;
import models.RosterManager;
import views.MenuBarView;
import views.RosterManagerView;

public class App extends Application {
	// add window data members
	final int WINDOW_WIDTH = 805;
	final int WINDOW_HEIGHT = 600;
	final int BACKGROUND_IMAGE_WIDTH = 805;
	final int BACKGROUND_IMAGE_HEIGHT = 525;
	// Add models
	

	@Override // Override the start method in the Application class
	public void start(Stage primaryStage) {		
		
		
		/*
		Adding background image
		// Refactor this code to a method in the future-
		// refactor this into a class called BackgroundImageView
		*/
		Image image = new Image(getClass().getResource("/assets/images/sweetHeartCircleFront.jpg").toExternalForm());
		ImageView imageView = new ImageView(image);
		imageView.setFitWidth(BACKGROUND_IMAGE_WIDTH);
		imageView.setFitHeight(BACKGROUND_IMAGE_HEIGHT);
		imageView.setOpacity(.25); // add opacity to an image
		
		//Window Layout and Structure
		BorderPane root = new BorderPane();
		
		

		// Home/Main Text
		Pane pane = new Pane(); // pane is used to show the text on the screen, used before showing gridpane
		Text text = new Text();
		text.setText("Interactive Learning and Engagement Platform");
		text.setFont(Font.font("Abyssinica SIL", FontWeight.EXTRA_BOLD, 20.0));
		text.setFill(Color.BLUE);
		text.setX(100);
		text.setY(100);
		pane.getChildren().add(text);

		// Display Views
		// Display menu bar
		MenuBarView menuBar = new MenuBarView();
		root.setTop(menuBar.addButtonsToHBox());
		// when a menu button is selected the center root should change to that view
		RosterManager rm = new RosterManager();
		RosterLoader rl = new RosterLoader();
		RosterManagerView rmv = new RosterManagerView(rl,rm);
		root.setCenter(rmv);
		
		//Center of BorderPane
		
		
//		menubar.showCenterPane();
		
		//Button CLick Views: RosterManager, Attendance, SlideActivities, Quiz, CodeSubmission, Presenation
//		RosterManagerView rosterManagerView = new RosterManagerView();

		
		
		// Adding stackpane to hold the background image, pane, and buttons on the
		// gridpane
		StackPane layout = new StackPane();
			layout.getChildren().addAll(imageView, root); //  pane, 

		// Scene creation and display
		Scene scene = new Scene(layout, WINDOW_WIDTH, WINDOW_HEIGHT);
//		scene.getStylesheets().add("views/stylesheet.css");
		primaryStage.setTitle("Interactive Learning and Engagement Platform"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage
		
		
	}

	public static void main(String[] args) {
		launch(args);
	}

}