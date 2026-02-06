package models;
// import Data.*;

import java.util.ArrayList;


/**
 * Roster class to hold an array of Students and course information
 *
 */
public class Roster {

    private  ArrayList<Student> students;
    private String rosterPathString;
    private String courseName;
    private String courseCode;
    private String courseSection;

    public Roster() {
        students = new ArrayList<Student>(); // ability to stor dynamic number of students
        courseName = "CSCI";
        courseCode = null;
        courseSection = "n";
        System.out.println("Roster created for course: " + courseName
                + "\nyou will need to select the courseCode: " + courseCode
                + "\nyou will need to select a section: " + courseSection);
    }

    public Roster(String filename) {
        parseRosterFilename(filename);
    }

    public Roster(String courseName, String courseCode, String courseSection) {
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.courseSection = courseSection;
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

    public String getRosterPathString() {
        return String.format("%s%s%s", courseName, courseCode, courseSection);
    }

    public void setStudents(Student[] students) {
        this.students = new ArrayList<Student>();
        for (Student student : students) {
            this.students.add(student);
        }
    }

    public void addStudentsToRoster(Student student) {
        this.students.add(student);
    }
           

    public void parseRosterFilename(String filename) {
        // parse the rosterPathString to get courseName, courseCode, courseSection
        String[] parts = filename.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        if (parts.length >= 3) {
            // Roster roster = new Roster();
            setCourseName(parts[0]);
            setCourseCode(parts[1]);
            setCourseSection(parts[2]);
            // return roster;
        }
        // return null;
    }

}
