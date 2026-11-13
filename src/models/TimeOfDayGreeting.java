package models;

import java.time.LocalTime;

/** The Home screen greeting for a time of day. No JavaFX code, so it can be tested on its own. */
public final class TimeOfDayGreeting {

	private static final LocalTime MORNING_STARTS = LocalTime.of(5, 0);
	private static final LocalTime AFTERNOON_STARTS = LocalTime.of(12, 0);
	private static final LocalTime EVENING_STARTS = LocalTime.of(17, 0);

	private TimeOfDayGreeting() {
	}

	/** "Good morning" from 5:00, "Good afternoon" from 12:00, "Good evening" from 17:00 until 4:59. */
	public static String greetingFor(LocalTime time) {
		if (!time.isBefore(MORNING_STARTS) && time.isBefore(AFTERNOON_STARTS)) {
			return "Good morning";
		}
		if (!time.isBefore(AFTERNOON_STARTS) && time.isBefore(EVENING_STARTS)) {
			return "Good afternoon";
		}
		return "Good evening";
	}
}
