package models;

import java.util.ArrayList;
import java.util.Scanner;

public class Attendance {
	ArrayList<Student> studentList;
	RosterManager rosterManager;
	ArrayList<Student> present;
	ArrayList<Student> absent;
	Roster roster;
	Scanner input = new Scanner(System.in);

	public Attendance() {

	}

	public Attendance(Roster roster) {
		setRoster(roster);
	}

	public Attendance(ArrayList<Student> studentList) {
		readStudentsInRoster(studentList);
	}

	public void setRoster(Roster roster) {
		this.roster = roster;
	}

	public ArrayList<Student> takeAttendance(ArrayList<Student> studentList, Scanner input) {
		present = new ArrayList<>();
		absent = new ArrayList<>();
		
		int attendanceMark = 0;
		int presentCount = 0;
		int absentCount = 0;
		System.out.println("Taking Attendance: ");
		for (Student s : studentList) {
			System.out.printf(" %s %s (Eagle ID: %s)%n", s.getFirstName(), s.getLastName(), s.getEagleID());
			System.out.print("Absent: 0\nPresent: 1");
			attendanceMark = input.nextInt();
//			boolean isPresent = false;
			if (attendanceMark == 1) {
				presentCount++;
				present.add(s);
			} else if(attendanceMark==0){
//				isPresent = false;
				absentCount++;
				absent.add(s); // this arraylist i smeant for logging, future emails etc
			}

		}
		readPresentStudents(present);
		return present;
	}
	
	

	// read students from roster
	public void readStudentsInRoster(ArrayList<Student> studentList) {
		System.out.println("The Students in this roster are: ");
		for (Student s : studentList) {
			System.out.printf(" %s %s (Eagle ID: %s)%n", s.getFirstName(), s.getLastName(), s.getEagleID());
		}
	}
	
	

	// Old implementations where Attendance was performing roster loading and
	// management.
	public void readPresentStudents(ArrayList<Student> present) {
		System.out.println("The Students in this roster are: ");
		for (Student s : present) {
			System.out.printf(" %s %s%n", s.getFirstName(), s.getLastName());
		}
	}
}

/* does the attendance class need to be an object? */
// yes it should be an object because we need to store the attendance records
// for each roster, and we can have multiple rosters with different attendance
// records.

// choose a roster
// can select a roster by course name, course code, semester term, year, or crn

// choose multiple rosters
// take attendance for selected roster(s)
// include the date of attendance
// mark students as present or absent
// user input: 0 for absent and 1 for present
// present and absent arraylists to store students based on attendance status
// store attendance records
// present file and absent file
// filename format: CRN-semesterTerm year-SUBJECTnumberSECTION-date-present.csv
// boolean for isPresent,
// method to calculate attendance percentage for each student

//    public void populateAttendance(Roster roster) {
//       System.out.println("=== Attendace Management System ===\n");
//        // Create a RosterManager
//        RosterManager manager = new RosterManager();
//        // Create a RosterLoader
//        RosterLoader loader = new RosterLoader();
//        
//        // Example 1: Load all rosters from the data folder
//        System.out.println("Loading all rosters from data folder...");
//        loader.loadAll(manager);
//        
//        System.out.println("\n=== Rosters Loaded ===");
//        System.out.println("Total rosters in manager: " + RosterManager.getNumberOfRosters());
//        
//        // Example 2: Display all rosters
//        System.out.println("\n=== All Rosters ===");
//        // for (Roster roster : manager.getRosters()) {
//        //     System.out.println(roster);
//        // }
//        
//        // Example 3: Get a specific roster and display its students
//        if (RosterManager.getNumberOfRosters() > 0) {
//            System.out.println("\n=== Students in First Roster ===");
//            Roster firstRoster = manager.getRosters().get(0);
//            System.out.println("Roster Info: " + firstRoster);
//            firstRoster.readStudentsInRoster();
//        }

//    }
//
//}
