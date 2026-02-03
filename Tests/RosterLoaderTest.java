package tests;

import models.RosterLoader;
import models.RosterManager;

public class RosterLoaderTest {
    public static void main(String[] args) {
        RosterLoader rosterLoader = new RosterLoader();
        RosterManager rosterManager = new RosterManager();
        rosterLoader.loadAll(rosterManager);

        // System.out.println("Number of rosters loaded: " + RosterManager.getNumberOfRosters());
        System.out.println(rosterManager.toString());
        
        // rosterLoader.getRosters();
        // rosterLoader.selectRosterFromFolder();


        // rosterBuilder.getFileFromDataFolder();
        // System.out.println(rosterBuilder.getRoster().toString());
    }
}
