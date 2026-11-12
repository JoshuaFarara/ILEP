package views;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * The Code Submission panel shown inside Class Session. Like the other views it only displays what
 * it is given and reports what the instructor does; CodeSubmissionController does the work.
 *
 * States: settings form, new-assignment form, open (or closed) assignment with live status.
 * An error banner and a busy indicator can appear above any state.
 */
public class CodeSubmissionView extends VBox {

	/** What the instructor typed into the new-assignment form. */
	public record AssignmentForm(String title, String instructions, String starterCode, String artificialIntelligenceUse,
			String artificialIntelligenceNote, LocalDate dueDate, String dueTime) {
	}

	/** One row of the live status table. */
	public record StudentStatusRow(String studentName, boolean presentToday, String status, String lastSaved, int runCount) {
	}

	private static final String defaultStarterCode = """
			import java.util.Scanner;

			public class Main {
			    public static void main(String[] arguments) {
			        Scanner scanner = new Scanner(System.in);
			        // Write your code here
			    }
			}
			""";

	private final Label errorBanner = new Label();
	private final HBox busyRow = new HBox(8);
	private final Label busyMessage = new Label();
	private final VBox content = new VBox(10);

	private final TableView<StudentStatusRow> statusTable = new TableView<>();
	private final Label statusCounts = new Label();
	private final Label lastUpdated = new Label();

	private BiConsumer<String, String> onSaveSettings;
	private Consumer<AssignmentForm> onCreateAssignment;
	private Runnable onRefresh;
	private Runnable onCloseAssignment;
	private Runnable onNewAssignment;
	private Runnable onEditSettings;

	public CodeSubmissionView() {
		super(10);
		setPadding(new Insets(0, 4, 4, 0));
		// Grow into the space beside the student list instead of pushing the list narrower
		setPrefWidth(420);
		setMinWidth(0);

		errorBanner.getStyleClass().add("code-submission-error");
		errorBanner.setWrapText(true);
		errorBanner.setMaxWidth(Double.MAX_VALUE);
		hide(errorBanner);

		ProgressIndicator spinner = new ProgressIndicator();
		spinner.setPrefSize(18, 18);
		busyRow.getChildren().setAll(spinner, busyMessage);
		busyRow.setAlignment(Pos.CENTER_LEFT);
		hide(busyRow);

		buildStatusTable();

		ScrollPane scroll = new ScrollPane(content);
		scroll.setFitToWidth(true);
		scroll.getStyleClass().add("code-submission-scroll");
		VBox.setVgrow(scroll, Priority.ALWAYS);
		getChildren().setAll(errorBanner, busyRow, scroll);
	}

	// ------------------------------------------------------------ states
	/** First-time setup, or changing the service address or API key. */
	public void showSettingsForm(String serviceUrl, boolean apiKeySaved) {
		TextField serviceUrlField = new TextField(serviceUrl);
		serviceUrlField.setPromptText("https://code-submission.example.edu");
		PasswordField apiKeyField = new PasswordField();
		apiKeyField.setPromptText(apiKeySaved ? "Saved. Leave blank to keep it." : "Paste the instructor API key");

		Label explanation = muted("The API key is a password. It is saved only on this computer, in src/appData "
				+ "(which git ignores), and is never shared with students.");
		Button save = new Button("Save settings");
		save.setDefaultButton(true);
		save.setOnAction(event -> {
			if (onSaveSettings != null) {
				onSaveSettings.accept(serviceUrlField.getText(), apiKeyField.getText());
			}
		});

		GridPane form = formGrid();
		form.addRow(0, new Label("Service address"), serviceUrlField);
		form.addRow(1, new Label("Instructor API key"), apiKeyField);

		content.getChildren().setAll(heading("Code Submission setup", 18), explanation, form, save);
	}

	/** No assignment is open for this course yet. */
	public void showAssignmentForm(String courseLabel, int studentCount) {
		TextField titleField = new TextField();
		titleField.setPromptText("Lab 4: Counting with Loops");
		TextArea instructionsArea = new TextArea();
		instructionsArea.setPromptText("What should students write? Include example input and output.");
		instructionsArea.setPrefRowCount(4);
		instructionsArea.setWrapText(true);
		TextArea starterCodeArea = new TextArea(defaultStarterCode);
		starterCodeArea.getStyleClass().add("code-submission-code");
		starterCodeArea.setPrefRowCount(8);

		ChoiceBox<String> artificialIntelligenceChoice = new ChoiceBox<>(
				FXCollections.observableArrayList("forbidden", "restricted", "permitted"));
		artificialIntelligenceChoice.setValue("restricted");
		TextField artificialIntelligenceNoteField = new TextField();
		artificialIntelligenceNoteField.setPromptText("e.g. You may ask an approved AI tool to explain a compiler error.");

		DatePicker dueDatePicker = new DatePicker();
		dueDatePicker.setPromptText("Optional");
		TextField dueTimeField = new TextField("23:59");
		dueTimeField.setPrefColumnCount(5);
		HBox dueRow = new HBox(8, dueDatePicker, new Label("at"), dueTimeField);
		dueRow.setAlignment(Pos.CENTER_LEFT);

		Button create = new Button("Sync roster & create assignment");
		create.setDefaultButton(true);
		create.setOnAction(event -> {
			if (onCreateAssignment != null) {
				onCreateAssignment.accept(new AssignmentForm(titleField.getText(), instructionsArea.getText(),
						starterCodeArea.getText(), artificialIntelligenceChoice.getValue(),
						artificialIntelligenceNoteField.getText(), dueDatePicker.getValue(), dueTimeField.getText()));
			}
		});

		GridPane form = formGrid();
		form.addRow(0, new Label("Title"), titleField);
		form.addRow(1, new Label("Instructions"), instructionsArea);
		form.addRow(2, new Label("Starter code"), starterCodeArea);
		form.addRow(3, new Label("AI rule"), artificialIntelligenceChoice);
		form.addRow(4, new Label("AI note"), artificialIntelligenceNoteField);
		form.addRow(5, new Label("Due"), dueRow);

		Label rosterNote = muted("All " + studentCount + " students on " + courseLabel
				+ " can join. Only random pseudonyms leave this computer: no names, emails, Eagle IDs or CRNs.");
		rosterNote.setWrapText(true);
		content.getChildren().setAll(header("New Code Submission assignment"), rosterNote, form, create);
	}

	/** An assignment is open (or was just closed): join code plus live status. */
	public void showAssignment(String courseLabel, String title, String joinCode, String joinUrl, boolean closed) {
		Label joinCodeLabel = new Label(joinCode);
		joinCodeLabel.getStyleClass().add("code-submission-join-code");
		Label joinUrlLabel = new Label(joinUrl);
		joinUrlLabel.getStyleClass().add("code-submission-muted");
		joinUrlLabel.setWrapText(true);

		Button copyCode = new Button("Copy code");
		copyCode.setOnAction(event -> copyToClipboard(joinCode));
		Button copyLink = new Button("Copy link");
		copyLink.setOnAction(event -> copyToClipboard(joinUrl));
		HBox copyRow = new HBox(8, copyCode, copyLink);

		VBox joinBox = new VBox(4, muted(closed ? "Join code (closed)" : "Students join at the link below with this code and their Eagle ID:"),
				joinCodeLabel, joinUrlLabel, copyRow);
		joinBox.getStyleClass().add("code-submission-join-box");
		joinBox.setPadding(new Insets(10));

		Button refresh = new Button("Refresh now");
		refresh.setOnAction(event -> run(onRefresh));
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		HBox actions = new HBox(8, refresh, statusCounts, spacer);
		actions.setAlignment(Pos.CENTER_LEFT);
		if (closed) {
			Button newAssignment = new Button("New assignment");
			newAssignment.setOnAction(event -> run(onNewAssignment));
			actions.getChildren().add(newAssignment);
		} else {
			Button close = new Button("Close assignment");
			close.setOnAction(event -> {
				if (confirm("Close \"" + title + "\"?",
						"Students can no longer edit, run or submit. Work that wasn't submitted stays as last saved.")) {
					run(onCloseAssignment);
				}
			});
			actions.getChildren().add(close);
		}

		content.getChildren().setAll(header(title), muted(courseLabel + (closed ? "  ·  Closed" : "  ·  Open")),
				joinBox, actions, statusTable, lastUpdated);
	}

	/** Replaces the rows of the status table. */
	public void showStatusRows(List<StudentStatusRow> rows, int notStartedCount, int workingCount, int submittedCount,
			String updatedAtText) {
		statusTable.setItems(FXCollections.observableArrayList(rows));
		statusCounts.setText(notStartedCount + " not started  ·  " + workingCount + " working  ·  " + submittedCount
				+ " submitted");
		lastUpdated.setText("Updated " + updatedAtText + ". Refreshes every 10 seconds while this panel is open.");
	}

	/** A message instead of a form, e.g. when no attendance has been taken. */
	public void showMessage(String message) {
		Label label = new Label(message);
		label.setWrapText(true);
		content.getChildren().setAll(heading("Code Submission", 18), label);
	}

	public void showError(String message) {
		errorBanner.setText(message);
		show(errorBanner);
	}

	public void clearError() {
		hide(errorBanner);
	}

	/** Shows a spinner and disables the panel while the service is working. */
	public void setBusy(boolean busy, String message) {
		busyMessage.setText(message == null ? "" : message);
		if (busy) {
			show(busyRow);
		} else {
			hide(busyRow);
		}
		content.setDisable(busy);
	}

	// ------------------------------------------------------------ handlers
	/** Called with (service address, API key). An empty API key means "keep the saved one". */
	public void setOnSaveSettings(BiConsumer<String, String> handler) { onSaveSettings = handler; }
	public void setOnCreateAssignment(Consumer<AssignmentForm> handler) { onCreateAssignment = handler; }
	public void setOnRefresh(Runnable handler) { onRefresh = handler; }
	public void setOnCloseAssignment(Runnable handler) { onCloseAssignment = handler; }
	public void setOnNewAssignment(Runnable handler) { onNewAssignment = handler; }
	public void setOnEditSettings(Runnable handler) { onEditSettings = handler; }

	// ------------------------------------------------------------ helpers
	private void buildStatusTable() {
		TableColumn<StudentStatusRow, String> nameColumn = new TableColumn<>("Student");
		nameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().studentName()));
		nameColumn.setPrefWidth(170);
		TableColumn<StudentStatusRow, String> presentColumn = new TableColumn<>("Present");
		presentColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().presentToday() ? "Yes" : "No"));
		TableColumn<StudentStatusRow, String> statusColumn = new TableColumn<>("Status");
		statusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().status()));
		TableColumn<StudentStatusRow, String> lastSavedColumn = new TableColumn<>("Last saved");
		lastSavedColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().lastSaved()));
		TableColumn<StudentStatusRow, Integer> runsColumn = new TableColumn<>("Runs");
		runsColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().runCount()));
		runsColumn.setPrefWidth(55);

		statusTable.getColumns().setAll(List.of(nameColumn, presentColumn, statusColumn, lastSavedColumn, runsColumn));
		statusTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
		statusTable.setPlaceholder(new Label("No students yet."));
		statusTable.setPrefHeight(260);
	}

	/** A heading with a small "Settings" button on the right. */
	private HBox header(String title) {
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		Button settings = new Button("Settings");
		settings.setOnAction(event -> run(onEditSettings));
		HBox header = new HBox(heading(title, 18), spacer, settings);
		header.setAlignment(Pos.CENTER_LEFT);
		return header;
	}

	private static GridPane formGrid() {
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(8);
		ColumnConstraints labelColumn = new ColumnConstraints();
		labelColumn.setMinWidth(Region.USE_PREF_SIZE); // labels never shrink to "..."
		ColumnConstraints fieldColumn = new ColumnConstraints();
		fieldColumn.setHgrow(Priority.ALWAYS);
		fieldColumn.setMinWidth(0);
		grid.getColumnConstraints().setAll(labelColumn, fieldColumn);
		return grid;
	}

	private static boolean confirm(String header, String detail) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, detail, ButtonType.OK, ButtonType.CANCEL);
		alert.setTitle("Code Submission");
		alert.setHeaderText(header);
		Optional<ButtonType> answer = alert.showAndWait();
		return answer.isPresent() && answer.get() == ButtonType.OK;
	}

	private static void copyToClipboard(String text) {
		ClipboardContent clipboardContent = new ClipboardContent();
		clipboardContent.putString(text);
		Clipboard.getSystemClipboard().setContent(clipboardContent);
	}

	private static Label muted(String text) {
		Label label = new Label(text);
		label.setWrapText(true);
		label.getStyleClass().add("code-submission-muted");
		return label;
	}

	private static Label heading(String text, double size) {
		Label label = new Label(text);
		label.setFont(Font.font(null, FontWeight.BOLD, size));
		return label;
	}

	private static void show(Node node) {
		node.setVisible(true);
		node.setManaged(true);
	}

	private static void hide(Node node) {
		node.setVisible(false);
		node.setManaged(false);
	}

	private static void run(Runnable handler) {
		if (handler != null) {
			handler.run();
		}
	}
}
