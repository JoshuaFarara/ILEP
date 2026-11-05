package views;

import java.util.List;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/**
 * The row of navigation buttons at the top of the window. It only reports
 * clicks; AppController decides what each click does.
 *
 * Slide Activities, Quiz, Code Submission and Presentation now live in the
 * Class Session screen's "Activities" menu.
 */
public class MenuBarView extends HBox {

	private static final double BAR_WIDTH = 805.0;
	private static final double BAR_HEIGHT = 50.0;
	private static final double BUTTON_HEIGHT = 35.0;

	private final Button btnHome = new Button("Home");
	private final Button btnRosterManager = new Button("Roster Manager");
	private final Button btnAttendance = new Button("Attendance");
	private final Button btnClassSession = new Button("Class Session");
	private final Button btnEC = new Button("EC"); // not shown yet

	private final List<Button> listButtons = List.of(btnHome, btnRosterManager, btnAttendance, btnClassSession);

	private Runnable onHomeClicked;
	private Runnable onRosterClicked;
	private Runnable onAttendanceClicked;
	private Runnable onClassSessionClicked;

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
		btnClassSession.setOnAction(e -> run(onClassSessionClicked));
	}

	private static void run(Runnable handler) {
		if (handler != null) {
			handler.run();
		}
	}

	public void setOnHomeClicked(Runnable r) { onHomeClicked = r; }
	public void setOnRosterClicked(Runnable r) { onRosterClicked = r; }
	public void setOnAttendanceClicked(Runnable r) { onAttendanceClicked = r; }
	public void setOnClassSessionClicked(Runnable r) { onClassSessionClicked = r; }
}
