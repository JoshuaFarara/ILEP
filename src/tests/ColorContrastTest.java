package tests;

import models.ColorContrast;

/** Checks the text-color readability warning used in Settings (issue #15). Run as a Java application. */
public class ColorContrastTest {

	public static void main(String[] args) {
		check(close(ColorContrast.contrastRatio("#000000", "#ffffff"), 21.0), "black on white is 21 to 1");
		check(close(ColorContrast.contrastRatio("#ffffff", "#000000"), 21.0), "the order of the colors doesn't matter");
		check(close(ColorContrast.contrastRatio("#1f4e8c", "#1f4e8c"), 1.0), "a color on itself is 1 to 1");
		// Published WCAG example: #767676 on white is the lightest gray that passes AA (about 4.54)
		check(ColorContrast.isReadable("#767676", "#ffffff"), "#767676 on white is readable");
		check(!ColorContrast.isReadable("#777777", "#ffffff"), "#777777 on white is just too light");

		check(ColorContrast.isReadable("#000000", ColorContrast.LIGHT_PAGE_BACKGROUND), "black text is readable in light mode");
		check(!ColorContrast.isReadable("#000000", ColorContrast.DARK_PAGE_BACKGROUND), "black text is flagged in dark mode");
		check(ColorContrast.isReadable("#ffffff", ColorContrast.DARK_PAGE_BACKGROUND), "white text is readable in dark mode");
		check(!ColorContrast.isReadable("#ffff00", ColorContrast.LIGHT_PAGE_BACKGROUND), "yellow text is flagged in light mode");

		boolean rejected;
		try {
			ColorContrast.contrastRatio("blue", "#ffffff");
			rejected = false;
		} catch (IllegalArgumentException expected) {
			rejected = true;
		}
		check(rejected, "a color that isn't #rrggbb is rejected");
	}

	private static boolean close(double actual, double expected) {
		return Math.abs(actual - expected) < 0.01;
	}

	private static void check(boolean ok, String message) {
		System.out.println((ok ? "PASS " : "FAIL ") + message);
	}
}
