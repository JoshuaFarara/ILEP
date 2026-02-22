package views;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class RosterManagerView extends Application {

    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {

        Label title = new Label("Interactive Learning and Engagement Platform");
        TextField selectRoster = new TextField();
        selectRoster.setPromptText("Select a Roster"); //change to a dropdown menu, or list display

// Create a button and place it in the scene
        Button btOK = new Button("OK");
        Scene scene = new Scene(btOK, 200, 250);
        primaryStage.setTitle("MyJavaFX"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage
    }

    public static void main(String[] args) {
        launch(args);
    }

}
