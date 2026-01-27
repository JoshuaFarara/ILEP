package Models;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import Data.*;

public class RosterBuilder extends Roster {
    ArrayList<Roster> rosters = new ArrayList<Roster>();
    private File filename;
    Scanner input = new Scanner(System.in);

    public RosterBuilder() {
        super();
    }

    public ArrayList<Roster> getRosters() {
        return rosters;
    }

    public void setRosters(ArrayList<Roster> rosters) {
        this.rosters = rosters;
    }

    public File getFilename() {
        return filename;
    }

    public void setFilename(File filename) {
        this.filename = filename;
    }

    // read folders from the Data folder
    public String getFileFromDataFolder() {
        String fileName = null;
        File foldername = new File("C:\\Users\\jfarara\\Desktop\\ILEP\\Data");
        // fileIO.readFolder("Data")
        for (File file : foldername.listFiles()) {
        // for (File file : foldername.listFiles()) {
            if (file.getName() == rosterBuilder()){
                fileName =  file.getName();
            }
            else {
                System.out.println("File not found: " + fileName);
            }
        }
        return fileName;
    }

    // System.out.println("File found: " + folder.getName());
    // }

    // java.io.File file = new java.io.File("scores.txt");

    // // Create a Scanner for the file
    // Scanner input = new Scanner(file);

    // // Read data from a file
    // while (input.hasNext()) {
    // String firstName = input.next();
    // String mi = input.next();
    // String lastName = input.next();
    // int score = input.nextInt();
    // System.out.println(
    // firstName + " " + mi + " " + lastName + " " + score);
    // }
    // File fileIO = new File();

    // read a file from Data folder to build rosters
    // public void buildRostersFromFile(File file) {
    // // Use FileIO class to read the file and build rosters
    // File fileIO = new File();

    // loop through each file and build a roster
    // for(File f:fileIO.readFolder("Data")){
    // // build a roster from the file
    // Roster roster = new Roster();
    // // add the roster to the list
    // rosters.add(roster);
    // }

    public String rosterBuilder() {
        Roster roster = new Roster();
        System.out.println("Select a course code: 0--> 1301, 1--> 1302");
        int courseCodeNumber = input.nextInt(); // scanner input
        switch (courseCodeNumber) {
            case 0:
                roster.setCourseCode("1301");
                break;
            case 1:
                roster.setCourseCode("1302");
                break;
            default:
                break;
        }

        System.out.println(
                "Select a course section: 0--> A, 1--> B, 2--> C, 3--> D, 4--> H, 5--> AH, 6--> AHCD, 7--> AHB");
        int courseSectionNumber = input.nextInt(); // scanner input
        switch (courseSectionNumber) {
            case 0:
                roster.setCourseSection("A");
                break;
            case 1:
                roster.setCourseSection("B");
                break;
            case 2:
                roster.setCourseSection("C");
                break;
            case 3:
                roster.setCourseSection("D");
                break;
            case 4:
                roster.setCourseSection("H");
                break;
            case 5:
                roster.setCourseSection("AH");
                break;
            case 6:
                roster.setCourseSection("AHCD");
                break;
            case 7:
                roster.setCourseSection("AHB");
                break;
            default:
                break;
        }
        System.out.println(roster.toString());
        return roster.getRosterPathString();
        // return roster;
    }

    // select a roster from Data folder by course number and section
    // public void selectRoster(String courseName, int courseNumber, char section) {
    // for(Roster roster : rosters) {
    // if(roster.getCourseName().equals(courseName) &&
    // roster.getCourseCode().equals(String.valueOf(courseNumber)) &&
    // roster.getSection() == section) {
    // System.out.println("Roster selected: " + roster.toString());
    // return;
    // }
    // }
    // System.out.println("Roster not found for: " + courseName + " " + courseNumber
    // + section);
    // }

}
