package models;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/** Finds image files in a folder. Has no JavaFX code, so it can be used and tested on its own. */
public class ImageLibrary {

	private final File folder;

	public ImageLibrary(File folder) {
		this.folder = folder;
	}

	/** URLs (file:/...) of every .jpg/.jpeg/.png/.gif file in the folder. Empty if the folder is missing. */
	public List<String> getImageUrls() {
		List<String> urls = new ArrayList<>();

		if (folder == null || !folder.isDirectory()) {
			System.err.println("Images folder not found: " + (folder == null ? "null" : folder.getAbsolutePath()));
			return urls;
		}

		File[] files = folder.listFiles();
		if (files == null) {
			System.err.println("Could not read folder contents: " + folder.getAbsolutePath());
			return urls;
		}

		for (File file : files) {
			if (isImageFile(file)) {
				urls.add(file.toURI().toString());
			}
		}
		System.out.println("Found " + urls.size() + " images.");
		return urls;
	}

	/**
	 * Up to {@code maximumCount} different images, chosen at random. Choosing a few instead of
	 * loading every image keeps startup fast however many images the folder holds (issue #17).
	 */
	public List<String> pickRandomImageUrls(int maximumCount, Random random) {
		List<String> shuffledUrls = new ArrayList<>(getImageUrls());
		Collections.shuffle(shuffledUrls, random);
		return new ArrayList<>(shuffledUrls.subList(0, Math.min(Math.max(maximumCount, 0), shuffledUrls.size())));
	}

	/** One image chosen at random, or empty if the folder has no images. */
	public Optional<String> pickRandomImageUrl(Random random) {
		return pickRandomImageUrls(1, random).stream().findFirst();
	}

	private static boolean isImageFile(File file) {
		if (!file.isFile()) {
			return false;
		}
		String name = file.getName().toLowerCase();
		return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif");
	}
}
