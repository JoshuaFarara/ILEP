package models;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Attendance {
	ArrayList<Student> studentList;
	RosterManager rosterManager;
	ArrayList<Student> present;
	ArrayList<Student> absent;
	Roster roster;
	Scanner input = new Scanner(System.in);
	File presentFile = new File("src/appData/present.txt");
	File absentFile = new File("src/appData/absent.txt");

	// --- GUI roll call session state ---
	// One roll call can span several rosters (e.g. combined sections).
	private final ArrayList<Roster> rosters = new ArrayList<>();
	// Student -> roster they were first found in (insertion order kept for display)
	private final LinkedHashMap<Student, Roster> studentRoster = new LinkedHashMap<>();
	// Student -> present (true) / absent (false)
	private final LinkedHashMap<Student, Boolean> marks = new LinkedHashMap<>();
	private LocalDate date = LocalDate.now();


	public Attendance() {

	}

	public Attendance(Roster roster) {
		setRoster(roster);
	}

	public Attendance(ArrayList<Student> studentList) {
		readStudentsInRoster(studentList);
	}

	/** Start one roll call that combines every student in the given rosters. */
	public Attendance(List<Roster> rosters) {
		setRosters(rosters);
	}

	public void setRoster(Roster roster) {
		this.roster = roster;
		setRosters(roster == null ? List.of() : List.of(roster));
	}

	/**
	 * Builds a single combined student list from the given rosters. A student who
	 * appears in more than one selected roster (same Eagle ID) is only listed once.
	 * Every student starts out marked absent.
	 */
	public void setRosters(List<Roster> selected) {
		rosters.clear();
		studentRoster.clear();
		marks.clear();
		ArrayList<String> seenIds = new ArrayList<>();
		for (Roster r : selected) {
			rosters.add(r);
			for (Student s : r.getStudents()) {
				String id = s.getEagleID();
				if (id != null && seenIds.contains(id)) {
					continue;
				}
				if (id != null) {
					seenIds.add(id);
				}
				studentRoster.put(s, r);
				marks.put(s, false);
			}
		}
	}

	public List<Roster> getRosters() {
		return new ArrayList<>(rosters);
	}

	/** All students in this roll call, in roster order. */
	public List<Student> getStudents() {
		return new ArrayList<>(marks.keySet());
	}

	/** The roster a student in this roll call came from. */
	public Roster getRosterFor(Student s) {
		return studentRoster.get(s);
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public void mark(Student s, boolean isPresent) {
		if (marks.containsKey(s)) {
			marks.put(s, isPresent);
		}
	}

	public boolean isPresent(Student s) {
		return marks.getOrDefault(s, false);
	}

	public void markAll(boolean isPresent) {
		for (Map.Entry<Student, Boolean> e : marks.entrySet()) {
			e.setValue(isPresent);
		}
	}

	public List<Student> getPresentStudents() {
		ArrayList<Student> list = new ArrayList<>();
		for (Map.Entry<Student, Boolean> e : marks.entrySet()) {
			if (e.getValue()) list.add(e.getKey());
		}
		return list;
	}

	public List<Student> getAbsentStudents() {
		ArrayList<Student> list = new ArrayList<>();
		for (Map.Entry<Student, Boolean> e : marks.entrySet()) {
			if (!e.getValue()) list.add(e.getKey());
		}
		return list;
	}

	/**
	 * Writes the current roll call to present.txt / absent.txt, one "First Last"
	 * per line (same format as the console takeAttendance). Also updates each
	 * Student's attendanceStatus so other views see the result.
	 */
	public void saveAttendance() throws IOException {
		present = new ArrayList<>(getPresentStudents());
		absent = new ArrayList<>(getAbsentStudents());
		try (PrintWriter presentOutput = new PrintWriter(presentFile);
				PrintWriter absentOutput = new PrintWriter(absentFile)) {
			for (Student s : present) {
				s.setAttendanceStatus(true);
				presentOutput.printf("%s %s%n", s.getFirstName(), s.getLastName());
			}
			for (Student s : absent) {
				s.setAttendanceStatus(false);
				absentOutput.printf("%s %s%n", s.getFirstName(), s.getLastName());
			}
		}
	}

	public ArrayList<Student> takeAttendance(ArrayList<Student> studentList, Scanner input) throws java.io.IOException   {
		PrintWriter presentOutput = new PrintWriter(presentFile);
		PrintWriter absentOutput = new PrintWriter(absentFile);

		present = new ArrayList<>();
		absent = new ArrayList<>();
		
		int attendanceMark = 0;
		int presentCount = 0;
		int absentCount = 0;
		System.out.println("Taking Attendance: ");
		for (Student s : studentList) {
			System.out.printf(" %s %s (Eagle ID: %s)%n", s.getFirstName(), s.getLastName(), s.getEagleID());
			System.out.printf("Absent: 0%nPresent: 1%n");
			attendanceMark = input.nextInt();
//			boolean isPresent = false;
			if (attendanceMark == 1) {
				presentCount++;
				present.add(s);
				presentOutput.printf("%s %s%n",s.getFirstName(), s.getLastName());
			} else if(attendanceMark==0){
//				isPresent = false;
				absentCount++;
				absent.add(s); // this arraylist i smeant for logging, future emails etc
				absentOutput.printf("%s %s%n",s.getFirstName(), s.getLastName());
			}

		}
		readPresentStudents(present);
		presentOutput.close();
		absentOutput.close();
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
		System.out.println("The Present Students in this roster are: ");
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
