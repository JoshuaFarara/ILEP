package models;

import java.util.ArrayList;
import java.util.Scanner;

public class RosterManager {
        ArrayList<Roster> rosters;
        // RosterBuilder builder = new RosterBuilder();
        // Roster roster = new Roster();
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
    
    

    
}

