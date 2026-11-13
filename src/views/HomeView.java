package views;

import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

/** Home screen: shows a few images. Which images to show is decided by HomeController. */
public class HomeView extends FlowPane {

	private static final int IMAGE_WIDTH = 200;
	private static final int IMAGE_HEIGHT = 150;

	public HomeView() {
		setVgap(8);
		setHgap(8);
		setPrefWrapLength(900);
	}

	/**
	 * Replaces the displayed images with the given image URLs. Each image is scaled to thumbnail
	 * size while it loads, and loads in the background, so large photos don't slow startup.
	 */
	public void setImages(List<String> imageUrls) {
		getChildren().clear();
		for (String url : imageUrls) {
			Image image = new Image(url, IMAGE_WIDTH, IMAGE_HEIGHT, true, true, true);
			ImageView imageView = new ImageView(image);
			imageView.setFitWidth(IMAGE_WIDTH);
			imageView.setFitHeight(IMAGE_HEIGHT);
			imageView.setPreserveRatio(true);
			// An unreadable file is removed once loading fails, instead of leaving an empty gap
			image.errorProperty().addListener((observable, hadError, hasError) -> {
				if (hasError) {
					System.err.println("Skipping image: " + url);
					getChildren().remove(imageView);
				}
			});
			getChildren().add(imageView);
		}
	}
}
