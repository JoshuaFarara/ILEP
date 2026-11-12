package models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

/** Helpers for files in src/appData that hold Class I data or secrets. */
final class LocalDataFiles {

	private LocalDataFiles() {
	}

	static void createParentFolder(Path file) throws IOException {
		Path parent = file.toAbsolutePath().getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
	}

	/**
	 * On macOS and Linux, makes the file readable and writable by its owner only. Windows has no
	 * POSIX permissions; there the file relies on the user's profile and disk encryption.
	 */
	static void restrictToOwner(Path file) {
		try {
			Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"));
		} catch (UnsupportedOperationException | IOException exception) {
			// Not supported on this file system (e.g. Windows); nothing more to do.
		}
	}
}
