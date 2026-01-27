package Tests;

import Models.Student;

public class StudentTest {
    public static void main(String[] args) {
        Student s1 = new Student();
        Student s2 = new Student(30, "Joshua Farara");
    
        System.out.println(s1.toString());
        System.out.println(s2.toString());

    }
}