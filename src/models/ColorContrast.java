package models;

/**
 * Measures how readable one color is on another, using the WCAG 2.1 contrast ratio:
 * 1.0 means identical colors, 21.0 means black on white. WCAG AA asks for at least 4.5 for
 * normal text. Used to warn when a chosen text color is hard to read (issue #15).
 */
public final class ColorContrast {

	/** WCAG AA minimum for normal-size text. */
	public static final double MINIMUM_READABLE_RATIO = 4.5;

	/** The page background in light mode (JavaFX Modena's -fx-background). */
	public static final String LIGHT_PAGE_BACKGROUND = "#f4f4f4";
	/** The page background in dark mode (see views/dark-theme.css). */
	public static final String DARK_PAGE_BACKGROUND = "#2b2b2b";

	private ColorContrast() {
	}

	/** Contrast ratio between two "#rrggbb" colors, from 1.0 to 21.0. */
	public static double contrastRatio(String firstColorHex, String secondColorHex) {
		double firstLuminance = relativeLuminance(firstColorHex);
		double secondLuminance = relativeLuminance(secondColorHex);
		double lighter = Math.max(firstLuminance, secondLuminance);
		double darker = Math.min(firstLuminance, secondLuminance);
		return (lighter + 0.05) / (darker + 0.05);
	}

	public static boolean isReadable(String textColorHex, String backgroundColorHex) {
		return contrastRatio(textColorHex, backgroundColorHex) >= MINIMUM_READABLE_RATIO;
	}

	/** WCAG relative luminance of a "#rrggbb" color, from 0 (black) to 1 (white). */
	static double relativeLuminance(String colorHex) {
		String hex = colorHex.startsWith("#") ? colorHex.substring(1) : colorHex;
		if (!hex.matches("^[0-9a-fA-F]{6}$")) {
			throw new IllegalArgumentException("Expected a color like #1f4e8c, got: " + colorHex);
		}
		double red = channel(Integer.parseInt(hex.substring(0, 2), 16));
		double green = channel(Integer.parseInt(hex.substring(2, 4), 16));
		double blue = channel(Integer.parseInt(hex.substring(4, 6), 16));
		return 0.2126 * red + 0.7152 * green + 0.0722 * blue;
	}

	private static double channel(int value) {
		double fraction = value / 255.0;
		return fraction <= 0.03928 ? fraction / 12.92 : Math.pow((fraction + 0.055) / 1.055, 2.4);
	}
}
