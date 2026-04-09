package views;

import java.util.Scanner;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import models.RosterLoader;
import models.RosterManager;
import models.Roster;
import models.Student;

public class RosterManagerView extends Pane {
	private Label title = new Label("Roster Manager");
	private Button selectRoster = new Button("Select");
	private Button loadRoster = new Button("Load Roster");
	private RosterManager rm = new RosterManager();
	private RosterLoader rl = new RosterLoader();
	Pane selectedRostersPane = new Pane();

	public RosterManagerView() {
		selectRoster.setText("Select a Roster");
		getChildren().add(selectRoster);
		getChildren().add(loadRoster);
		getChildren().add(new ScrollPane(createRosterListView()));
	
	}

	private ListView<Roster> createRosterListView() {
		rl.loadAll(rm);
		ListView<Roster> lv = new ListView<>(FXCollections.observableArrayList(rm.getRosters()));

		lv.setPrefSize(805, 400);
		lv.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		lv.getSelectionModel().selectedItemProperty().addListener(
				ov -> {
//					for (int i : lv.getSelectionModel().getSelectedIndices()) {
//						new Text(rm.getRosters().toString());
//                    }
		});
		return lv;
	}

	public static void loadSelectedRoster(RosterManager manager) {
		System.out.println("\n=== Loading Single Roster ===");
		try {
			// String testFilename = "83129-Fall 2025-CSCI1302A.csv"; // replace with choose
			// a roster filename method later
			Roster selectedRoster = chooseRoster(manager);
			System.out.println("For today's attendance, you have selected the following roster:"); // replace today with
																									// date method later
			System.out.println(selectedRoster);

			System.out.println("CRN: " + selectedRoster.getCrn());
			System.out.println("Semester: " + selectedRoster.getSemesterTerm() + " " + selectedRoster.getYear());
			System.out.println("Course: " + selectedRoster.getCourseName() + selectedRoster.getCourseCode());
			System.out.println("Section: " + selectedRoster.getCourseSection());
			Text selectedRosterText = new Text(selectedRoster.getCrn()
					+ selectedRoster.getSemesterTerm()
					+ selectedRoster.getYear()
					+ selectedRoster.getCourseName()
					+ selectedRoster.getCourseName()
					+ selectedRoster.getCourseCode()
					+ selectedRoster.getCourseSection());

//			return selectedRosterText ;
		} catch (Exception e) {
			System.err.println("Error loading single roster: " + e.getMessage());
//			return null;
		}

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
