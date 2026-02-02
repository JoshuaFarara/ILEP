package tests;


import models.RosterManager;

public class RosterManagerTest {
    public static void main(String[] args) {
            RosterManager rosterManager = new RosterManager();
            RosterManager.readFilesInFolder();
            String selectedRoster = rosterManager.selectRosterFromFolder();
            RosterManager.readFile(selectedRoster);
    }        
}

