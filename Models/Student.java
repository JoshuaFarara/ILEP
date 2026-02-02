package models;

public class Student {
    private int age;
    private long ID;
    private String name;
    private int year;
    private String major;

    public Student() {
        ID = 1;
        age = 18;
        name = "First Name " + "Last Name";
        year = 1;
        major = null;
    }

    public Student(int age, String name) {
        ID = 1;
        setAge(age);
        setName(name);
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age){
        if(age > 0){
            this.age = age;
        } else {
            age = 18;
        }
    }

    public long getID() {
        return ID;
    }

    public void setID(long iD) {
        ID = iD;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    @Override
    public String toString() {
        return String.format("The student, %s with ID %d is %d years old, is in year %d, and majors in %s.", name, ID, age, year, major);
    
    
}
}
