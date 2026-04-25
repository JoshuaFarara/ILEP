//package views;
//
//import java.io.File;
//import java.util.ArrayList;
//
//import javafx.scene.image.Image;
//import javafx.scene.image.ImageView;
//import javafx.scene.layout.FlowPane;
//
//public class HomeView extends FlowPane {
//	private ArrayList<Image> images;
////	Image[] images = {new Image(getClass().getResource("/assets/images/sweetHeartCircleFront.jpg").toExternalForm())};
//	final static File IMAGES_FOLDER = new File("C:\\Users\\jfarara\\Documents\\Github\\ILEP\\src\\assets\\images");
//	final int LIST_VIEW_WIDTH = 700;
//	final int LIST_VIEW_HEIGHT = 350;
//	static FlowPane flowPaneImages;
//	
//	HomeView() {
////		images = new ArrayList<>();
//		setVgap(8);
//		setHgap(4);
//		setPrefWrapLength(300); // preferred width = 300
////		getBackgroundImages();
//		flowPaneImages = showImages(images);
//		
////		getChildren().addAll(flowPaneImages);
//
//	}
//
//	public ArrayList<Image> getBackgroundImages() {
//		images = new ArrayList<>();
//		File folder = IMAGES_FOLDER;
//		if (!folder.exists() || !folder.isDirectory()) {
//			System.err.println("Data folder not found: " + IMAGES_FOLDER.getAbsolutePath());
//		
//		}
//
//		for (File file : folder.listFiles()) {
//			if (file.isFile()) {// && file.getName().endsWith(".png") || file.getName().endsWith(".jpg")) {
//				try {
//					images.add(new Image(getClass().getResource(file.getName()).toExternalForm()));
//				} catch (Exception e) {
//					System.err.println("Skipping invalid file: " + file.getName() + " - " + e.getMessage());
//				}
//			}
//		}
//		System.out.println("Loaded " + images.size() + " images successfully.");
//		return images;
//	}
//	
//	public FlowPane showImages(ArrayList<Image> images) {
//		FlowPane flow = new FlowPane();
//		images = getBackgroundImages();
//		for (Image image : images) {
//			
//	         flow.getChildren().addAll(new ImageView(image));
//	     }
//		return flow;
//	}
//}
package views;

import java.io.File;
import java.util.ArrayList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

public class HomeView extends FlowPane {

    // ✅ Injected — not hardcoded. Pass this in from App.java
    private final File imagesFolder;

    private static final int IMAGE_WIDTH  = 200;
    private static final int IMAGE_HEIGHT = 150;

    public HomeView(File imagesFolder) {
        this.imagesFolder = imagesFolder;

        // FlowPane config — HomeView IS the FlowPane
        setVgap(8);
        setHgap(8);
        setPrefWrapLength(900);

        // ✅ Load then display — in the right order
        ArrayList<Image> images = loadImages();
        populateFlowPane(images);
    }

    // ✅ Responsibility: load images from disk, return them
    private ArrayList<Image> loadImages() {
        ArrayList<Image> images = new ArrayList<>();

        if (!imagesFolder.exists() || !imagesFolder.isDirectory()) {
            System.err.println("Images folder not found: "
                + imagesFolder.getAbsolutePath());
            return images; // return empty list — don't crash
        }

        File[] files = imagesFolder.listFiles();
        if (files == null) {
            System.err.println("Could not read folder contents.");
            return images;
        }

        for (File file : files) {
            if (isImageFile(file)) {
                try {
                    // ✅ file.toURI().toString() — correct for filesystem paths
                    images.add(new Image(file.toURI().toString()));
                } catch (Exception e) {
                    System.err.println("Skipping: " + file.getName()
                        + " — " + e.getMessage());
                }
            }
        }

        System.out.println("Loaded " + images.size() + " images.");
        return images;
    }

    // ✅ Responsibility: take images, add ImageViews to THIS FlowPane
    private void populateFlowPane(ArrayList<Image> images) {
        for (Image image : images) {
            ImageView iv = new ImageView(image);
            iv.setFitWidth(IMAGE_WIDTH);
            iv.setFitHeight(IMAGE_HEIGHT);
            iv.setPreserveRatio(true); // ✅ don't stretch/squash
            getChildren().add(iv);    // ✅ add to THIS FlowPane, not a new one
        }
    }

    // ✅ Keeps file filtering logic in one readable place
    private boolean isImageFile(File file) {
        if (!file.isFile()) return false;
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg")
            || name.endsWith(".jpeg")
            || name.endsWith(".png")
            || name.endsWith(".gif");
    }
}