package models;

import java.util.ArrayList;

public class Attendance {
    Student[] students;
    ArrayList<Student> present;
    ArrayList<Student> absent;
    Roster roster;

    Attendance(Roster roster){
        setRosters(roster);
    }

    public Student[] getStudents() {
        return students;
    }
    public void setRosters(Roster roster) {
        this.roster = roster;
        // this.students = roster.getStudents();
    }

    public void populateAttendance(){
    }

    

}
