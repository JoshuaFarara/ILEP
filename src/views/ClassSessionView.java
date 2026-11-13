package views;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import models.ClassActivity;
import models.Roster;
import models.Student;

/**
 * Main screen for a class in progress. Shows the date and time, the present
 * students, an "Activities" menu (Slide Activities, Quiz, ...) and a
 * "Student Tools" menu (shuffle, random student, random groups).
 *
 * Like the other views it only displays what it is given and reports what the
 * user picks; ClassSessionController does the work.
 */
public class ClassSessionView extends BorderPane {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm:ss a");
	private static final int LIST_WIDTH = 260;
	private static final int LIST_HEIGHT = 360;
	private static final int DEFAULT_GROUP_SIZE = 3;
	private static final int MAX_GROUP_SIZE = 8;

	private final Label dateLabel = new Label();
	private final Label timeLabel = new Label();
	private final Label sessionInfo = new Label();
	private final Menu activitiesMenu = new Menu("Activities");
	private final Menu toolsMenu = new Menu("Student Tools");

	private final ListView<Student> studentList = new ListView<>();
	private final VBox resultPanel = new VBox(8);
	private final HBox sessionContent;

	private Consumer<ClassActivity> onActivitySelected;
	private Runnable onShuffle;
	private Runnable onRandomStudent;
	private IntConsumer onRandomGroups;
	private Runnable onTakeAttendance;

	public ClassSessionView() {
		setPadding(new Insets(10));

		// Header: title on the left, live date + time on the right
		Label title = heading("Class Session", 18);
		dateLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
		timeLabel.setFont(Font.font(14));
		VBox clockBox = new VBox(2, dateLabel, timeLabel);
		clockBox.setAlignment(Pos.CENTER_RIGHT);
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		HBox header = new HBox(title, spacer, clockBox);
		header.setAlignment(Pos.CENTER_LEFT);

		setTop(new VBox(6, header, buildMenuBar(), sessionInfo));

		// Body: present students on the left, activity results on the right
		studentList.setPrefSize(LIST_WIDTH, LIST_HEIGHT);
		resultPanel.setPadding(new Insets(0, 0, 0, 10));
		HBox.setHgrow(resultPanel, Priority.ALWAYS);
		sessionContent = new HBox(studentList, resultPanel);
		sessionContent.setPadding(new Insets(8, 0, 0, 0));

		startClock();
		showNoSession();
	}

	// ------------------------------------------------------------ controller API
	/** Shown when no roll call has been started yet. */
	public void showNoSession() {
		sessionInfo.setText("");
		setMenusEnabled(false);

		Label message = new Label("No attendance has been taken yet. Take attendance to start the class session.");
		Button takeAttendance = new Button("Take Attendance");
		takeAttendance.setOnAction(e -> run(onTakeAttendance));
		VBox box = new VBox(10, message, takeAttendance);
		box.setPadding(new Insets(20, 0, 0, 0));
		setCenter(box);
	}

	/** Shows the session for the given rosters and present students. */
	public void showSession(List<Roster> rosters, List<Student> presentStudents) {
		StringBuilder codes = new StringBuilder();
		for (Roster r : rosters) {
			if (codes.length() > 0) codes.append(", ");
			codes.append(r.getCourseName()).append(r.getCourseCode()).append(r.getCourseSection());
		}
		sessionInfo.setText("Rosters: " + codes + "   |   Present: " + presentStudents.size());

		setStudents(presentStudents);
		setCenter(sessionContent);
		setMenusEnabled(!presentStudents.isEmpty());

		if (presentStudents.isEmpty()) {
			showMessage("No students are marked present. Mark students on the Attendance screen.");
		} else {
			showMessage("Choose an activity or a student tool from the menu above.");
		}
	}

	/** Updates the order of the student list (e.g. after a shuffle). */
	public void setStudents(List<Student> students) {
		studentList.setItems(FXCollections.observableArrayList(students));
		studentList.getSelectionModel().clearSelection();
	}

	public void showMessage(String text) {
		Label label = new Label(text);
		label.setWrapText(true);
		resultPanel.getChildren().setAll(label);
	}

	public void showPickedStudent(Student student) {
		studentList.getSelectionModel().select(student);
		studentList.scrollTo(student);
		resultPanel.getChildren().setAll(new Label("Random student:"), heading(displayName(student), 28));
	}

	public void showGroups(List<List<Student>> groups) {
		FlowPane flow = new FlowPane(10, 10);
		flow.setPrefWrapLength(420);
		for (int i = 0; i < groups.size(); i++) {
			VBox box = new VBox(2);
			box.setPadding(new Insets(6));
			box.setPrefWidth(130);
			box.setStyle("-fx-border-color: #336699; -fx-border-radius: 4; -fx-background-color: rgba(255,255,255,0.7);");
			box.getChildren().add(heading("Group " + (i + 1), 13));
			for (Student s : groups.get(i)) {
				box.getChildren().add(new Label(displayName(s)));
			}
			flow.getChildren().add(box);
		}
		ScrollPane scroll = new ScrollPane(flow);
		scroll.setFitToWidth(true);
		resultPanel.getChildren().setAll(new Label(groups.size() + " random groups:"), scroll);
	}

	public void showActivity(ClassActivity activity, int presentCount) {
		Label note = new Label("Coming soon. This activity will run with the " + presentCount + " present students.");
		note.setWrapText(true);
		resultPanel.getChildren().setAll(heading(activity.getLabel(), 18), note);
	}

	/** Shows an activity's own panel (e.g. Code Submission) in the result area, filling the space. */
	public void showPanel(Node panel) {
		VBox.setVgrow(panel, Priority.ALWAYS);
		resultPanel.getChildren().setAll(panel);
	}

	// ------------------------------------------------------------ handlers
	public void setOnActivitySelected(Consumer<ClassActivity> handler) { onActivitySelected = handler; }
	public void setOnShuffle(Runnable handler) { onShuffle = handler; }
	public void setOnRandomStudent(Runnable handler) { onRandomStudent = handler; }
	/** Called with the group size the user chose. */
	public void setOnRandomGroups(IntConsumer handler) { onRandomGroups = handler; }
	public void setOnTakeAttendance(Runnable handler) { onTakeAttendance = handler; }

	// ------------------------------------------------------------ layout
	private MenuBar buildMenuBar() {
		for (ClassActivity activity : ClassActivity.values()) {
			MenuItem item = new MenuItem(activity.getLabel());
			item.setOnAction(e -> {
				if (onActivitySelected != null) onActivitySelected.accept(activity);
			});
			activitiesMenu.getItems().add(item);
		}

		MenuItem shuffle = new MenuItem("Shuffle Students");
		shuffle.setOnAction(e -> run(onShuffle));
		MenuItem randomStudent = new MenuItem("Choose Random Student");
		randomStudent.setOnAction(e -> run(onRandomStudent));
		MenuItem randomGroups = new MenuItem("Group Students Randomly...");
		randomGroups.setOnAction(e -> askGroupSize().ifPresent(size -> {
			if (onRandomGroups != null) onRandomGroups.accept(size);
		}));
		toolsMenu.getItems().addAll(shuffle, randomStudent, randomGroups);

		return new MenuBar(activitiesMenu, toolsMenu);
	}

	private OptionalInt askGroupSize() {
		List<Integer> sizes = IntStream.rangeClosed(2, MAX_GROUP_SIZE).boxed().toList();
		ChoiceDialog<Integer> dialog = new ChoiceDialog<>(DEFAULT_GROUP_SIZE, sizes);
		dialog.setTitle("Group Students Randomly");
		dialog.setHeaderText("How many students per group?");
		dialog.setContentText("Group size:");
		return dialog.showAndWait().map(OptionalInt::of).orElse(OptionalInt.empty());
	}

	private void setMenusEnabled(boolean enabled) {
		activitiesMenu.setDisable(!enabled);
		toolsMenu.setDisable(!enabled);
	}

	private void startClock() {
		updateClock();
		Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock()));
		clock.setCycleCount(Timeline.INDEFINITE);
		clock.play();
	}

	private void updateClock() {
		LocalDateTime now = LocalDateTime.now();
		dateLabel.setText(now.format(DATE_FORMAT));
		timeLabel.setText(now.format(TIME_FORMAT));
	}

	// ------------------------------------------------------------ helpers
	private static String displayName(Student s) {
		return s.getFirstName() + " " + s.getLastName();
	}

	private static void run(Runnable handler) {
		if (handler != null) {
			handler.run();
		}
	}

	private static Label heading(String text, double size) {
		Label l = new Label(text);
		l.setFont(Font.font(null, FontWeight.BOLD, size));
		return l;
	}
}
