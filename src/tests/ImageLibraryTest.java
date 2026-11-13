package tests;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import models.ImageLibrary;

/** Checks how ImageLibrary finds and randomly picks images (issue #17). Run as a Java application. */
public class ImageLibraryTest {

	public static void main(String[] args) throws Exception {
		Path folder = Files.createTempDirectory("image-library-test");
		for (int imageNumber = 1; imageNumber <= 8; imageNumber++) {
			Files.createFile(folder.resolve("picture-" + imageNumber + (imageNumber % 2 == 0 ? ".png" : ".jpg")));
		}
		Files.createFile(folder.resolve("notes.txt"));
		Files.createDirectory(folder.resolve("subfolder.jpg"));
		ImageLibrary library = new ImageLibrary(folder.toFile());

		check(library.getImageUrls().size() == 8, "finds the 8 image files and ignores other files and folders");

		List<String> fivePicked = library.pickRandomImageUrls(5, new Random(1));
		check(fivePicked.size() == 5, "picks at most 5 images from 8");
		check(new HashSet<>(fivePicked).size() == 5, "never picks the same image twice");
		check(library.getImageUrls().containsAll(fivePicked), "picks only real images from the folder");

		boolean sawDifferentFirstImage = false;
		String firstPick = library.pickRandomImageUrl(new Random(0)).orElseThrow();
		for (int seed = 1; seed < 20; seed++) {
			sawDifferentFirstImage |= !library.pickRandomImageUrl(new Random(seed)).orElseThrow().equals(firstPick);
		}
		check(sawDifferentFirstImage, "the random background changes between runs");

		check(library.pickRandomImageUrls(20, new Random()).size() == 8, "asking for more than exist returns all of them");
		check(library.pickRandomImageUrls(0, new Random()).isEmpty(), "asking for none returns none");

		Path emptyFolder = Files.createTempDirectory("image-library-empty");
		check(new ImageLibrary(emptyFolder.toFile()).pickRandomImageUrl(new Random()).isEmpty(),
				"an empty folder gives no background image");
		check(new ImageLibrary(new File(folder.toFile(), "missing")).pickRandomImageUrls(5, new Random()).isEmpty(),
				"a missing folder gives no images instead of crashing");

		try (var files = Files.walk(folder)) {
			files.sorted(java.util.Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
		}
		Files.delete(emptyFolder);
	}

	private static void check(boolean ok, String message) {
		System.out.println((ok ? "PASS " : "FAIL ") + message);
	}
}
