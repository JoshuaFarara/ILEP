package views;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.SVGPath;

/**
 * The row of navigation buttons at the top of the window, plus a gear button for Settings
 * (issue #13). It only reports clicks; AppController decides what each click does.
 *
 * The buttons share the window's width and grow with it (issue #16).
 *
 * Slide Activities, Quiz, Code Submission and Presentation live in the
 * Class Session screen's "Activities" menu.
 */
public class MenuBarView extends HBox {

	private static final double BUTTON_MINIMUM_HEIGHT = 35.0;
	private static final double GEAR_ICON_SIZE = 24.0;
	private static final double DEFAULT_TEXT_SIZE = 13.0;

	/** Gear outline from Google's Material Icons "settings" (Apache License 2.0), drawn in a 24x24 box. */
	private static final String GEAR_ICON_PATH = "M19.14,12.94c0.04-0.3,0.06-0.61,0.06-0.94c0-0.32-0.02-0.64-0.07-0.94l2.03-1.58"
			+ "c0.18-0.14,0.23-0.41,0.12-0.61l-1.92-3.32c-0.12-0.22-0.37-0.29-0.59-0.22l-2.39,0.96c-0.5-0.38-1.03-0.7-1.62-0.94"
			+ "L14.4,2.81c-0.04-0.24-0.24-0.41-0.48-0.41h-3.84c-0.24,0-0.43,0.17-0.47,0.41L9.25,5.35C8.66,5.59,8.12,5.92,7.63,6.29"
			+ "L5.24,5.33c-0.22-0.08-0.47,0-0.59,0.22L2.74,8.87C2.62,9.08,2.66,9.34,2.86,9.48l2.03,1.58C4.84,11.36,4.8,11.69,4.8,12"
			+ "s0.02,0.64,0.07,0.94l-2.03,1.58c-0.18,0.14-0.23,0.41-0.12,0.61l1.92,3.32c0.12,0.22,0.37,0.29,0.59,0.22l2.39-0.96"
			+ "c0.5,0.38,1.03,0.7,1.62,0.94l0.36,2.54c0.05,0.24,0.24,0.41,0.48,0.41h3.84c0.24,0,0.44-0.17,0.47-0.41l0.36-2.54"
			+ "c0.59-0.24,1.13-0.56,1.62-0.94l2.39,0.96c0.22,0.08,0.47,0,0.59-0.22l1.92-3.32c0.12-0.22,0.07-0.47-0.12-0.61"
			+ "L19.14,12.94z M12,15.6c-1.98,0-3.6-1.62-3.6-3.6s1.62-3.6,3.6-3.6s3.6,1.62,3.6,3.6S13.98,15.6,12,15.6z";

	private final Button btnHome = new Button("Home");
	private final Button btnRosterManager = new Button("Roster Manager");
	private final Button btnAttendance = new Button("Attendance");
	private final Button btnClassSession = new Button("Class Session");
	private final Button btnSettings = new Button();
	private final Button btnEC = new Button("EC"); // not shown yet

	private final List<Button> listButtons = List.of(btnHome, btnRosterManager, btnAttendance, btnClassSession);

	private Runnable onHomeClicked;
	private Runnable onRosterClicked;
	private Runnable onAttendanceClicked;
	private Runnable onClassSessionClicked;
	private Runnable onSettingsClicked;

	public MenuBarView() {
		setSpacing(2);
		setPadding(new Insets(2));
		setAlignment(Pos.CENTER_LEFT);

		for (Button button : listButtons) {
			// Share the width equally and grow with the window
			button.setMaxWidth(Double.MAX_VALUE);
			button.setMinHeight(BUTTON_MINIMUM_HEIGHT);
			HBox.setHgrow(button, Priority.ALWAYS);
			getChildren().add(button);
		}
		getChildren().add(buildSettingsButton());
		wireButtons();
	}

	private Button buildSettingsButton() {
		SVGPath gearIcon = new SVGPath();
		gearIcon.setContent(GEAR_ICON_PATH);
		gearIcon.getStyleClass().add("settings-gear-icon");
		// The icon grows with the text size chosen in Settings
		btnSettings.fontProperty().addListener((observable, oldFont, newFont) -> {
			double scale = newFont.getSize() / DEFAULT_TEXT_SIZE;
			gearIcon.setScaleX(scale);
			gearIcon.setScaleY(scale);
		});
		// A Group's size includes the icon's scaling, so the button grows to fit it
		btnSettings.setGraphic(new Group(gearIcon));
		btnSettings.setMinWidth(GEAR_ICON_SIZE + 24);
		btnSettings.setMinHeight(BUTTON_MINIMUM_HEIGHT);
		btnSettings.setTooltip(new Tooltip("Settings"));
		btnSettings.setAccessibleText("Settings");
		btnSettings.getStyleClass().add("settings-gear-button");
		return btnSettings;
	}

	private void wireButtons() {
		btnHome.setOnAction(e -> run(onHomeClicked));
		btnRosterManager.setOnAction(e -> run(onRosterClicked));
		btnAttendance.setOnAction(e -> run(onAttendanceClicked));
		btnClassSession.setOnAction(e -> run(onClassSessionClicked));
		btnSettings.setOnAction(e -> run(onSettingsClicked));
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
	public void setOnSettingsClicked(Runnable r) { onSettingsClicked = r; }
}
