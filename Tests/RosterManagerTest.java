package tests;

import models.RosterManager;

public class RosterManagerTest {

    public static void main(String[] args) {
        System.out.println("RosterManager Test");
        RosterManager rosterManager = new RosterManager();
        // RosterManager.readFilesInFolder();
        // String selectedRoster = rosterManager.selectRosterFromFolder();
        // RosterManager.readFile(selectedRoster); 
        // rosterManager.populateRosterFromFile(selectedRoster);
        // System.out.println("Number of rosters: " + RosterManager.getNumberOfRosters());
        System.out.println(rosterManager.toString());
    }
}
