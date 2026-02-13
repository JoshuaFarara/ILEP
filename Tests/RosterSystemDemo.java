package tests;

import models.Roster;
import models.RosterLoader;
import models.RosterManager;

/**
 * Demo class to test the updated RosterLoader and RosterManager
 */
public class RosterSystemDemo {
    
    public static void main(String[] args) {
        
        System.out.println("=== Roster Management System Demo ===\n");
        
        // Create a RosterManager
        RosterManager manager = new RosterManager();
        
        // Create a RosterLoader
        RosterLoader loader = new RosterLoader();
        
        // Example 1: Load all rosters from the data folder
        System.out.println("Loading all rosters from data folder...");
        loader.loadAll(manager);
        
        System.out.println("\n=== Rosters Loaded ===");
        System.out.println("Total rosters in manager: " + manager.getNumberOfRosters());
        
        // Example 2: Display all rosters
        System.out.println("\n=== All Rosters ===");
        for (Roster roster : manager.getRosters()) {
            System.out.println(roster);
        }
        
        // // Example 3: Get a specific roster and display its students
        // if (RosterManager.getNumberOfRosters() > 0) {
        //     System.out.println("\n=== Students in First Roster ===");
        //     Roster firstRoster = manager.getRosters().get(0);
        //     System.out.println("Roster Info: " + firstRoster);
        //     firstRoster.readStudentsInRoster();
        // }
        
        // // Example 4: Load a single roster manually
        // System.out.println("\n=== Loading Single Roster ===");
        // try {
        //     String testFilename = "83129-Fall 2025-CSCI1302A.csv";
        //     Roster singleRoster = loader.populateRosterFromFile(testFilename);
        //     System.out.println(singleRoster);
        //     System.out.println("CRN: " + singleRoster.getCrn());
        //     System.out.println("Semester: " + singleRoster.getSemesterTerm() + " " + singleRoster.getYear());
        //     System.out.println("Course: " + singleRoster.getCourseName() + singleRoster.getCourseCode());
        //     System.out.println("Section: " + singleRoster.getCourseSection());
        // } catch (Exception e) {
        //     System.err.println("Error loading single roster: " + e.getMessage());
        // }
    }
}
