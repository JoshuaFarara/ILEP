package Models;
import Models.Student;
import Data.*;
import java.io.File;

/**
 * Roster class to hold an array of Students and course information
 * 
 */

public class Roster {
    private Student[] students;

    private String courseName;
    private String courseCode;
    private String courseSection;

    public Roster(){
        students = new Student[30]; // default size of 30 students
        courseName = "CSCI";
        courseCode = null;
        courseSection = "n";
        System.out.println("Roster created for course: " + courseName
        + "\nyou will need to select the courseCode: " + courseCode
        + "\nyou will need to select a section: " + courseSection);
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseSection() {
        return courseSection;
    }

    public void setCourseSection(String courseSection) {
        this.courseSection = courseSection;
    }

    @Override
    public String toString() {
        return String.format("The roster created is: %s%s%s", courseName, courseCode, courseSection);
    }

    // public void selectRoster(String courseName) {
        
    //     if(courseName.equals(this.courseName)){
    //         System.out.println("Roster selected: " + this.toString());
    //     } else {
    //         System.out.println("Roster not found for course name: " + courseName);
    //     }
    // }

    public String getRosterPathString() {
        return String.format("%s%s%s", courseName, courseCode, courseSection);
    }

    
    
    
}
