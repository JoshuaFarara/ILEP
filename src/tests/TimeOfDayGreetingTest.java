package tests;

import java.time.LocalTime;

import models.TimeOfDayGreeting;

/** Checks the Home screen greeting at the edges of each part of the day. Run as a Java application. */
public class TimeOfDayGreetingTest {

	public static void main(String[] args) {
		check(TimeOfDayGreeting.greetingFor(LocalTime.of(5, 0)).equals("Good morning"), "5:00 is morning");
		check(TimeOfDayGreeting.greetingFor(LocalTime.of(11, 59)).equals("Good morning"), "11:59 is morning");
		check(TimeOfDayGreeting.greetingFor(LocalTime.of(12, 0)).equals("Good afternoon"), "12:00 is afternoon");
		check(TimeOfDayGreeting.greetingFor(LocalTime.of(16, 59)).equals("Good afternoon"), "16:59 is afternoon");
		check(TimeOfDayGreeting.greetingFor(LocalTime.of(17, 0)).equals("Good evening"), "17:00 is evening");
		check(TimeOfDayGreeting.greetingFor(LocalTime.of(23, 30)).equals("Good evening"), "23:30 is evening");
		check(TimeOfDayGreeting.greetingFor(LocalTime.of(0, 0)).equals("Good evening"), "midnight is evening");
		check(TimeOfDayGreeting.greetingFor(LocalTime.of(4, 59)).equals("Good evening"), "4:59 is still evening");
	}

	private static void check(boolean ok, String message) {
		System.out.println((ok ? "PASS " : "FAIL ") + message);
	}
}
