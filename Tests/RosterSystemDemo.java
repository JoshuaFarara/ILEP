package tests;

import java.util.Scanner;
import models.Roster;
import models.RosterLoader;
import models.RosterManager;

/**
 * Demo class to test the updated RosterLoader and RosterManager
 */
public class RosterSystemDemo {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.println("=== Roster Management System Demo ===\n");

        // Create a RosterLoader
        RosterLoader loader = new RosterLoader();
        // Create a RosterManager
        RosterManager manager = new RosterManager();

        // Example 1: Load all rosters from the data folder
        System.out.println("Loading all rosters from data folder...");
        loader.loadAll(manager);

        System.out.println("\n=== Rosters Loaded ===");
        System.out.println("Total rosters in manager: " + manager.getNumberOfRosters());

        // Example 2: Display all rosters
        // public void displayRosters() { 
        System.out.println("\n=== All Rosters ===");
        for (Roster roster : manager.getRosters()) {
            System.out.println(roster.toString());
        }

        // Example 3: Get a specific roster and display its students
        // if (RosterManager.getNumberOfRosters() > 0) {
        //     System.out.println("\n=== Select a Roster to View Students ===");
        //     Roster firstRoster = chooseRoster(manager);
        //     System.out.println("Roster Info: " + firstRoster);
        //     firstRoster.readStudentsInRoster();
        // }

        // // Example 4: Load a single selected roster manually
        loadSelectedRoster(manager);
    }

    public static void loadSelectedRoster(RosterManager manager){
                System.out.println("\n=== Loading Single Roster ===");
        try {
            // String testFilename = "83129-Fall 2025-CSCI1302A.csv"; // replace with choose a roster filename method later
            Roster selectedRoster = chooseRoster(manager);
            System.out.println("For today's attendance, you have selected the following roster:"); //replace today with date method later
            System.out.println(selectedRoster);
            
            System.out.println("CRN: " + selectedRoster.getCrn());
            System.out.println("Semester: " + selectedRoster.getSemesterTerm() + " " + selectedRoster.getYear());
            System.out.println("Course: " + selectedRoster.getCourseName() + selectedRoster.getCourseCode());
            System.out.println("Section: " + selectedRoster.getCourseSection());
        } catch (Exception e) {
            System.err.println("Error loading single roster: " + e.getMessage());
        }

    }

    public static Roster chooseRoster(RosterManager manager) {
        Scanner input = new Scanner(System.in);

        System.out.println("Select a roster: ");
        for (int i = 0; i < manager.getRosters().size(); i++) {
            System.out.println(i + ": " + manager.getRosters().get(i).getRosterPathString());
        }
        int chosenRosterIndex = input.nextInt();
        switch (chosenRosterIndex) {
            case 0 ->
                manager.getRosters().get(0);
            case 1 ->
                manager.getRosters().get(1);
            default -> {
                System.out.println("Invalid index. Please select a valid roster index.");
            }
        }
        return manager.getRosters().get(chosenRosterIndex);
    }

}

