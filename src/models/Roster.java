package models;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// import Student;



/**
 * Roster class to hold an array of Students and course information
 *
 */
public class Roster {

    private ArrayList<Student> students;
    private String crn;
    private String semesterTerm;
    private String year;
    private String courseName;
    private String courseCode;
    private String courseSection;
//    private static int numStudentsAdded = 0;

    // Pattern to match the filename format: CRN-semesterTerm year-SUBJECTnumberSECTION.csv
    private static final Pattern FILENAME_PATTERN = 
        Pattern.compile("^(\\d+)-(\\w+)\\s+(\\d{4})-([A-Z]+)(\\d+)([A-Z])(\\.csv)?$");

    public Roster() {
        students = new ArrayList<>(); // ability to store dynamic number of students
        courseName = "CSCI";
        courseCode = null;
        courseSection = null;
    }
        

    public Roster(String filename) {
        students = new ArrayList<>();
        parseRosterFilename(filename);
    }

    public Roster(String courseName, String courseCode, String courseSection, ArrayList<Student> students) {
        this.students = new ArrayList<Student>(); 
        setCourseName(courseName);
        setCourseCode(courseCode);
        setCourseSection(courseSection);
 
    }

    /**
     * Parse the roster filename to extract course information
     * Format: CRN-semesterTerm year-SUBJECTnumberSECTION.csv
     * Example: 83129-Fall 2025-CSCI1302A.csv
     */
    public void parseRosterFilename(String filename) {
        Matcher matcher = FILENAME_PATTERN.matcher(filename);
        
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                """
                Invalid filename format. Expected: CRN-semesterTerm year-SUBJECTnumberSECTION.csv
                Example: 83129-Fall 2025-CSCI1302A.csv
                Got: """ + filename
            );
        }
        
        this.crn = matcher.group(1);
        this.semesterTerm = matcher.group(2);
        this.year = matcher.group(3);
        this.courseName = matcher.group(4);
        this.courseCode = matcher.group(5);
        this.courseSection = matcher.group(6);
    }

    // Getters and Setters
    public String getCrn() {
        return crn;
    }

    public String getSemesterTerm() {
        return semesterTerm;
    }

    public String getYear() {
        return year;
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

    public ArrayList<Student> getStudents() {
        return students;
    }

//    public int getNumStudentsAdded() {
//        return numStudentsAdded;
//    }

     @Override
    public String toString() {
        return String.format("%s%s\t%s%s%s\tStudents: %d", 
            semesterTerm, year, courseName, courseCode, courseSection, students.size());
    }

    public String getRosterPathString() {
        return String.format("%s%s%s", courseName, courseCode, courseSection);
    }

    // Add a single student and increment counter
    public void addStudentToRoster(Student student) {
        this.students.add(student);
//        numStudentsAdded++;
    }

    public void readStudentsInRoster() {
        System.out.println("The Students in this roster are: ");
        for (Student s : students) {
            System.out.printf(" %s %s (Eagle ID: %s)%n", 
                s.getFirstName(), s.getLastName(), s.getEagleID());
        }
    }
    

}
