package models;

import java.util.Date;

public class Student {
    private String lastName;
    private String firstName;
    private String middleName;
    private String preferredFirstName;
    private String email;
    private String eagleID;
    private String major;
    private String campus;
    private Date firstDayOfIsolationQuarantineModification = null;
    private Date lastDayOfIsolationQuarantineModification = null;
    private Date registeredLate;
    private boolean attendanceStatus = false;



    public Student() {
        lastName = "Last Name";
        firstName = "First Name";
        middleName = "Middle Name";
        preferredFirstName = "Preferred First Name";
        email = "email@domain.com";
        eagleID = "Eagle ID";
        major = "Computer Science";
        major = null;
    }

    

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getPreferredFirstName() {
        return preferredFirstName;
    }

    public void setPreferredFirstName(String preferredFirstName) {
        this.preferredFirstName = preferredFirstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEagleID() {
        return eagleID;
    }

    public void setEagleID(String eagleID) {
        this.eagleID = eagleID;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public Date getFirstDayOfIsolationQuarantineModification() {
        return firstDayOfIsolationQuarantineModification;
    }

    public void setFirstDayOfIsolationQuarantineModification(Date firstDayOfIsolationQuarantineModification) {
        this.firstDayOfIsolationQuarantineModification = firstDayOfIsolationQuarantineModification;
    }

    public Date getLastDayOfIsolationQuarantineModification() {
        return lastDayOfIsolationQuarantineModification;
    }

    public void setLastDayOfIsolationQuarantineModification(Date lastDayOfIsolationQuarantineModification) {
        this.lastDayOfIsolationQuarantineModification = lastDayOfIsolationQuarantineModification;
    }

    public Date getRegisteredLate() {
        return registeredLate;
    }

    public void setRegisteredLate(Date registeredLate) {
        this.registeredLate = registeredLate;
    }

    public boolean isAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(boolean attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

     @Override
    public String toString() {
        return String.format("The student, %s %s with ID %s majoring in  %s.", firstName, lastName, eagleID, major);
}
}
