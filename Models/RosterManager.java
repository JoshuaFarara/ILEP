package models;

import java.util.ArrayList;
import java.util.Scanner;

public class RosterManager {
        ArrayList<Roster> rosters;
        Scanner input = new Scanner(System.in);
        private static int numberOfRosters = 0;


    public RosterManager() {
        rosters = new ArrayList<>();
        }

    public void addRoster(Roster roster) {
        rosters.add(roster);
        numberOfRosters++;
    }
    public void removeRoster(Roster roster) {
        rosters.remove(roster);
        numberOfRosters--;
    }
    public static int getNumberOfRosters() {
        return numberOfRosters;
    }
    public ArrayList<Roster> getRosters() {
        return rosters;
    }
    public void setRosters(ArrayList<Roster> rosters) {
        this.rosters = rosters;
    }

    public Roster getRoster() {
        Scanner input = new Scanner(System.in);
        // scanner input
        for (Roster roster : rosters) {
            System.out.println(rosters.indexOf(roster) + ": " + roster.getRosterPathString());
        }
        int index = input.nextInt();
        for (Roster roster : rosters) {
            if (rosters.indexOf(roster) == index) {
                return roster;
            }
        }
       
        return null;
    }
    public Roster getRosterClaude() {
        Scanner input = new Scanner(System.in);
        // scanner input
        for (Roster roster : rosters) {
            System.out.println(rosters.indexOf(roster) + ": " + roster.getRosterPathString());
        }
        int index = input.nextInt();
        for (Roster roster : rosters) {
            if (rosters.indexOf(roster) == index) {
                return roster;
            }
        }
       
        return null;
    }

    public Roster getRoster(String rosterName) {
        for (Roster roster : rosters) {
            if (roster.getRosterPathString().equals(rosterName)) {
                return roster;
            }
        }
        System.out.println("Roster not found: " + rosterName);
        return null;
    }

    @Override
    public String toString() {
        return String.format(
            "RosterManager: rosters=%s"
            + "Number of Rosters=%d",rosters, getNumberOfRosters());
    }

    // public String chooseRoster() {
    //     System.out.println("Select a course code: 0--> 1301, 1--> 1302");
    //     int courseCodeNumber = input.nextInt(); // scanner input
    //     switch (courseCodeNumber) {
    //          case 0 -> roster.setCourseCode("1301");
    //         case 1 -> roster.setCourseCode("1302");
    //         default -> {
    //         }
    //     }
    
    

    
}

