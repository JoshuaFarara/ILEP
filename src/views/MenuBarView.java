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
        // addButtonsToHBox();
        // displayMenuBarScene();
    }

    protected Scene displayMenuBarScene() {
        VBox root = new VBox();
        root.getChildren().add(addButtonsToHBox());
        return new Scene(root, 100, 100);
    }

    
    protected HBox addButtonsToHBox() {
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20;");
        for (Button button : listButtons) {
            buttonBox.getChildren().add(button);
        }
        return buttonBox;
    }


    
}
