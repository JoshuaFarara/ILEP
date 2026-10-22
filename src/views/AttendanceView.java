package views;

import java.io.File;

import javafx.application.Application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import models.Attendance;
import models.Roster;
import models.RosterLoader;
import models.RosterManager;
import views.MenuBarView;
import views.RosterManagerView;

public class AttendanceView extends BorderPane{
	
	final int LIST_VIEW_WIDTH = 700;
	final int LIST_VIEW_HEIGHT = 350;

	private Label title = new Label("Roster Manager");
	private Button selectRoster = new Button("Select");
	private Button loadRoster = new Button("Load Roster");
	private RosterManager rm;
	private RosterLoader rl;
	private Attendance att;
	private ListView<Roster> rosterListView;
	private VBox rosterDetailView;
	Pane selectedRostersPane = new Pane();
	
	
	public AttendanceView(RosterManager rm, RosterLoader rl) {
		this.rm = rm;
		this.rl = rl;
		rl.loadAll(rm);
		
	}
	
	
	
	
}
