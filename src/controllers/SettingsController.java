package controllers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import controllers.AppState.Screen;
import javafx.scene.Parent;
import javafx.scene.text.Font;
import models.ColorContrast;
import models.DisplaySettings;
import views.SettingsView;

/**
 * Runs the Settings screen (issues #14 and #15). Every change is applied to the whole window at
 * once (through {@code applyDisplaySettings}) and saved, so it is still there next time ILEP starts.
 */
public class SettingsController {

	private final AppState state;
	private final SettingsView view = new SettingsView();
	private final Consumer<DisplaySettings> applyDisplaySettings;
	private final DisplaySettings settings;

	public SettingsController(AppState state, Consumer<DisplaySettings> applyDisplaySettings) {
		this(state, applyDisplaySettings, DisplaySettings.DEFAULT_SETTINGS_FILE);
	}

	/** The settings file can be redirected, e.g. to a temporary folder for testing. */
	public SettingsController(AppState state, Consumer<DisplaySettings> applyDisplaySettings, Path settingsFile) {
		this.state = state;
		this.applyDisplaySettings = applyDisplaySettings;
		this.settings = loadSettings(settingsFile);

		view.setOnDarkModeChanged(darkMode -> change(() -> settings.setDarkMode(darkMode)));
		view.setOnIncreaseTextSize(() -> change(settings::increaseTextSize));
		view.setOnDecreaseTextSize(() -> change(settings::decreaseTextSize));
		view.setOnResetTextSize(() -> change(() -> settings.setTextSize(DisplaySettings.DEFAULT_TEXT_SIZE)));
		view.setOnFontChanged(fontFamily -> change(() -> settings.setFontFamily(fontFamily)));
		view.setOnTextColorChanged(textColor -> change(() -> settings.setTextColorHex(textColor)));
		view.setOnResetTextColor(() -> change(settings::resetTextColor));

		view.setOnGoHome(() -> state.navigateTo(Screen.HOME));
		view.setOnGoRosterManager(() -> state.navigateTo(Screen.ROSTER_MANAGER));
		view.setOnGoAttendance(() -> state.navigateTo(Screen.ATTENDANCE));
		view.setOnGoClassSession(() -> state.navigateTo(Screen.CLASS_SESSION));

		// Apply the saved settings as soon as ILEP starts
		applyDisplaySettings.accept(settings);
		refreshView();
	}

	public Parent getView() {
		return view;
	}

	/** The curated fonts that are actually installed on this computer, in the curated order. */
	public static List<String> availableFonts() {
		List<String> installedFamilies = new ArrayList<>();
		for (String family : Font.getFamilies()) {
			installedFamilies.add(family.toLowerCase(Locale.ROOT));
		}
		List<String> available = new ArrayList<>();
		for (String family : DisplaySettings.CURATED_FONT_FAMILIES) {
			if (family.equals(DisplaySettings.DEFAULT_FONT_FAMILY) || installedFamilies.contains(family.toLowerCase(Locale.ROOT))) {
				available.add(family);
			}
		}
		return available;
	}

	// ------------------------------------------------------------ helpers
	/** Applies one change to the whole window, shows it, and saves it. */
	private void change(Runnable update) {
		update.run();
		applyDisplaySettings.accept(settings);
		refreshView();
		try {
			settings.save();
			view.showSaveStatus("Saved. These settings are used every time ILEP starts.", false);
		} catch (IOException exception) {
			view.showSaveStatus("Applied, but could not be saved: " + exception.getMessage(), true);
		}
	}

	private void refreshView() {
		view.showSettings(settings, availableFonts());
		view.showContrastWarning(contrastWarning());
	}

	/** A warning if the custom text color is hard to read on the current page background, else null. */
	private String contrastWarning() {
		String textColor = settings.getTextColorHex();
		if (textColor == null) {
			return null;
		}
		String pageBackground = settings.isDarkMode() ? ColorContrast.DARK_PAGE_BACKGROUND : ColorContrast.LIGHT_PAGE_BACKGROUND;
		double ratio = ColorContrast.contrastRatio(textColor, pageBackground);
		if (ratio >= ColorContrast.MINIMUM_READABLE_RATIO) {
			return null;
		}
		return String.format("Hard to read: contrast is %.1f to 1 on the %s background (at least %.1f to 1 is recommended). "
				+ "Choose a %s color, or use the theme colors.", ratio, settings.isDarkMode() ? "dark" : "light",
				ColorContrast.MINIMUM_READABLE_RATIO, settings.isDarkMode() ? "lighter" : "darker");
	}

	private static DisplaySettings loadSettings(Path settingsFile) {
		try {
			return DisplaySettings.load(settingsFile);
		} catch (IOException | RuntimeException exception) {
			System.err.println("Could not read display settings; using the defaults: " + exception.getMessage());
			return new DisplaySettings(settingsFile);
		}
	}
}
