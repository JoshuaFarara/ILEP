package models;
// import Data.*;
import models.Student;

import java.util.ArrayList;


/**
 * Roster class to hold an array of Students and course information
 *
 */
public class Roster {

    private ArrayList<Student> students;
    Student student;
    private String rosterPathString;
    private String courseName;
    private String courseCode;
    private String courseSection;
    private static int numStudentsAdded;

    public Roster() {
        students = new ArrayList<Student>(); // ability to stor dynamic number of students
        courseName = "CSCI";
        courseCode = null;
        courseSection = null;
        // System.out.println("Roster created for course: " + courseName
        //         + "\nyou will need to select the courseCode: " + courseCode
        //         + "\nyou will need to select a section: " + courseSection);
    }

    public Roster(String filename) {
        parseRosterFilename(filename);
    }

    public Roster(String courseName, String courseCode, String courseSection, Students[] students) {
        setCourseName(courseName);
        setCourseCode(courseCode);
        setCourseSection(courseSection);
        setStudents(students);
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

    public ArrayList<Student> getStudents(){
        return students;
    }
    public int getNumStudentsAdded(){
        return numStudentsAdded;
    }

    @Override
    public String toString() {
        return String.format("The roster created is: %s%s%s, with %d students", courseName, courseCode, courseSection, getNumStudentsAdded());
    }

    public String getRosterPathString() {
        return String.format("%s%s%s", courseName, courseCode, courseSection);
    }

    public void setStudents(String[] studentArr) {
        for (String s : studentArr) {
            student = new Student(s);
            this.students.add(student);
            numStudentsAdded++;
        }
    }

    public void addStudentToRoster(Student student) {
        this.students.add(student);
    }
    
    public void readStudentsInRoster(){
        System.out.println("The Students in this roster are: ");
        for(Student s : students )
            System.out.printf(" %s%n",s.getName());
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
