package views;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import models.Roster;
import models.Student;

/**
 * Roll call screen. Has two modes, chosen by AttendanceController:
 *  - {@link #showRosterPicker(List)}: choose one or more rosters
 *  - {@link #showRollCall}: one checkbox per student (checked = present)
 *
 * The view never changes the Attendance model. It reports what the user does
 * through the setOn... handlers, and the controller updates the model.
 */
public class AttendanceView extends BorderPane {

	final int LIST_VIEW_WIDTH = 700;
	final int LIST_VIEW_HEIGHT = 350;
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy");

	// Checkbox state for each student in the current roll call
	private final Map<Student, BooleanProperty> presentProps = new HashMap<>();
	private final Label countsLabel = new Label();
	// True while the controller is setting checkboxes, so those changes are not reported back
	private boolean updatingMarks = false;

	private Consumer<List<Roster>> onStartRollCall;
	private BiConsumer<Student, Boolean> onMarkChanged;
	private Consumer<Boolean> onMarkAll;
	private Runnable onSave;
	private Runnable onChangeRosters;
	private Runnable onStartClassSession;

	public AttendanceView() {
		setPadding(new Insets(10));
	}

	// ---------------------------------------------------------------- step 1
	/** Lets the user choose one or more of the given rosters. */
	public void showRosterPicker(List<Roster> rosters) {
		presentProps.clear();

		Label title = heading("Take Attendance");
		Label hint = new Label("Select one or more rosters (Ctrl/Shift + click) to combine into one roll call.");

		ListView<Roster> picker = new ListView<>(FXCollections.observableArrayList(rosters));
		picker.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		picker.setPrefSize(LIST_VIEW_WIDTH, LIST_VIEW_HEIGHT);

		Button start = new Button("Start Roll Call");
		start.setDisable(true);
		picker.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Roster>) c ->
				start.setDisable(picker.getSelectionModel().getSelectedItems().isEmpty()));
		start.setOnAction(e -> {
			if (onStartRollCall != null) {
				onStartRollCall.accept(new ArrayList<>(picker.getSelectionModel().getSelectedItems()));
			}
		});

		setTop(new VBox(4, title, hint));
		setCenter(picker);
		setBottom(buttonBar(start));
	}

	// ---------------------------------------------------------------- step 2
	/**
	 * Shows a roll call.
	 *
	 * @param date      date shown in the heading
	 * @param rosters   rosters in this roll call
	 * @param students  students to list, in display order
	 * @param isPresent initial checkbox state per student
	 * @param rosterOf  which roster each student came from (shown when there are several rosters)
	 */
	public void showRollCall(LocalDate date, List<Roster> rosters, List<Student> students,
			Predicate<Student> isPresent, Function<Student, Roster> rosterOf) {
		presentProps.clear();

		// Header: date + which rosters are in this roll call
		Label title = heading("Attendance - " + date.format(DATE_FORMAT));
		StringBuilder names = new StringBuilder();
		for (Roster r : rosters) {
			if (names.length() > 0) names.append(", ");
			names.append(rosterCode(r));
		}
		setTop(new VBox(4, title, new Label("Rosters: " + names)));

		// Student list with a "present" checkbox per student
		for (Student s : students) {
			BooleanProperty prop = new SimpleBooleanProperty(isPresent.test(s));
			prop.addListener((obs, was, isNow) -> {
				if (!updatingMarks && onMarkChanged != null) {
					onMarkChanged.accept(s, isNow);
				}
			});
			presentProps.put(s, prop);
		}
		boolean showRoster = rosters.size() > 1;
		ListView<Student> studentList = new ListView<>(FXCollections.observableArrayList(students));
		studentList.setPrefSize(LIST_VIEW_WIDTH, LIST_VIEW_HEIGHT);
		studentList.setCellFactory(CheckBoxListCell.forListView(presentProps::get, new StringConverter<Student>() {
			@Override
			public String toString(Student s) {
				String label = s.getLastName() + ", " + s.getFirstName();
				if (showRoster) {
					label += "   (" + rosterCode(rosterOf.apply(s)) + ")";
				}
				return label;
			}

			@Override
			public Student fromString(String string) {
				return null; // not editable as text
			}
		}));
		setCenter(studentList);

		// Actions
		Button allPresent = new Button("Mark All Present");
		allPresent.setOnAction(e -> fireMarkAll(true));
		Button clearAll = new Button("Clear All");
		clearAll.setOnAction(e -> fireMarkAll(false));
		Button save = new Button("Save Attendance");
		save.setOnAction(e -> run(onSave));
		Button changeRosters = new Button("Change Rosters");
		changeRosters.setOnAction(e -> run(onChangeRosters));
		Button startSession = new Button("Start Class Session");
		startSession.setOnAction(e -> run(onStartClassSession));

		setBottom(new VBox(4, buttonBar(allPresent, clearAll, save, changeRosters, startSession), countsLabel));
	}

	/** Updates every checkbox to match the model, without reporting the changes back. */
	public void setMarks(Predicate<Student> isPresent) {
		updatingMarks = true;
		try {
			for (Map.Entry<Student, BooleanProperty> e : presentProps.entrySet()) {
				e.getValue().set(isPresent.test(e.getKey()));
			}
		} finally {
			updatingMarks = false;
		}
	}

	public void setCounts(int present, int absent, int total) {
		countsLabel.setText(String.format("Present: %d   Absent: %d   Total: %d", present, absent, total));
	}

	// ---------------------------------------------------------------- step 3
	public void showSaveSuccess(LocalDate date, int present, int absent) {
		Alert done = new Alert(Alert.AlertType.INFORMATION);
		done.setTitle("Attendance Saved");
		done.setHeaderText("Attendance saved for " + date.format(DATE_FORMAT));
		done.setContentText(String.format("Present: %d%nAbsent: %d", present, absent));
		done.showAndWait();
	}

	public void showSaveError(String message) {
		Alert err = new Alert(Alert.AlertType.ERROR);
		err.setTitle("Save Failed");
		err.setHeaderText("Could not write attendance files");
		err.setContentText(message);
		err.showAndWait();
	}

	// ---------------------------------------------------------------- handlers
	public void setOnStartRollCall(Consumer<List<Roster>> handler) { onStartRollCall = handler; }
	public void setOnMarkChanged(BiConsumer<Student, Boolean> handler) { onMarkChanged = handler; }
	public void setOnMarkAll(Consumer<Boolean> handler) { onMarkAll = handler; }
	public void setOnSave(Runnable handler) { onSave = handler; }
	public void setOnChangeRosters(Runnable handler) { onChangeRosters = handler; }
	public void setOnStartClassSession(Runnable handler) { onStartClassSession = handler; }

	// ---------------------------------------------------------------- helpers
	private void fireMarkAll(boolean present) {
		if (onMarkAll != null) {
			onMarkAll.accept(present);
		}
	}

	private static void run(Runnable handler) {
		if (handler != null) {
			handler.run();
		}
	}

	private static String rosterCode(Roster r) {
		return r == null ? "?" : r.getCourseName() + r.getCourseCode() + r.getCourseSection();
	}

	private static Label heading(String text) {
		Label l = new Label(text);
		l.setFont(Font.font(null, FontWeight.BOLD, 18));
		return l;
	}

	private static HBox buttonBar(Node... nodes) {
		HBox bar = new HBox(10, nodes);
		bar.setPadding(new Insets(8, 0, 0, 0));
		bar.setAlignment(Pos.CENTER_LEFT);
		return bar;
	}
}
