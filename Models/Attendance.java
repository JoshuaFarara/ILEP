package models;

import java.util.ArrayList;
import java.util.Set;

public class Attendance {
    Student[] students;
    RosterManager rosterManager;
    ArrayList<Student> present;
    ArrayList<Student> absent;
    Roster roster;

    Attendance(){
         
    }
    
    Attendance(Roster roster){
        setRoster(roster);
    }

    public void setRoster(Roster roster) {
        this.roster = roster;
    }
    // read students from roster
    public void readStudentsInRoster() {
        System.out.println("The Students in this roster are: ");
        for (Student s : students) {
            System.out.printf(" %s %s (Eagle ID: %s)%n", 
                s.getFirstName(), s.getLastName(), s.getEagleID());
        }
    
    
    /* does the attendance class need to be an object? */
    // yes it should be an object because we need to store the attendance records for each roster, and we can have multiple rosters with different attendance records.


    //choose a roster
            //can select a roster by course name, course code, semester term, year, or crn

    //choose multiple rosters
    // take attendance for selected roster(s)
    // include the date of attendance
    // mark students as present or absent
        //user input: 0 for absent and 1 for present
        //present and absent arraylists to store students based on attendance status
    // store attendance records
        // present file and absent file
        // filename format: CRN-semesterTerm year-SUBJECTnumberSECTION-date-present.csv
    // boolean for isPresent, 
    // method to calculate attendance percentage for each student
    }

    public void populateAttendance(Roster roster) {
       System.out.println("=== Attendace Management System ===\n");
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
        
        // Example 3: Get a specific roster and display its students
        if (RosterManager.getNumberOfRosters() > 0) {
            System.out.println("\n=== Students in First Roster ===");
            Roster firstRoster = manager.getRosters().get(0);
            System.out.println("Roster Info: " + firstRoster);
            firstRoster.readStudentsInRoster();
        }

    }
    

    

}
