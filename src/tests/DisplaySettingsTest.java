package tests;

import java.nio.file.Files;
import java.nio.file.Path;

import models.DisplaySettings;

/** Checks the Settings values and how they are saved (issue #15). Run as a Java application. */
public class DisplaySettingsTest {

	public static void main(String[] args) throws Exception {
		Path folder = Files.createTempDirectory("display-settings-test");
		Path settingsFile = folder.resolve("appData").resolve("ilep-settings.properties");

		DisplaySettings defaults = DisplaySettings.load(settingsFile);
		check(!defaults.isDarkMode(), "dark mode is off by default");
		check(defaults.getTextSize() == 13, "text size defaults to 13");
		check(defaults.getFontFamily().equals("System"), "font defaults to System");
		check(defaults.getTextColorHex() == null, "text color defaults to the theme's colors");

		DisplaySettings settings = new DisplaySettings(settingsFile);
		settings.setTextSize(27);
		settings.increaseTextSize();
		settings.increaseTextSize();
		check(settings.getTextSize() == 28, "text size stops at 28");
		settings.setTextSize(11);
		settings.decreaseTextSize();
		settings.decreaseTextSize();
		check(settings.getTextSize() == 10, "text size stops at 10");
		settings.setTextSize(500);
		check(settings.getTextSize() == 28, "an out-of-range size is limited to 28");

		settings.setFontFamily("Wingdings");
		check(settings.getFontFamily().equals("System"), "a font outside the curated list falls back to System");
		settings.setFontFamily("Georgia");
		check(settings.getFontFamily().equals("Georgia"), "a curated font is accepted");

		settings.setTextColorHex("navy");
		check(settings.getTextColorHex() == null, "a color that isn't #rrggbb is ignored");
		settings.setTextColorHex("#1F4E8C");
		check("#1f4e8c".equals(settings.getTextColorHex()), "a #rrggbb color is accepted (stored in lowercase)");

		settings.setDarkMode(true);
		settings.setTextSize(18);
		settings.save();
		check(Files.exists(settingsFile), "saving creates the settings file and its folder");

		DisplaySettings reloaded = DisplaySettings.load(settingsFile);
		check(reloaded.isDarkMode() && reloaded.getTextSize() == 18 && reloaded.getFontFamily().equals("Georgia")
				&& "#1f4e8c".equals(reloaded.getTextColorHex()), "every setting survives a restart");

		reloaded.resetTextColor();
		reloaded.save();
		check(DisplaySettings.load(settingsFile).getTextColorHex() == null, "resetting the text color is saved");

		Files.writeString(settingsFile, "darkMode=maybe\ntextSize=huge\nfontFamily=\ntextColor=#zzz\n");
		DisplaySettings damaged = DisplaySettings.load(settingsFile);
		check(!damaged.isDarkMode() && damaged.getTextSize() == 13 && damaged.getFontFamily().equals("System")
				&& damaged.getTextColorHex() == null, "a damaged settings file falls back to the defaults");

		try (var files = Files.walk(folder)) {
			files.sorted(java.util.Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
		}
	}

	private static void check(boolean ok, String message) {
		System.out.println((ok ? "PASS " : "FAIL ") + message);
	}
}
