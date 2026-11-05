package views;

import java.util.List;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/**
 * The row of navigation buttons at the top of the window. It only reports
 * clicks; AppController decides what each click does.
 */
public class MenuBarView extends HBox {

	private static final double BAR_WIDTH = 805.0;
	private static final double BAR_HEIGHT = 50.0;
	private static final double BUTTON_HEIGHT = 35.0;

	private final Button btnHome = new Button("Home");
	private final Button btnRosterManager = new Button("Roster Manager");
	private final Button btnAttendance = new Button("Attendance");
	private final Button btnSlideActivities = new Button("Slide Activities");
	private final Button btnQuiz = new Button("Quiz");
	private final Button btnCodeSubmission = new Button("Code Submission");
	private final Button btnPresentation = new Button("Presentation");
	private final Button btnEC = new Button("EC"); // not shown yet

	private final List<Button> listButtons = List.of(btnHome, btnRosterManager, btnAttendance,
			btnSlideActivities, btnQuiz, btnCodeSubmission, btnPresentation);

	private Runnable onHomeClicked;
	private Runnable onRosterClicked;
	private Runnable onAttendanceClicked;
	private Runnable onSlideActivitiesClicked;

	public MenuBarView() {
		setPrefHeight(BAR_HEIGHT);
		setPrefWidth(BAR_WIDTH);

		double buttonWidth = BAR_WIDTH / listButtons.size();
		for (Button button : listButtons) {
			button.setPrefWidth(buttonWidth);
			button.setPrefHeight(BUTTON_HEIGHT);
			getChildren().add(button);
		}
		wireButtons();
	}

	private void wireButtons() {
		btnHome.setOnAction(e -> run(onHomeClicked));
		btnRosterManager.setOnAction(e -> run(onRosterClicked));
		btnAttendance.setOnAction(e -> run(onAttendanceClicked));
		btnSlideActivities.setOnAction(e -> run(onSlideActivitiesClicked));
	}

	private static void run(Runnable handler) {
		if (handler != null) {
			handler.run();
		}
	}

	public void setOnHomeClicked(Runnable r) { onHomeClicked = r; }
	public void setOnRosterClicked(Runnable r) { onRosterClicked = r; }
	public void setOnAttendanceClicked(Runnable r) { onAttendanceClicked = r; }
	public void setOnSlideActivitiesClicked(Runnable r) { onSlideActivitiesClicked = r; }
}
