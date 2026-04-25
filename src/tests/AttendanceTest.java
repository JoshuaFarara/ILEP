package tests;

import models.RosterManager;
import models.Student;

import java.util.ArrayList;
import java.util.Scanner;

import models.Attendance;
import models.Roster;
import models.RosterLoader;

public class AttendanceTest {

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
         System.out.println("Total rosters in manager: " + RosterManager.getNumberOfRosters());

         // Example 2: Display all rosters
         // public void displayRosters() { 
         System.out.println("\n=== All Rosters ===");
         for (Roster roster : manager.getRosters()) {
//        	 roster.populate
             System.out.println(roster.toString());
         }
    	
    	//Select a roster then access the students
         Roster selectedRoster = manager.getRoster(input);
         ArrayList<Student> studentList = new ArrayList<>(selectedRoster.getStudents());
         
         System.out.println("Attendance Test");
         Attendance attendance = new Attendance(studentList);
         attendance.takeAttendance(studentList, input);
//         attendance.readPresentStudents(studentList);
//         attendance.populateAttendance(selectedRoster);
        // populateAttendance(attendance, selectedRoster);
    }
}
