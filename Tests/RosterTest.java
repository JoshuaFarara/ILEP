package tests;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import models.Roster;
import models.RosterBuilder;


public class RosterTest {
    public static void main(String[] args) {
        ArrayList<Roster> rosters = new ArrayList<Roster>();
        Roster roster = new Roster();
        Scanner input = new Scanner(System.in);
        /* features for a GUI
        * THese features would be used in a GUI to select course name, code, and section
        System.out.println("Select a course name: ");
        */
    //    System.out.println("Select a course name: ");
    //     roster.setCourseName("CSCI");
        System.out.println("Select a course code: 0--> 1301, 1--> 1302");
        int courseCodeNumber = input.nextInt(); //scanner input
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

        System.out.println("Select a course section: 0--> A, 1--> B, 2--> C, 3--> D, 4--> H");
        int courseSectionNumber = input.nextInt(); //scanner input
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
            default:
                break;
        }

        roster.getRosterPathString();
        rosters.add(roster);
        
        
}
}
