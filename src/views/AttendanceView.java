package views;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import models.Attendance;
import models.Roster;
import models.RosterManager;
import models.Student;

/**
 * Roll call screen. Shows every student from the chosen roster(s) in one list;
 * checking a student marks them present. Saving writes present.txt/absent.txt
 * through {@link Attendance#saveAttendance()}.
 *
 * Two ways in:
 *  - RosterManagerView "Take Attendance" -> {@link #startRollCall(List)}
 *  - Attendance menu button with nothing chosen -> pick rosters here first
 */
public class AttendanceView extends BorderPane {

	final int LIST_VIEW_WIDTH = 700;
	final int LIST_VIEW_HEIGHT = 350;
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy");

	private final RosterManager rm;
	private Attendance att;

	// Checkbox state for each student in the current roll call
	private final Map<Student, BooleanProperty> presentProps = new HashMap<>();
	private final Label countsLabel = new Label();

	public AttendanceView(RosterManager rm) {
		this.rm = rm;
		setPadding(new Insets(10));
		showRosterPicker();
	}

	// ---------------------------------------------------------------- step 1
	/** Lets the user choose one or more rosters when none were passed in. */
	public void showRosterPicker() {
		att = null;
		presentProps.clear();

		Label title = heading("Take Attendance");
		Label hint = new Label("Select one or more rosters (Ctrl/Shift + click) to combine into one roll call.");

		ListView<Roster> picker = new ListView<>(FXCollections.observableArrayList(rm.getRosters()));
		picker.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		picker.setPrefSize(LIST_VIEW_WIDTH, LIST_VIEW_HEIGHT);

		Button start = new Button("Start Roll Call");
		start.setDisable(true);
		picker.getSelectionModel().getSelectedItems()
				.addListener((ListChangeListener<Roster>) c ->
						start.setDisable(picker.getSelectionModel().getSelectedItems().isEmpty()));
		start.setOnAction(e -> startRollCall(new ArrayList<>(picker.getSelectionModel().getSelectedItems())));

		setTop(new VBox(4, title, hint));
		setCenter(picker);
		setBottom(buttonBar(start));
	}

	// ---------------------------------------------------------------- step 2
	/** Builds a single roll call for all students in the given rosters. */
	public void startRollCall(List<Roster> rosters) {
		if (rosters == null || rosters.isEmpty()) {
			showRosterPicker();
			return;
		}
		att = new Attendance(rosters);
		presentProps.clear();

		// Header: date + which rosters are in this roll call
		Label title = heading("Attendance - " + att.getDate().format(DATE_FORMAT));
		StringBuilder names = new StringBuilder();
		for (Roster r : att.getRosters()) {
			if (names.length() > 0) names.append(", ");
			names.append(rosterCode(r));
		}
		Label rosterLabel = new Label("Rosters: " + names);
		setTop(new VBox(4, title, rosterLabel));

		// Student list with a "present" checkbox per student
		for (Student s : att.getStudents()) {
			BooleanProperty prop = new SimpleBooleanProperty(att.isPresent(s));
			prop.addListener((obs, was, isNow) -> {
				att.mark(s, isNow);
				updateCounts();
			});
			presentProps.put(s, prop);
		}
		ListView<Student> studentList = new ListView<>(FXCollections.observableArrayList(att.getStudents()));
		studentList.setPrefSize(LIST_VIEW_WIDTH, LIST_VIEW_HEIGHT);
		studentList.setCellFactory(CheckBoxListCell.forListView(presentProps::get, new StringConverter<Student>() {
			@Override
			public String toString(Student s) {
				String label = s.getLastName() + ", " + s.getFirstName();
				if (att.getRosters().size() > 1) {
					label += "   (" + rosterCode(att.getRosterFor(s)) + ")";
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
		allPresent.setOnAction(e -> setAll(true));
		Button clearAll = new Button("Clear All");
		clearAll.setOnAction(e -> setAll(false));
		Button save = new Button("Save Attendance");
		save.setOnAction(e -> save());
		Button changeRosters = new Button("Change Rosters");
		changeRosters.setOnAction(e -> showRosterPicker());

		updateCounts();
		setBottom(buttonBar(allPresent, clearAll, save, changeRosters, countsLabel));
	}

	// ---------------------------------------------------------------- step 3
	private void save() {
		try {
			att.saveAttendance();
			Alert done = new Alert(Alert.AlertType.INFORMATION);
			done.setTitle("Attendance Saved");
			done.setHeaderText("Attendance saved for " + att.getDate().format(DATE_FORMAT));
			done.setContentText(String.format("Present: %d%nAbsent: %d",
					att.getPresentStudents().size(), att.getAbsentStudents().size()));
			done.showAndWait();
		} catch (IOException ex) {
			Alert err = new Alert(Alert.AlertType.ERROR);
			err.setTitle("Save Failed");
			err.setHeaderText("Could not write attendance files");
			err.setContentText(ex.getMessage());
			err.showAndWait();
		}
	}

	// ---------------------------------------------------------------- helpers
	private void setAll(boolean present) {
		for (BooleanProperty p : presentProps.values()) {
			p.set(present); // listener updates the Attendance model + counts
		}
	}

	private void updateCounts() {
		if (att == null) return;
		countsLabel.setText(String.format("Present: %d   Absent: %d   Total: %d",
				att.getPresentStudents().size(), att.getAbsentStudents().size(), att.getStudents().size()));
	}

	private static String rosterCode(Roster r) {
		return r.getCourseName() + r.getCourseCode() + r.getCourseSection();
	}

	private static Label heading(String text) {
		Label l = new Label(text);
		l.setFont(Font.font(null, FontWeight.BOLD, 18));
		return l;
	}

	private static HBox buttonBar(javafx.scene.Node... nodes) {
		HBox bar = new HBox(10, nodes);
		bar.setPadding(new Insets(8, 0, 0, 0));
		bar.setAlignment(Pos.CENTER_LEFT);
		return bar;
	}
}
