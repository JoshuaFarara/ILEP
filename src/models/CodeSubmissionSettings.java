package models;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Where the Code Submission service is and the instructor's API key.
 *
 * Saved in {@code src/appData/code-submission-settings.properties}, which git ignores. The API key
 * is a password: it stays on this computer and is never committed or shared.
 */
public class CodeSubmissionSettings {

	public static final Path DEFAULT_SETTINGS_FILE = Path.of("src", "appData", "code-submission-settings.properties");

	private final Path settingsFile;
	private String serviceUrl = "";
	private String instructorApiKey = "";

	public CodeSubmissionSettings(Path settingsFile) {
		this.settingsFile = settingsFile;
	}

	/** Loads the settings file if it exists; otherwise the settings stay empty. */
	public static CodeSubmissionSettings load(Path settingsFile) throws IOException {
		CodeSubmissionSettings settings = new CodeSubmissionSettings(settingsFile);
		if (Files.exists(settingsFile)) {
			Properties properties = new Properties();
			try (Reader reader = Files.newBufferedReader(settingsFile, StandardCharsets.UTF_8)) {
				properties.load(reader);
			}
			settings.serviceUrl = properties.getProperty("serviceUrl", "");
			settings.instructorApiKey = properties.getProperty("instructorApiKey", "");
		}
		return settings;
	}

	public void save() throws IOException {
		Properties properties = new Properties();
		properties.setProperty("serviceUrl", serviceUrl);
		properties.setProperty("instructorApiKey", instructorApiKey);
		LocalDataFiles.createParentFolder(settingsFile);
		try (Writer writer = Files.newBufferedWriter(settingsFile, StandardCharsets.UTF_8)) {
			properties.store(writer, "ILEP Code Submission settings. Contains a password: never commit or share.");
		}
		LocalDataFiles.restrictToOwner(settingsFile);
	}

	public boolean isComplete() {
		return !serviceUrl.isBlank() && !instructorApiKey.isBlank();
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	/** Trims spaces and trailing slashes, e.g. "https://code.example.edu/" becomes "https://code.example.edu". */
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl == null ? "" : serviceUrl.trim().replaceAll("/+$", "");
	}

	public String getInstructorApiKey() {
		return instructorApiKey;
	}

	public void setInstructorApiKey(String instructorApiKey) {
		this.instructorApiKey = instructorApiKey == null ? "" : instructorApiKey.trim();
	}
}
