package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Quick classroom activities performed on a list of (present) students.
 * Never changes the list it is given; methods return new lists instead.
 */
public class Activities {

	private final Random random;

	public Activities() {
		this(new Random());
	}

	/** Pass a seeded Random to get repeatable results in tests. */
	public Activities(Random random) {
		this.random = random;
	}

	/** Kept for older callers; same as {@link #pickRandom(List)}. */
	public Student getRandomStudentName(ArrayList<Student> present) {
		return pickRandom(present);
	}

	/** A random student, or null if the list is empty. */
	public Student pickRandom(List<Student> students) {
		if (students == null || students.isEmpty()) {
			return null;
		}
		return students.get(random.nextInt(students.size()));
	}

	/**
	 * A random student other than {@code exclude}, so the same student is not
	 * picked twice in a row. Falls back to any student if only one is left.
	 */
	public Student pickRandom(List<Student> students, Student exclude) {
		if (students == null || students.size() < 2 || exclude == null || !students.contains(exclude)) {
			return pickRandom(students);
		}
		List<Student> others = new ArrayList<>(students);
		others.remove(exclude);
		return pickRandom(others);
	}

	/** A shuffled copy of the list. */
	public List<Student> shuffle(List<Student> students) {
		List<Student> copy = new ArrayList<>(students);
		Collections.shuffle(copy, random);
		return copy;
	}

	/**
	 * Splits the students into random groups of at most {@code groupSize}.
	 * Group sizes differ by at most one, e.g. 10 students in groups of 3 gives
	 * groups of 3, 3, 2, 2.
	 */
	public List<List<Student>> randomGroups(List<Student> students, int groupSize) {
		if (groupSize < 1) {
			throw new IllegalArgumentException("Group size must be at least 1, got " + groupSize);
		}
		List<Student> shuffled = shuffle(students);
		int groupCount = (shuffled.size() + groupSize - 1) / groupSize;

		List<List<Student>> groups = new ArrayList<>();
		for (int i = 0; i < groupCount; i++) {
			groups.add(new ArrayList<>());
		}
		for (int i = 0; i < shuffled.size(); i++) {
			groups.get(i % groupCount).add(shuffled.get(i)); // deal like cards
		}
		return groups;
	}
}
