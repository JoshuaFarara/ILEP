package views;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Roster;
import models.Student;

/**
 * Roster Manager screen: a list of rosters, a detail panel for the focused
 * roster, and a "Take Attendance" button. It only displays what it is given
 * and reports user actions; RosterManagerController does the rest.
 */
public class RosterManagerView extends BorderPane {
	final int LIST_VIEW_WIDTH = 700;
	final int LIST_VIEW_HEIGHT = 350;

	private final ListView<Roster> rosterListView;
	private final VBox rosterDetailView;
	private final Button takeAttendance = new Button("Take Attendance");
	private final Label selectedInfo = new Label();

	private Consumer<List<Roster>> onSelectionChanged;
	private Consumer<Roster> onRosterFocused;
	private Runnable onTakeAttendance;

	public RosterManagerView() {
		rosterListView = buildRosterListView();
		rosterDetailView = buildEmptyDetailPanel();

		setCenter(new ScrollPane(rosterListView));
		setBottom(buildButtonBar());
		setRight(rosterDetailView);
	}

	// ------------------------------------------------------------ controller API
	public void setRosters(List<Roster> rosters) {
		rosterListView.setItems(FXCollections.observableArrayList(rosters));
	}

	public void setSelectionSummary(int rosterCount, int studentCount) {
		selectedInfo.setText(rosterCount + " roster(s) selected, " + studentCount + " students");
	}

	public void setTakeAttendanceEnabled(boolean enabled) {
		takeAttendance.setDisable(!enabled);
	}

	public void showRosterDetail(Roster roster) {
		rosterDetailView.getChildren().clear();
		if (roster == null) {
			rosterDetailView.getChildren().add(whiteLabel("Select a roster to view details"));
			return;
		}
		ListView<Student> lvStudents = new ListView<>(FXCollections.observableArrayList(roster.getStudents()));
		rosterDetailView.getChildren().addAll(
				whiteLabel("CRN: " + roster.getCrn()),
				whiteLabel("Course: " + roster.getCourseName()),
				whiteLabel("Section: " + roster.getCourseSection()),
				whiteLabel("Semester: " + roster.getSemesterTerm() + " " + roster.getYear()),
				whiteLabel("Students: " + roster.getStudents().size()),
				new ScrollPane(lvStudents));
	}

	/** Called with every selected roster whenever the selection changes. */
	public void setOnSelectionChanged(Consumer<List<Roster>> handler) {
		this.onSelectionChanged = handler;
	}

	/** Called with the most recently clicked roster (for the detail panel). */
	public void setOnRosterFocused(Consumer<Roster> handler) {
		this.onRosterFocused = handler;
	}

	public void setOnTakeAttendance(Runnable handler) {
		this.onTakeAttendance = handler;
	}

	// ------------------------------------------------------------ layout
	private ListView<Roster> buildRosterListView() {
		ListView<Roster> lv = new ListView<>();
		lv.setPrefSize(LIST_VIEW_WIDTH, LIST_VIEW_HEIGHT);
		// Ctrl/Shift + click to select several rosters for one combined roll call
		lv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		lv.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null && onRosterFocused != null) {
				onRosterFocused.accept(newVal);
			}
		});
		lv.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Roster>) c -> {
			if (onSelectionChanged != null) {
				onSelectionChanged.accept(new ArrayList<>(lv.getSelectionModel().getSelectedItems()));
			}
		});
		return lv;
	}

	private HBox buildButtonBar() {
		HBox bar = new HBox(10, takeAttendance, selectedInfo);
		bar.setPadding(new Insets(8));
		bar.setAlignment(Pos.CENTER_LEFT);
		takeAttendance.setDisable(true);
		takeAttendance.setOnAction(e -> {
			if (onTakeAttendance != null) {
				onTakeAttendance.run();
			}
		});
		return bar;
	}

	// shown before any roster is selected
	private VBox buildEmptyDetailPanel() {
		VBox box = new VBox();
		box.setPrefHeight(LIST_VIEW_HEIGHT);
		box.setPadding(new Insets(15, 12, 15, 12));
		box.setStyle("-fx-background-color: #336699;");
		box.getChildren().add(whiteLabel("Select a roster to view details"));
		return box;
	}

	private static Label whiteLabel(String text) {
		Label label = new Label(text);
		label.setStyle("-fx-text-fill:white");
		return label;
	}
}
