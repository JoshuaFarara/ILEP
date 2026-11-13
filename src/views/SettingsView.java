package views;

import java.util.List;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import models.DisplaySettings;

/**
 * Settings screen (issues #14 and #15): dark mode, text size, font and text color, plus buttons
 * to go to the other screens. Like the other views it only shows what it is given and reports
 * what the user changes; SettingsController does the work.
 */
public class SettingsView extends ScrollPane {

	private final ToggleButton darkModeToggle = new ToggleButton();
	private final Label textSizeLabel = new Label();
	private final Button decreaseTextSize = new Button("A−");
	private final Button increaseTextSize = new Button("A+");
	private final Button resetTextSize = new Button("Reset");
	private final ComboBox<String> fontChoice = new ComboBox<>();
	private final ColorPicker textColorPicker = new ColorPicker();
	private final Button resetTextColor = new Button("Use theme colors");
	private final Label contrastWarning = new Label();
	private final Label saveStatus = new Label();

	// True while the controller fills in the controls, so those changes are not reported back
	private boolean updatingControls = false;

	private Consumer<Boolean> onDarkModeChanged;
	private Runnable onIncreaseTextSize;
	private Runnable onDecreaseTextSize;
	private Runnable onResetTextSize;
	private Consumer<String> onFontChanged;
	private Consumer<String> onTextColorChanged;
	private Runnable onResetTextColor;
	private Runnable onGoHome;
	private Runnable onGoRosterManager;
	private Runnable onGoAttendance;
	private Runnable onGoClassSession;

	public SettingsView() {
		setFitToWidth(true);
		getStyleClass().add("code-submission-scroll"); // transparent, so the background image shows

		Label title = new Label("Settings");
		title.getStyleClass().add("heading");

		VBox content = new VBox(14, title, buildAppearanceSection(), buildNavigationSection(), saveStatus);
		content.setPadding(new Insets(14));
		setContent(content);
		wireControls();
	}

	// ------------------------------------------------------------ controller API
	/** Shows the current settings; only the given fonts (those installed) are offered. */
	public void showSettings(DisplaySettings settings, List<String> availableFonts) {
		updatingControls = true;
		try {
			darkModeToggle.setSelected(settings.isDarkMode());
			darkModeToggle.setText(settings.isDarkMode() ? "Dark mode: On" : "Dark mode: Off");

			textSizeLabel.setText(settings.getTextSize() + (settings.getTextSize() == DisplaySettings.DEFAULT_TEXT_SIZE
					? " (default)" : ""));
			decreaseTextSize.setDisable(settings.getTextSize() <= DisplaySettings.MINIMUM_TEXT_SIZE);
			increaseTextSize.setDisable(settings.getTextSize() >= DisplaySettings.MAXIMUM_TEXT_SIZE);
			resetTextSize.setDisable(settings.getTextSize() == DisplaySettings.DEFAULT_TEXT_SIZE);

			fontChoice.setItems(FXCollections.observableArrayList(availableFonts));
			fontChoice.setValue(settings.getFontFamily());

			String textColor = settings.getTextColorHex();
			textColorPicker.setValue(textColor == null ? (settings.isDarkMode() ? Color.WHITE : Color.BLACK)
					: Color.web(textColor));
			resetTextColor.setDisable(textColor == null);
		} finally {
			updatingControls = false;
		}
	}

	/** A readability warning under the text color, or none when message is null. */
	public void showContrastWarning(String message) {
		contrastWarning.setText(message == null ? "" : message);
		contrastWarning.setVisible(message != null);
		contrastWarning.setManaged(message != null);
	}

	/** "Saved" after each change, or an error message. */
	public void showSaveStatus(String message, boolean isError) {
		saveStatus.setText(message);
		saveStatus.getStyleClass().removeAll("settings-warning", "settings-saved");
		saveStatus.getStyleClass().add(isError ? "settings-warning" : "settings-saved");
	}

	// ------------------------------------------------------------ handlers
	public void setOnDarkModeChanged(Consumer<Boolean> handler) { onDarkModeChanged = handler; }
	public void setOnIncreaseTextSize(Runnable handler) { onIncreaseTextSize = handler; }
	public void setOnDecreaseTextSize(Runnable handler) { onDecreaseTextSize = handler; }
	public void setOnResetTextSize(Runnable handler) { onResetTextSize = handler; }
	public void setOnFontChanged(Consumer<String> handler) { onFontChanged = handler; }
	/** Called with the chosen color as "#rrggbb". */
	public void setOnTextColorChanged(Consumer<String> handler) { onTextColorChanged = handler; }
	public void setOnResetTextColor(Runnable handler) { onResetTextColor = handler; }
	public void setOnGoHome(Runnable handler) { onGoHome = handler; }
	public void setOnGoRosterManager(Runnable handler) { onGoRosterManager = handler; }
	public void setOnGoAttendance(Runnable handler) { onGoAttendance = handler; }
	public void setOnGoClassSession(Runnable handler) { onGoClassSession = handler; }

	// ------------------------------------------------------------ layout
	private Node buildAppearanceSection() {
		decreaseTextSize.setTooltip(new Tooltip("Smaller text"));
		decreaseTextSize.setAccessibleText("Decrease text size");
		increaseTextSize.setTooltip(new Tooltip("Larger text"));
		increaseTextSize.setAccessibleText("Increase text size");
		textSizeLabel.setMinWidth(Region.USE_PREF_SIZE);
		HBox textSizeRow = row(decreaseTextSize, textSizeLabel, increaseTextSize, resetTextSize);

		// Each font name is shown in its own font, so the instructor can see it before choosing
		fontChoice.setCellFactory(listView -> new FontNameCell());
		fontChoice.setButtonCell(new FontNameCell());
		fontChoice.setMaxWidth(Double.MAX_VALUE);

		contrastWarning.getStyleClass().add("settings-warning");
		contrastWarning.setWrapText(true);
		showContrastWarning(null);
		VBox textColorBox = new VBox(6, row(textColorPicker, resetTextColor), contrastWarning);

		Label preview = new Label("Preview: The quick brown fox jumps over the lazy dog. 0123456789");
		preview.setWrapText(true);

		GridPane grid = new GridPane();
		grid.setHgap(14);
		grid.setVgap(10);
		ColumnConstraints labelColumn = new ColumnConstraints();
		labelColumn.setMinWidth(Region.USE_PREF_SIZE);
		ColumnConstraints controlColumn = new ColumnConstraints();
		controlColumn.setHgrow(Priority.ALWAYS);
		grid.getColumnConstraints().setAll(labelColumn, controlColumn);
		grid.addRow(0, new Label("Theme"), darkModeToggle);
		grid.addRow(1, new Label("Text size"), textSizeRow);
		grid.addRow(2, new Label("Font"), fontChoice);
		grid.addRow(3, new Label("Text color"), textColorBox);

		return section("Appearance", grid, preview);
	}

	private Node buildNavigationSection() {
		Button home = new Button("Home");
		home.setOnAction(event -> run(onGoHome));
		Button rosterManager = new Button("Roster Manager");
		rosterManager.setOnAction(event -> run(onGoRosterManager));
		Button attendance = new Button("Attendance");
		attendance.setOnAction(event -> run(onGoAttendance));
		Button classSession = new Button("Class Session");
		classSession.setOnAction(event -> run(onGoClassSession));
		FlowPane buttons = new FlowPane(10, 10, home, rosterManager, attendance, classSession);
		return section("Go to", buttons);
	}

	private void wireControls() {
		darkModeToggle.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
			if (!updatingControls && onDarkModeChanged != null) {
				onDarkModeChanged.accept(isSelected);
			}
		});
		decreaseTextSize.setOnAction(event -> run(onDecreaseTextSize));
		increaseTextSize.setOnAction(event -> run(onIncreaseTextSize));
		resetTextSize.setOnAction(event -> run(onResetTextSize));
		fontChoice.valueProperty().addListener((observable, oldFont, newFont) -> {
			if (!updatingControls && newFont != null && onFontChanged != null) {
				onFontChanged.accept(newFont);
			}
		});
		textColorPicker.valueProperty().addListener((observable, oldColor, newColor) -> {
			if (!updatingControls && newColor != null && onTextColorChanged != null) {
				onTextColorChanged.accept(toHex(newColor));
			}
		});
		resetTextColor.setOnAction(event -> run(onResetTextColor));
	}

	// ------------------------------------------------------------ helpers
	private static final class FontNameCell extends ListCell<String> {
		@Override
		protected void updateItem(String fontFamily, boolean empty) {
			super.updateItem(fontFamily, empty);
			setText(empty || fontFamily == null ? null : fontFamily);
			setStyle(empty || fontFamily == null ? "" : "-fx-font-family: \"" + fontFamily + "\";");
		}
	}

	private static VBox section(String heading, Node... content) {
		Label headingLabel = new Label(heading);
		headingLabel.getStyleClass().add("heading-small");
		VBox section = new VBox(10, headingLabel);
		section.getChildren().addAll(content);
		section.getStyleClass().add("settings-section");
		return section;
	}

	private static HBox row(Node... nodes) {
		HBox row = new HBox(8, nodes);
		row.setAlignment(Pos.CENTER_LEFT);
		return row;
	}

	private static String toHex(Color color) {
		return String.format("#%02x%02x%02x", Math.round(color.getRed() * 255), Math.round(color.getGreen() * 255),
				Math.round(color.getBlue() * 255));
	}

	private static void run(Runnable handler) {
		if (handler != null) {
			handler.run();
		}
	}
}
