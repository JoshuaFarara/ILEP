package models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class RosterLoader {

    Scanner input = new Scanner(System.in);
    private static int numberOfRostersBuilt = 0;
    final static File DATA_FOLDER_PATH = new File("C:\\Users\\jfarara\\Documents\\Github\\ILEP\\Data");
    // final static File DATA_FOLDER_PATH = new File("H:\\git\\ILEP\\data");


    // private final RosterManager manager;
    public RosterLoader() {
    }

    /**
     * Load all CSV files from the data folder and add them to the RosterManager
     */
    public void loadAll(RosterManager manager) {
        File folder = DATA_FOLDER_PATH;
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Data folder not found: " + DATA_FOLDER_PATH.getAbsolutePath());
            return;
        }

        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".csv")) {
                try {
                    Roster roster = populateRosterFromFile(file.getName());
                    manager.addRoster(roster);
                    numberOfRostersBuilt++;
                } catch (Exception e) {
                    System.err.println("Skipping invalid file: " + file.getName() + " - " + e.getMessage());
                }
            }
        }
        System.out.println("Loaded " + numberOfRostersBuilt + " rosters successfully.");
    }

    public static int getNumberOfRostersBuilt() {
        return numberOfRostersBuilt;
    }

    // read files from the Data folder
    public void readFilesInFolder() { 
        int numberofFiles = DATA_FOLDER_PATH.listFiles().length;
        System.out.println("--------------------------------Reading files from folder: " + DATA_FOLDER_PATH.getName());
        System.out.println("Files found: " + numberofFiles);
        File[] listOfFiles = DATA_FOLDER_PATH.listFiles();
        
        for (int i = 0; i < numberofFiles; i++) {
            System.out.println("File " + (i + 1) + ": " + listOfFiles[i].getName());
        }
    }

    // retrieves files from the Data folder and returns the file name as a string
    public String getFileFromDataFolder(File DATA_FOLDER_PATH) {
        String fileName = null;
        for (File file : DATA_FOLDER_PATH.listFiles()) {
            if (file.getName().equals(selectRosterFromFolder())){
                fileName =  file.getName();
            }
            else {
                System.out.println("File not found: " + fileName);
            }
        }
        return fileName;
    }

    // allows the user to select a roster from the Data folder and returns the file name as a string
    public String selectRosterFromFolder() {
        int numberofFiles = DATA_FOLDER_PATH.listFiles().length;
        System.out.println("--------------------------------Select a roster from below:------------------------------------------------------------------");
        for (int i = 0; i < numberofFiles; i++) {
            System.out.printf("Choose %d--> %s%n", i, DATA_FOLDER_PATH.listFiles()[i].getName());
        }
        // Roster roster = new Roster();
        int selectedRoster = input.nextInt(); // scanner input
        String rosterPath = DATA_FOLDER_PATH.listFiles()[selectedRoster].getName();
        
        System.out.println("Roster selected: " + rosterPath);
        return rosterPath;
    }

    // reads the file selected by the user and populates a roster with the names of the students in the roster
    /**
     * Populate a Roster from a CSV file
     * Expected filename format: CRN-semesterTerm year-SUBJECTnumberSECTION.csv
     * Example: 83129-Fall 2025-CSCI1302A.csv
     */
    public Roster populateRosterFromFile(String fileName) {
        // Create roster and parse filename for course info
        Roster roster = new Roster(fileName);
        
        // Find the file in the data folder
        File file = new File(DATA_FOLDER_PATH, fileName);
        
        if (!file.exists()) {
            System.err.println("File not found: " + file.getAbsolutePath());
            return roster;
        }

        // Read CSV file and create Student objects
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;
            int studentCount = 0;
            
            while ((line = br.readLine()) != null) {
                // Skip header row
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                Student student = parseStudentFromCSVLine(line);
                if (student != null) {
                    roster.addStudentToRoster(student);
                    studentCount++;
                }
            }
            
            System.out.println("Loaded " + studentCount + " students from " + fileName);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + fileName);
            e.printStackTrace();
        }
        
        return roster;
    }

    /**
     * Parse a single CSV line into a Student object
     * Expected CSV format:
     * Last Name, First Name, Middle Name, Preferred First Name, Email, Eagle ID, Major, Campus, 
     * First Day of Isolation/Quarantine Modification, Last Day of Isolation/Quarantine Modification, 
     * Registered Late, Attendance Status
     */
    private Student parseStudentFromCSVLine(String line) {
        try {
            // Split by comma (handling potential tabs)
            String[] parts = line.split("[,\t]+");
            
            if (parts.length < 8) {
                System.err.println("Skipping invalid line (not enough fields): " + line);
                return null;
            }
            
            Student student = new Student();
            
            // Parse required fields (indices 0-7)
            student.setLastName(parts[0].trim());
            student.setFirstName(parts[1].trim());
            student.setMiddleName(parts[2].trim());
            student.setPreferredFirstName(parts[3].trim());
            student.setEmail(parts[4].trim());
            student.setEagleID(parts[5].trim());
            student.setMajor(parts[6].trim());
            student.setCampus(parts[7].trim());
            
            // Parse optional date fields if present
            if (parts.length > 8 && !isNullOrEmpty(parts[8])) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
                    Date firstDay = sdf.parse(parts[8].trim());
                    student.setFirstDayOfIsolationQuarantineModification(firstDay);
                } catch (Exception e) {
                    // Skip if date parsing fails
                }
            }
            
            if (parts.length > 9 && !isNullOrEmpty(parts[9])) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
                    Date lastDay = sdf.parse(parts[9].trim());
                    student.setLastDayOfIsolationQuarantineModification(lastDay);
                } catch (Exception e) {
                    // Skip if date parsing fails
                }
            }
            
            if (parts.length > 10 && !isNullOrEmpty(parts[10])) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
                    Date regLate = sdf.parse(parts[10].trim());
                    student.setRegisteredLate(regLate);
                } catch (Exception e) {
                    // Skip if date parsing fails
                }
            }
            
            if (parts.length > 11 && !isNullOrEmpty(parts[11])) {
                student.setAttendanceStatus(parts[11].trim().equalsIgnoreCase("Y"));
            }
            
            return student;
            
        } catch (Exception e) {
            System.err.println("Error parsing student from line: " + line);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper method to check if a string is null or empty (including "null" string)
     */
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty() || str.trim().equalsIgnoreCase("null");
    }


    // reads the file selected by the user and prints the names of the students in the roster
    public void readFile(String fileName) {
        for (File file : DATA_FOLDER_PATH.listFiles()) {
            if (file.getName().equals(fileName)){
                int studentCount = 0; 
                try (Scanner input = new Scanner(file)) {
                    while (input.hasNextLine()) {
                        String name = input.nextLine().trim();
                        studentCount++;
                        System.out.println(studentCount + " " + name);
                        // namesList.add(name);
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("File not found: " + fileName);
                    // e.printStackTrace();
                }
            }
        }
    }

    

    public String chooseRoster() {
        Roster roster = new Roster();
        System.out.println("Select a course code: 0--> 1301, 1--> 1302");
        int courseCodeNumber = input.nextInt(); // scanner input
        switch (courseCodeNumber) {
             case 0 -> roster.setCourseCode("1301");
            case 1 -> roster.setCourseCode("1302");
            default -> {
            }
        }

        System.out.println(
                "Select a course section: 0--> A, 1--> B, 2--> C, 3--> D, 4--> H, 5--> AH, 6--> AHCD, 7--> AHB");
        int courseSectionNumber = input.nextInt(); // scanner input
        switch (courseSectionNumber) {
            case 0 -> roster.setCourseSection("A");
            case 1 -> roster.setCourseSection("B");
            case 2 -> roster.setCourseSection("C");
            case 3 -> roster.setCourseSection("D");
            case 4 -> roster.setCourseSection("H");
            case 5 -> roster.setCourseSection("AH");
            case 6 -> roster.setCourseSection("AHCD");
            case 7 -> roster.setCourseSection("AHB");
            default -> {
            }
        }
        System.out.println(roster.toString());
        // manager.addRoster(roster);
        numberOfRostersBuilt++;
        return roster.getRosterPathString();

    }

    public static File buildRosterFile(String fileName) {
        File file = new File(fileName);
        return file;
    }

}
