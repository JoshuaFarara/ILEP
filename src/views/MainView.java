package views;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/**
 * The main window layout: a faded background image behind a BorderPane with
 * the menu bar on top. The controller decides what goes in the center and
 * which background image each screen uses.
 */
public class MainView extends StackPane {

	private static final int BACKGROUND_IMAGE_WIDTH = 805;
	private static final int BACKGROUND_IMAGE_HEIGHT = 525;
	private static final double BACKGROUND_OPACITY = 0.25;

	private final BorderPane root = new BorderPane();
	private final ImageView background = new ImageView();

	// Each background is loaded once, then reused when the user returns to that screen
	private final Map<String, Image> loadedBackgrounds = new HashMap<>();

	public MainView(Node menuBar) {
		background.setFitWidth(BACKGROUND_IMAGE_WIDTH);
		background.setFitHeight(BACKGROUND_IMAGE_HEIGHT);
		background.setOpacity(BACKGROUND_OPACITY);

		root.setTop(menuBar);
		getChildren().addAll(background, root);
	}

	/** Shows a screen in the center of the window. */
	public void setContent(Node content) {
		root.setCenter(content);
	}

	/**
	 * Shows the faded image at this URL behind every screen, or no image if the URL is null.
	 * The image is scaled down while it loads and loads in the background, so a large photo
	 * never slows the window down.
	 */
	public void setBackgroundImage(String imageUrl) {
		if (imageUrl == null) {
			background.setImage(null);
			return;
		}
		background.setImage(loadedBackgrounds.computeIfAbsent(imageUrl, url ->
				new Image(url, BACKGROUND_IMAGE_WIDTH, BACKGROUND_IMAGE_HEIGHT, false, true, true)));
	}
}
