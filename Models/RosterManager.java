package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class RosterManager {
        ArrayList<Roster> rosters = new ArrayList<>();
        Scanner input = new Scanner(System.in);
        final static File DATA_FOLDER_PATH = new File("C:\\Users\\jfarara\\Documents\\Github\\ILEP\\Data");
        private static int numberOfRosters = 0;


    public RosterManager() {

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

    public static void readFilesInFolder() {
        int numberofFiles = DATA_FOLDER_PATH.listFiles().length;
        System.out.println("--------------------------------Reading files from folder: " + DATA_FOLDER_PATH.getName());
        System.out.println("Files found: " + numberofFiles);
        File[] listOfFiles = DATA_FOLDER_PATH.listFiles();
        
        for (int i = 0; i < numberofFiles; i++) {
            System.out.println("File " + (i + 1) + ": " + listOfFiles[i].getName());
        }
    }
    
    // read folders from the Data folder
    public String getFileFromDataFolder(File DATA_FOLDER_PATH) {
        String fileName = null;
        // File foldername = new File("C:\\Users\\jfarara\\Documents\\Github\\ILEP\\Data");
        // fileIO.readFolder("Data")
        for (File file : DATA_FOLDER_PATH.listFiles()) {
        // for (File file : foldername.listFiles()) {
            if (file.getName().equals(selectRosterFromFolder())){
            // if (file.getName() == selectRosterFromFolder()){
                fileName =  file.getName();
            }
            else {
                System.out.println("File not found: " + fileName);
            }
        }
        return fileName;
    }

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
    
    public static void readFile(String fileName) {
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
}

