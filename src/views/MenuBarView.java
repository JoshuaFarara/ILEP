package views;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class MenuBarView {

    final Button btnRosterManager = new Button("Roster Manager");
	final Button btnAttendance = new Button("Attendance");
	final Button btnSlideActivities = new Button("Slide Activities");
	final Button btnQuiz = new Button("Quiz");
	final Button btnCodeSubmission = new Button("Code Submission");
	final Button btnPresentation = new Button("Presentation");
	final Button btnEC = new Button("EC");

	Button[] arrButtons = { btnRosterManager, btnAttendance, btnSlideActivities, btnQuiz, btnCodeSubmission, btnPresentation };
	protected ArrayList<Button> listButtons = new ArrayList<>(Arrays.asList(arrButtons));

    public MenuBarView() {
        addButtonsToHBox();
        // displayMenuBarScene();
    }

    protected Scene displayMenuBarScene() {
        VBox root = new VBox();
        root.getChildren().add(addButtonsToHBox());
        return new Scene(root);
    }

    
    protected HBox addButtonsToHBox() {
        HBox buttonBox = new HBox();
        buttonBox.setPrefHeight(50);
        buttonBox.setPrefWidth(805); 
        // buttonBox.setAlignment(Pos.CENTER);
        // buttonBox.setStyle("-fx-padding: 20;"); // does not work, try to add padding to the gridpane instead
        // Set the preferred width to match the window width
         // add padding and bottom border to the menu bar
        // buttonBox.setStyle("-fx-border-color: #A99260; -fx-border-width: 0px 0px 100px 0px;"); // add bottom border to the menu bar
        // buttonBox.setSpacing(10); // add horizontal gap between buttons
        buttonBox.setStyle("-fx-background-color: #041E42;"); // set background color for the menu bar

        for (Button button : listButtons) {
            int numButtons = listButtons.size();
            double buttonWidth = 805.0 / numButtons; // Calculate button width based on number of buttons
            double buttonHeight = 35.0; // Set button height to 50
            button.setPrefWidth(buttonWidth);
            button.setPrefHeight(buttonHeight);
            buttonBox.getChildren().add(button);
        }

        return buttonBox;
    }



}
