package tests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import models.Activities;
import models.Student;

public class ActivitiesTest {

	public static void main(String[] args) {
		Activities activities = new Activities(new Random(42));
		List<Student> students = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			Student s = new Student();
			s.setFirstName("Student" + i);
			s.setLastName("Test");
			students.add(s);
		}

		// pickRandom
		check(activities.pickRandom(new ArrayList<>()) == null, "pickRandom on empty list returns null");
		check(students.contains(activities.pickRandom(students)), "pickRandom returns a student from the list");
		Student last = students.get(0);
		boolean repeated = false;
		for (int i = 0; i < 100; i++) {
			Student next = activities.pickRandom(students, last);
			repeated |= next == last;
			last = next;
		}
		check(!repeated, "pickRandom(list, exclude) never repeats the excluded student");

		// shuffle
		List<Student> shuffled = activities.shuffle(students);
		check(shuffled.size() == students.size() && new HashSet<>(shuffled).equals(new HashSet<>(students)),
				"shuffle keeps every student exactly once");
		check(students.get(0).getFirstName().equals("Student1"), "shuffle does not change the original list");

		// randomGroups
		List<List<Student>> groups = activities.randomGroups(students, 3);
		check(groups.size() == 4, "10 students in groups of 3 -> 4 groups");
		int total = 0;
		for (List<Student> g : groups) {
			check(g.size() == 2 || g.size() == 3, "group size is 2 or 3: " + g);
			total += g.size();
		}
		check(total == 10, "every student is in a group");
		check(activities.randomGroups(new ArrayList<>(), 3).isEmpty(), "no students -> no groups");
	}

	private static void check(boolean ok, String message) {
		System.out.println((ok ? "PASS " : "FAIL ") + message);
	}
}
