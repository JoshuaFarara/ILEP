package views;

import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

/** Home screen: shows a grid of images. Which images to show is decided by HomeController. */
public class HomeView extends FlowPane {

	private static final int IMAGE_WIDTH = 200;
	private static final int IMAGE_HEIGHT = 150;

	public HomeView() {
		setVgap(8);
		setHgap(8);
		setPrefWrapLength(900);
	}

	/** Replaces the displayed images with the given image URLs. */
	public void setImages(List<String> imageUrls) {
		getChildren().clear();
		for (String url : imageUrls) {
			Image image = new Image(url);
			if (image.isError()) {
				System.err.println("Skipping image: " + url);
				continue;
			}
			ImageView iv = new ImageView(image);
			iv.setFitWidth(IMAGE_WIDTH);
			iv.setFitHeight(IMAGE_HEIGHT);
			iv.setPreserveRatio(true);
			getChildren().add(iv);
		}
	}
}
