package tests;
import java.util.Scanner;
import models.Roster;
import models.Student;


public class RosterTest {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String[] studentsNames = {"LastName, FirstName",
                                "Coller, John",
                                "Frazier, Jameson",
                                "Franz, Carl",
                                "Andrews, Phillip",
                                "Jacobs, Maxuel"
        };
        //    Create a roster giving the courseName, code, section
        Roster roster = new Roster();
        roster.setCourseName("CSCI");
        roster.setCourseCode("1301");
        roster.setCourseSection("A");
        roster.setStudents(studentsNames);
        System.out.println(roster.toString());
        roster.readStudentsInRoster();
        
        // roster

    }
}
