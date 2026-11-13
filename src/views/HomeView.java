package views;

import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

/**
 * Home screen: shows a few images. Which images to show is decided by HomeController.
 * The images grow and shrink with the window, about three per row (issue #16).
 */
public class HomeView extends FlowPane {

	private static final double GAP = 8;
	private static final double IMAGES_PER_ROW = 3;
	private static final double MINIMUM_IMAGE_WIDTH = 140;
	/** Images are decoded at this size, so they stay sharp when the window is large. */
	private static final int MAXIMUM_IMAGE_WIDTH = 480;
	private static final int MAXIMUM_IMAGE_HEIGHT = 360;

	// Width of each image: the window width shared by three images, within sensible limits
	private final DoubleBinding imageWidth = Bindings.createDoubleBinding(() -> {
		double available = getWidth() - 2 * GAP - (IMAGES_PER_ROW - 1) * GAP;
		return Math.max(MINIMUM_IMAGE_WIDTH, Math.min(MAXIMUM_IMAGE_WIDTH, available / IMAGES_PER_ROW));
	}, widthProperty());

	public HomeView() {
		setVgap(GAP);
		setHgap(GAP);
		setPadding(new Insets(GAP));
	}

	/**
	 * Replaces the displayed images with the given image URLs. Each image is decoded at a limited
	 * size and loads in the background, so large photos don't slow startup.
	 */
	public void setImages(List<String> imageUrls) {
		getChildren().clear();
		for (String url : imageUrls) {
			Image image = new Image(url, MAXIMUM_IMAGE_WIDTH, MAXIMUM_IMAGE_HEIGHT, true, true, true);
			ImageView imageView = new ImageView(image);
			imageView.setPreserveRatio(true);
			imageView.setSmooth(true);
			imageView.fitWidthProperty().bind(imageWidth);
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
