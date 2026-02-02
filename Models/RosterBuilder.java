package models;

import java.io.File;
import java.util.Scanner;

public class RosterBuilder{
    Scanner input = new Scanner(System.in);
    private static int numberOfRostersBuilt = 0;
    // private final RosterManager manager;

    public RosterBuilder() {
    }
    public static int getNumberOfRostersBuilt() {
        return numberOfRostersBuilt;
    }

    public String createARoster() {
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
