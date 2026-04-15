package views;

import java.io.File;
import java.util.ArrayList;

import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;

public class HomeView extends FlowPane {
	private ArrayList<Image> images;
//	Image[] images = {new Image(getClass().getResource("/assets/images/sweetHeartCircleFront.jpg").toExternalForm())};
	final static File IMAGES_FOLDER = new File("C:\\Users\\jfarara\\Documents\\Github\\ILEP\\src\\assets\\images");
	final int LIST_VIEW_WIDTH = 700;
	final int LIST_VIEW_HEIGHT = 350;
	
	HomeView() {
		images = new ArrayList<>();
		setVgap(8);
		setHgap(4);
		setPrefWrapLength(300); // preferred width = 300
//		flowPaneImages = showImages();
		
		getChildren().addAll();

	}

	public void getBackgroundImages() {
		File folder = IMAGES_FOLDER;
		if (!folder.exists() || !folder.isDirectory()) {
			System.err.println("Data folder not found: " + IMAGES_FOLDER.getAbsolutePath());
			return;
		}

		for (File file : folder.listFiles()) {
			if (file.isFile() && file.getName().endsWith(".png") || file.getName().endsWith(".png")) {
				try {
					images.add(new Image(file.getName()));
				} catch (Exception e) {
					System.err.println("Skipping invalid file: " + file.getName() + " - " + e.getMessage());
				}
			}
		}
		System.out.println("Loaded " + images.size() + " images successfully.");
	}
	
//	public static showImages() {
//		
//		for (int i = 0; i < images.size(); i++) {
//	         flow.getChildren().add(new ImageView(image[i]);
//	     }
//	}
}
