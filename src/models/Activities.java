package models;

import java.util.ArrayList;
import java.util.Random;

public class Activities {
// ability to use a roster and perform activities with present students
	ArrayList<Student> present;
	
	
	public Activities(){
		
	}
//	public Activities(ArrayList<Student> present){
//		present = new 
//	}
	
	public Student getRandomStudentName(ArrayList<Student> present) {
		Random rand = new Random();
		int selectedStudent = rand.nextInt(present.size());
		return present.get(selectedStudent);	
	}
}
