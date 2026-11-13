package models;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/**
 * How ILEP looks: dark mode, text size, font and text color (issue #15). Has no JavaFX code, so it
 * can be tested on its own; MainView turns these values into styles.
 *
 * Saved in {@code src/appData/ilep-settings.properties} (git-ignored) and applied at startup.
 */
public class DisplaySettings {

	public static final Path DEFAULT_SETTINGS_FILE = Path.of("src", "appData", "ilep-settings.properties");

	public static final int MINIMUM_TEXT_SIZE = 10;
	public static final int MAXIMUM_TEXT_SIZE = 28;
	/** JavaFX's normal text size, in pixels. */
	public static final int DEFAULT_TEXT_SIZE = 13;

	public static final String DEFAULT_FONT_FAMILY = "System";
	/** Readable fonts offered in Settings; only the ones installed on the computer are shown. */
	public static final List<String> CURATED_FONT_FAMILIES = List.of("System", "Arial", "Verdana", "Tahoma", "Segoe UI",
			"Georgia", "Times New Roman", "Consolas", "Comic Sans MS");

	private final Path settingsFile;
	private boolean darkMode;
	private int textSize = DEFAULT_TEXT_SIZE;
	private String fontFamily = DEFAULT_FONT_FAMILY;
	private String textColorHex; // null means "use the theme's own text colors"

	public DisplaySettings(Path settingsFile) {
		this.settingsFile = settingsFile;
	}

	/** Loads saved settings; anything missing or invalid falls back to the default. */
	public static DisplaySettings load(Path settingsFile) throws IOException {
		DisplaySettings settings = new DisplaySettings(settingsFile);
		if (!Files.exists(settingsFile)) {
			return settings;
		}
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(settingsFile, StandardCharsets.UTF_8)) {
			properties.load(reader);
		}
		settings.setDarkMode(Boolean.parseBoolean(properties.getProperty("darkMode", "false")));
		try {
			settings.setTextSize(Integer.parseInt(properties.getProperty("textSize", "" + DEFAULT_TEXT_SIZE).trim()));
		} catch (NumberFormatException invalidNumber) {
			settings.setTextSize(DEFAULT_TEXT_SIZE);
		}
		settings.setFontFamily(properties.getProperty("fontFamily", DEFAULT_FONT_FAMILY));
		settings.setTextColorHex(properties.getProperty("textColor", ""));
		return settings;
	}

	public void save() throws IOException {
		Properties properties = new Properties();
		properties.setProperty("darkMode", Boolean.toString(darkMode));
		properties.setProperty("textSize", Integer.toString(textSize));
		properties.setProperty("fontFamily", fontFamily);
		properties.setProperty("textColor", textColorHex == null ? "" : textColorHex);
		Path parentFolder = settingsFile.toAbsolutePath().getParent();
		if (parentFolder != null) {
			Files.createDirectories(parentFolder);
		}
		try (Writer writer = Files.newBufferedWriter(settingsFile, StandardCharsets.UTF_8)) {
			properties.store(writer, "ILEP display settings (Settings screen)");
		}
	}

	// ------------------------------------------------------------ dark mode
	public boolean isDarkMode() {
		return darkMode;
	}

	public void setDarkMode(boolean darkMode) {
		this.darkMode = darkMode;
	}

	// ------------------------------------------------------------ text size
	public int getTextSize() {
		return textSize;
	}

	/** Keeps the size between MINIMUM_TEXT_SIZE and MAXIMUM_TEXT_SIZE. */
	public void setTextSize(int textSize) {
		this.textSize = Math.max(MINIMUM_TEXT_SIZE, Math.min(MAXIMUM_TEXT_SIZE, textSize));
	}

	public void increaseTextSize() {
		setTextSize(textSize + 1);
	}

	public void decreaseTextSize() {
		setTextSize(textSize - 1);
	}

	// ------------------------------------------------------------ font
	public String getFontFamily() {
		return fontFamily;
	}

	/** Only fonts from CURATED_FONT_FAMILIES are accepted; anything else becomes the default. */
	public void setFontFamily(String fontFamily) {
		this.fontFamily = CURATED_FONT_FAMILIES.contains(fontFamily) ? fontFamily : DEFAULT_FONT_FAMILY;
	}

	// ------------------------------------------------------------ text color
	/** The custom text color as "#rrggbb", or null when the theme's own colors are used. */
	public String getTextColorHex() {
		return textColorHex;
	}

	/** Accepts "#rrggbb"; anything else (including blank) resets to the theme's own colors. */
	public void setTextColorHex(String textColorHex) {
		this.textColorHex = textColorHex != null && textColorHex.trim().matches("^#[0-9a-fA-F]{6}$")
				? textColorHex.trim().toLowerCase()
				: null;
	}

	public void resetTextColor() {
		textColorHex = null;
	}
}
