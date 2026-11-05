package views;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/**
 * The main window layout: a faded background image behind a BorderPane with
 * the menu bar on top. The controller decides what goes in the center.
 */
public class MainView extends StackPane {

	private static final int BACKGROUND_IMAGE_WIDTH = 805;
	private static final int BACKGROUND_IMAGE_HEIGHT = 525;
	private static final String BACKGROUND_IMAGE = "/assets/images/sweetHeartCircleFront.jpg";

	private final BorderPane root = new BorderPane();

	public MainView(Node menuBar) {
		Image image = new Image(getClass().getResource(BACKGROUND_IMAGE).toExternalForm());
		ImageView background = new ImageView(image);
		background.setFitWidth(BACKGROUND_IMAGE_WIDTH);
		background.setFitHeight(BACKGROUND_IMAGE_HEIGHT);
		background.setOpacity(.25);

		root.setTop(menuBar);
		getChildren().addAll(background, root);
	}

	/** Shows a screen in the center of the window. */
	public void setContent(Node content) {
		root.setCenter(content);
	}
}
