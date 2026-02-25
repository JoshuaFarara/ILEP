package views;

import assets.images.*;

import java.util.ArrayList;
import java.util.Arrays;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class App extends Application {
	// add window datamembers
	final int WINDOW_WIDTH = 805;
	final int WINDOW_HEIGHT = 600;
	// Add Nodes

	// final Button btnRosterManager = new Button("Roster Manager");
	// final Button btnAttendance = new Button("Attendance");
	// final Button btnSlideActivities = new Button("Slide Activities");
	// final Button btnQuiz = new Button("Quiz");
	// final Button btnCodeSubmission = new Button("Code Submission");
	// final Button btnPresentation = new Button("Presentation");
	// final Button btnEC = new Button("EC");

	// Button[] arrButtons = { btnRosterManager, btnAttendance, btnSlideActivities, btnQuiz, btnCodeSubmission, btnPresentation };
	// protected ArrayList<Button> listButtons = new ArrayList<>(Arrays.asList(arrButtons));
	// // for (Button button : listButtons) {
	// 	button.setPrefWidth(120);
	// }

	@Override // Override the start method in the Application class
	public void start(Stage primaryStage) {
		MenuBarView menuBarView = new MenuBarView();		
		// Adding background image
		Image image = new Image(getClass().getResource("/assets/images/sweetHeartCircleFront.jpg").toExternalForm());
		ImageView imageView = new ImageView(image);
		imageView.setFitHeight(WINDOW_HEIGHT);
		imageView.setFitWidth(WINDOW_WIDTH);
		imageView.setOpacity(.25); // add opacity to an image

		//MEnuBar menuBar = new MenuBar();
		// Create the baseline view and add buttons to the view
		// for (Button button : menuBarView.listButtons) {
		// 	button.setPrefWidth(120);
		// 	button.setPrefHeight(40);
		// }

		// Label title = new Label("Interactive Learning and Engagement Platform");
		// TextField selectRoster = new TextField();
		// selectRoster.setPromptText("Select a Roster"); //change to a dropdown menu,
		// or list display

		// Home/Main Text
		Text text = new Text();
		text.setText("Interactive Learning and Engagement Platform");
		text.setFont(Font.font("Abyssinica SIL", FontWeight.EXTRA_BOLD, 20.0));
		text.setFill(Color.BLUE);
		text.setX(100);
		text.setY(100);

		Pane pane = new Pane(); // pane is used to show the text on the screen, used before showing gridpane
		pane.getChildren().add(text);

		/*
		 * GridPane Will need to replace GridPane for scalability. Better to place
		 * buttons with a for loop Adding buttons to the gridpane layout
		 */
		

		MenuBarView menuBar = new MenuBarView();
		GridPane gridPane = new GridPane();
		gridPane.getChildren().add(menuBar.addButtonsToHBox()); 
		// center this pane and add padding to the gridpane
		// gridPane.setHgap(10); // add horizontal gap between buttons
		gridPane.setVgap(10); // add vertical gap between buttons
		//offset the gridpane to the center of the screen
		// gridPane.setAlignment(Pos.CENTER);
		// gridPane.setAlignment(Pos.TOP_CENTER);
		// gridPane.add(btnRosterManager, 0, 4);
		// gridPane.add(btnAttendance, 1, 4);
		// gridPane.add(btnSlideActivities, 2, 4);
		// gridPane.add(btnQuiz, 3, 4);
		// gridPane.add(btnCodeSubmission, 4, 4);
		// gridPane.add(btnPresentation, 5, 4);
		// gridPane.add(btnEC, 6, 4);
		

		// Adding stackpane to hold the background image, pane, and buttons on the
		// gridpane
		StackPane layout = new StackPane();
		layout.getChildren().addAll(imageView, pane,  gridPane);

		// Scene creation and display
		Scene scene = new Scene(layout, WINDOW_WIDTH, WINDOW_HEIGHT);
		primaryStage.setTitle("Interactive Learning and Engagement Platform"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage
	}

	public static void main(String[] args) {
		launch(args);
	}

}