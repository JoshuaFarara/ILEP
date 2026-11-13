package views;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import models.DisplaySettings;

/**
 * The main window layout: a faded background image behind a BorderPane with
 * the menu bar on top. The controller decides what goes in the center,
 * which background image each screen uses, and the display settings.
 *
 * The background always covers the whole window, whatever its size (issue #16): it is scaled
 * up or down without stretching, and the edges that don't fit are cropped.
 */
public class MainView extends StackPane {

	/** Backgrounds are decoded at about full-HD size: sharp when maximized, still light on memory. */
	private static final int BACKGROUND_LOAD_WIDTH = 1920;
	private static final int BACKGROUND_LOAD_HEIGHT = 1200;
	private static final double BACKGROUND_OPACITY = 0.25;
	private static final URL DARK_THEME = MainView.class.getResource("dark-theme.css");

	private final BorderPane root = new BorderPane();
	private final ImageView background = new ImageView();

	// Each background is loaded once, then reused when the user returns to that screen
	private final Map<String, Image> loadedBackgrounds = new HashMap<>();

	public MainView(Node menuBar) {
		background.setPreserveRatio(true);
		background.setSmooth(true);
		background.setOpacity(BACKGROUND_OPACITY);

		// The image sits on its own layer that the layout ignores, so it never forces the window
		// to a minimum size; layoutBackground() sizes and centers it to cover the window.
		Pane backgroundLayer = new Pane(background);
		backgroundLayer.setManaged(false);
		backgroundLayer.setMouseTransparent(true);

		Rectangle clip = new Rectangle();
		clip.widthProperty().bind(widthProperty());
		clip.heightProperty().bind(heightProperty());
		setClip(clip);

		widthProperty().addListener((observable, oldWidth, newWidth) -> layoutBackground());
		heightProperty().addListener((observable, oldHeight, newHeight) -> layoutBackground());
		background.imageProperty().addListener((observable, oldImage, newImage) -> layoutBackground());

		root.setTop(menuBar);
		getChildren().addAll(backgroundLayer, root);
	}

	/** Shows a screen in the center of the window. */
	public void setContent(Node content) {
		root.setCenter(content);
	}

	/**
	 * Shows the faded image at this URL behind every screen, or no image if the URL is null.
	 * The image loads in the background at a limited size, so a large photo never slows the window.
	 */
	public void setBackgroundImage(String imageUrl) {
		if (imageUrl == null) {
			background.setImage(null);
			return;
		}
		background.setImage(loadedBackgrounds.computeIfAbsent(imageUrl, url -> {
			Image image = new Image(url, BACKGROUND_LOAD_WIDTH, BACKGROUND_LOAD_HEIGHT, true, true, true);
			// The image's real size is only known once loading finishes
			image.progressProperty().addListener((observable, oldProgress, newProgress) -> {
				if (newProgress.doubleValue() >= 1.0) {
					layoutBackground();
				}
			});
			return image;
		}));
	}

	/**
	 * Applies dark mode, text size, font and text color to every screen (issue #15).
	 * Headings use relative sizes (em), so they grow and shrink with the text size.
	 */
	public void applyDisplaySettings(DisplaySettings settings) {
		StringBuilder style = new StringBuilder()
				.append("-fx-font-size: ").append(settings.getTextSize()).append("px;")
				.append("-fx-font-family: \"").append(settings.getFontFamily()).append("\";");
		String textColor = settings.getTextColorHex();
		if (textColor != null) {
			// Text on backgrounds (labels), on controls (buttons, lists) and on ILEP's own buttons
			style.append("-fx-text-background-color: ").append(textColor).append(';')
					.append("-fx-text-base-color: ").append(textColor).append(';')
					.append("-ilep-button-text-color: ").append(textColor).append(';');
		}
		setStyle(style.toString());

		if (DARK_THEME != null) {
			String darkThemeUrl = DARK_THEME.toExternalForm();
			getStylesheets().remove(darkThemeUrl);
			if (settings.isDarkMode()) {
				getStylesheets().add(darkThemeUrl);
			}
		}
	}

	/** Scales the image so it covers the whole window without stretching, and centers it. */
	private void layoutBackground() {
		Image image = background.getImage();
		double windowWidth = getWidth();
		double windowHeight = getHeight();
		if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0 || windowWidth <= 0 || windowHeight <= 0) {
			return;
		}
		double scale = Math.max(windowWidth / image.getWidth(), windowHeight / image.getHeight());
		double displayedWidth = image.getWidth() * scale;
		double displayedHeight = image.getHeight() * scale;
		background.setFitWidth(displayedWidth);
		background.setFitHeight(displayedHeight);
		background.setLayoutX((windowWidth - displayedWidth) / 2);
		background.setLayoutY((windowHeight - displayedHeight) / 2);
	}
}
