package views;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import models.TimeOfDayGreeting;

/**
 * Home screen: a greeting, the time and date in large type, quick-action buttons, and one
 * featured image. Like the other views it only displays and reports clicks; HomeController
 * decides which image to show and what the buttons do.
 */
public class HomeView extends VBox {

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm:ss a");
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
	/** The featured image is decoded at this size: sharp on large windows, light on memory. */
	private static final int FEATURED_IMAGE_LOAD_WIDTH = 960;
	private static final int FEATURED_IMAGE_LOAD_HEIGHT = 720;

	private final Label greetingLabel = new Label();
	private final Label timeLabel = new Label();
	private final Label dateLabel = new Label();
	private final ImageView featuredImage = new ImageView();
	private final StackPane featuredImageFrame = new StackPane(featuredImage);

	private Runnable onTakeAttendance;
	private Runnable onStartClassSession;
	private Runnable onOpenRosterManager;

	public HomeView() {
		super(10);
		setAlignment(Pos.TOP_CENTER);
		setPadding(new Insets(24, 16, 16, 16));

		greetingLabel.getStyleClass().add("heading");
		timeLabel.getStyleClass().add("home-clock");
		dateLabel.getStyleClass().add("heading-small");

		Button takeAttendance = new Button("Take Attendance");
		takeAttendance.setOnAction(event -> run(onTakeAttendance));
		Button startClassSession = new Button("Start Class Session");
		startClassSession.setOnAction(event -> run(onStartClassSession));
		Button openRosterManager = new Button("Roster Manager");
		openRosterManager.setOnAction(event -> run(onOpenRosterManager));
		for (Button button : new Button[] { takeAttendance, startClassSession, openRosterManager }) {
			button.getStyleClass().add("home-action-button");
		}
		// Wraps to a second row on narrow windows instead of being cut off
		FlowPane quickActions = new FlowPane(12, 10, takeAttendance, startClassSession, openRosterManager);
		quickActions.setAlignment(Pos.CENTER);
		quickActions.setPadding(new Insets(8, 0, 8, 0));

		// The featured image fills the space left below the buttons, without stretching
		featuredImage.setPreserveRatio(true);
		featuredImage.setSmooth(true);
		featuredImage.setOpacity(0);
		// Rounded corners and a soft shadow that always match the picture's own shape
		Rectangle roundedCorners = new Rectangle();
		roundedCorners.setArcWidth(16);
		roundedCorners.setArcHeight(16);
		featuredImage.layoutBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
			roundedCorners.setWidth(newBounds.getWidth());
			roundedCorners.setHeight(newBounds.getHeight());
		});
		featuredImage.setClip(roundedCorners);
		featuredImageFrame.setEffect(new DropShadow(14, Color.rgb(0, 0, 0, 0.35)));
		featuredImageFrame.setMinSize(0, 0);
		VBox.setVgrow(featuredImageFrame, Priority.ALWAYS);
		featuredImage.fitWidthProperty().bind(featuredImageFrame.widthProperty().subtract(16));
		featuredImage.fitHeightProperty().bind(featuredImageFrame.heightProperty().subtract(16));

		getChildren().addAll(greetingLabel, timeLabel, dateLabel, quickActions, featuredImageFrame);
		startClock();
	}

	// ------------------------------------------------------------ controller API
	/**
	 * Shows one framed image, or none if the URL is null. It loads in the background and fades in
	 * once it is ready, instead of popping in.
	 */
	public void setFeaturedImage(String imageUrl) {
		featuredImageFrame.setVisible(imageUrl != null);
		featuredImageFrame.setManaged(imageUrl != null);
		if (imageUrl == null) {
			return;
		}
		Image image = new Image(imageUrl, FEATURED_IMAGE_LOAD_WIDTH, FEATURED_IMAGE_LOAD_HEIGHT, true, true, true);
		featuredImage.setImage(image);
		image.progressProperty().addListener((observable, oldProgress, newProgress) -> {
			if (newProgress.doubleValue() >= 1.0 && !image.isError()) {
				FadeTransition fadeIn = new FadeTransition(Duration.millis(400), featuredImage);
				fadeIn.setToValue(1);
				fadeIn.play();
			}
		});
		image.errorProperty().addListener((observable, hadError, hasError) -> {
			if (hasError) {
				System.err.println("Skipping featured image: " + imageUrl);
				setFeaturedImage(null);
			}
		});
	}

	// ------------------------------------------------------------ handlers
	public void setOnTakeAttendance(Runnable handler) { onTakeAttendance = handler; }
	public void setOnStartClassSession(Runnable handler) { onStartClassSession = handler; }
	public void setOnOpenRosterManager(Runnable handler) { onOpenRosterManager = handler; }

	// ------------------------------------------------------------ helpers
	private void startClock() {
		updateClock();
		Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateClock()));
		clock.setCycleCount(Timeline.INDEFINITE);
		clock.play();
	}

	private void updateClock() {
		LocalDateTime now = LocalDateTime.now();
		greetingLabel.setText(TimeOfDayGreeting.greetingFor(now.toLocalTime()));
		timeLabel.setText(now.format(TIME_FORMAT));
		dateLabel.setText(now.format(DATE_FORMAT));
	}

	private static void run(Runnable handler) {
		if (handler != null) {
			handler.run();
		}
	}
}
