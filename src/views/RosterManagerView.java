package views;

import java.awt.Scrollbar;
import java.util.List;
import java.util.Scanner;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import models.RosterLoader;
import models.RosterManager;
import models.Roster;
import models.Student;

public class RosterManagerView extends BorderPane {
	final int LIST_VIEW_WIDTH = 700;
	final int LIST_VIEW_HEIGHT = 350;

	private Label title = new Label("Roster Manager");
	private Button selectRoster = new Button("Select");
	private Button loadRoster = new Button("Load Roster");
	private RosterManager rm;
	private RosterLoader rl;
	private ListView<Roster> rosterListView;
	private VBox rosterDetailView;
	Pane selectedRostersPane = new Pane();

	public RosterManagerView(RosterLoader rl, RosterManager rm) {
		this.rl = rl;
		this.rm = rm;
		rl.loadAll(rm);

		rosterListView = buildRosterListVeiw();
//		HBox rlvButtons = showCenterPaneButtons(rm.getRoster());
		rosterDetailView = buildEmptyDetailPanel();
//		studentsInAllRosters = 

//		selectRoster.setText("Select a Roster");
		getChildren().addAll(selectRoster, loadRoster, new ScrollPane(rosterListView));

		setCenter(new ScrollPane(rosterListView));
//		setBottom(rlvButtons); 
		setRight(rosterDetailView);

	}

	private ListView<Roster> buildRosterListVeiw() {

		ListView<Roster> lv = new ListView<>(FXCollections.observableArrayList(rm.getRosters()));
		lv.setPrefSize(LIST_VIEW_WIDTH, LIST_VIEW_HEIGHT);
		lv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		lv.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				showRosterDetail(newVal);
			}
		});
		return lv;
	}
	private HBox showCenterPaneButtons(Roster roster) {
		HBox listViewButtons = new HBox();
		Label studentsInAllRosters = new Label();
//		studentsInAllRosters.setText(String.valueOf(roster.getNumStudentsAdded()));
		
		Button lvRollCall = new Button("Roll Call"); 
		listViewButtons.getChildren().addAll(studentsInAllRosters,lvRollCall);
		return listViewButtons;
	}

	// shown before any roster is selected
	private VBox buildEmptyDetailPanel() {
		VBox box = new VBox();
		box.setPrefHeight(LIST_VIEW_HEIGHT);
//		Insets(double top, double right,double bottom,double left); //the formal parameters
		box.setPadding(new Insets(15, 12, 15, 12));
		
		box.setStyle("-fx-background-color: #336699;");
		Label selectRoster = new Label("Select a roster to view details");
		selectRoster.setStyle("-fx-text-fill:white");
		box.getChildren().add(selectRoster);
		return box;
	}
	private HBox showButtonsOnSelection() {
		HBox hbox = new HBox();
//		hbox.setPrefHeight(LIST_VIEW_HEIGHT);
//		hbox.setPadding(new Insets(100,12 , 100, 12));
//		rdButtons
		rosterDetailView.getChildren().addAll(loadRoster);
		return hbox;
	};
	
	private void showRosterDetail(Roster roster) {
		rosterDetailView.getChildren().clear();
		Label crn = new Label("CRN: "      + roster.getCrn());
		crn.setStyle("-fx-text-fill:white");
		Label course = new Label("Course: "   + roster.getCourseName());
		course.setStyle("-fx-text-fill:white");
		Label section = new Label("Section: "  + roster.getCourseSection());
		section.setStyle("-fx-text-fill:white");
		Label semester = new Label("Semester: " + roster.getSemesterTerm() + " " + roster.getYear());
		semester.setStyle("-fx-text-fill:white");
		Label studentSize = new Label("Students: " + roster.getStudents().size());
		studentSize.setStyle("-fx-text-fill:white");
	
		ListView<Student> lvStudents = new ListView<>(FXCollections.observableArrayList(roster.getStudents()));
		ScrollPane viewStudentsInRoster = new ScrollPane(lvStudents);
		Group rosterDetailGroup= new Group();
		rosterDetailGroup.getStyleClass().add("rd-label-group");
		rosterDetailGroup.getChildren().addAll(crn,course,section,semester,studentSize,loadRoster,viewStudentsInRoster
				);
	
		rosterDetailView.getChildren().addAll(
	           crn,course,section,semester,studentSize, viewStudentsInRoster,loadRoster	           
//	           rosterDetailGroup        
	        );
//		HBox loadRosterButtonsPane = showButtonsOnSelection();
	}

	private void onRosterSelected(Roster roster) {
		System.out.printf("Selected: %s%s%n", roster.getCourseCode(), roster.getCourseSection()); // swap for real UI later

//	public static void loadSelectedRoster(RosterManager manager) {
//		System.out.println("\n=== Loading Single Roster ===");
//		try {
//			// String testFilename = "83129-Fall 2025-CSCI1302A.csv"; // replace with choose
//			// a roster filename method later
//			Roster selectedRoster = chooseRoster(manager);
//			System.out.println("For today's attendance, you have selected the following roster:"); // replace today with
//																									// date method later
//			System.out.println(selectedRoster);
//
//			System.out.println("CRN: " + selectedRoster.getCrn());
//			System.out.println("Semester: " + selectedRoster.getSemesterTerm() + " " + selectedRoster.getYear());
//			System.out.println("Course: " + selectedRoster.getCourseName() + selectedRoster.getCourseCode());
//			System.out.println("Section: " + selectedRoster.getCourseSection());
//			Text selectedRosterText = new Text(selectedRoster.getCrn()
//					+ selectedRoster.getSemesterTerm()
//					+ selectedRoster.getYear()
//					+ selectedRoster.getCourseName()
//					+ selectedRoster.getCourseName()
//					+ selectedRoster.getCourseCode()
//					+ selectedRoster.getCourseSection());
//
		//// return selectedRosterText ;
//		} catch (Exception e) {
//			System.err.println("Error loading single roster: " + e.getMessage());
		//// return null;
//		}

	}

	public static Roster chooseRoster(RosterManager manager) {
		try (Scanner input = new Scanner(System.in)) {
			System.out.println("Select a roster: ");
			for (int i = 0; i < manager.getRosters().size(); i++) {
				System.out.println(i + ": " + manager.getRosters().get(i).getRosterPathString());
			}
			int chosenRosterIndex = input.nextInt();
			switch (chosenRosterIndex) {
			case 0 -> manager.getRosters().get(0);
			case 1 -> manager.getRosters().get(1);
			default -> {
				System.out.println("Invalid index. Please select a valid roster index.");
			}
			}
			return manager.getRosters().get(chosenRosterIndex);
		}
	}
}
