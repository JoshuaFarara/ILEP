package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class RosterLoader {

    Scanner input = new Scanner(System.in);
    private static int numberOfRostersBuilt = 0;
    final static File DATA_FOLDER_PATH = new File("C:\\Users\\jfarara\\Documents\\Github\\ILEP\\Data");


    // private final RosterManager manager;
    public RosterLoader() {
    }

    public void loadAll(RosterManager manager) {
        File folder = DATA_FOLDER_PATH;
        for (File file : folder.listFiles()) {
            Roster roster = populateRosterFromFile(file.getName());
            manager.addRoster(roster);
        }
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
    public Roster populateRosterFromFile(String fileName) {
        Roster roster = new Roster();
        for (File file : DATA_FOLDER_PATH.listFiles()) {
            if (file.getName().equals(fileName)){
                int studentCount = 0; 
                try (Scanner input = new Scanner(file)) {
                    while (input.hasNextLine()) {
                        String name = input.nextLine().trim();
                        Student student = new Student(name);
                        roster.addStudentsToRoster(student);
                        studentCount++;
                        System.out.println(studentCount + " " + student.getName());
                        // namesList.add(name);
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("File not found: " + fileName);
                    // e.printStackTrace();
                }
            }
        }
        // RosterManager.addRoster(roster);
        return roster;
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
            case 0 ->
                roster.setCourseCode("1301");
            case 1 ->
                roster.setCourseCode("1302");
            default -> {
            }
        }

        System.out.println(
                "Select a course section: 0--> A, 1--> B, 2--> C, 3--> D, 4--> H, 5--> AH, 6--> AHCD, 7--> AHB");
        int courseSectionNumber = input.nextInt(); // scanner input
        switch (courseSectionNumber) {
            case 0 ->
                roster.setCourseSection("A");
            case 1 ->
                roster.setCourseSection("B");
            case 2 ->
                roster.setCourseSection("C");
            case 3 ->
                roster.setCourseSection("D");
            case 4 ->
                roster.setCourseSection("H");
            case 5 ->
                roster.setCourseSection("AH");
            case 6 ->
                roster.setCourseSection("AHCD");
            case 7 ->
                roster.setCourseSection("AHB");
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
