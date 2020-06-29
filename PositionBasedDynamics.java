package pbd;

import java.util.ArrayList;

import javafx.animation.*;
import javafx.application.Application;
import javafx.event.*;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.geometry.*;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.util.Duration;

/*
 * Author: Daniel Rehberg
 * Instructor: Thomas Bylander
 * Class: CSC 160
 * Date: May 10, 2019
 * 
 * DISCLAIMER -- THIS IS NOT A PERFECT SIMULATION and relies on heuristic ideas, it merely
 * 		establishes the minimum needed to begin setting up Position Based Dynamics tests.
 * No Constraint for friction was included, so the object will just keep spinning in place if it
 * 	is sufficiently round.
 * Additionally, impulses are not implemented only Verlet integration for sudden positional changes
 * 	between frames.
 * 
 * This application offers a basic test of a dynamic object and a static object being brought
 * 	into contact solely through Gravity as a downward force.
 * The classes built can be extended -- and probably will be if I find time -- but exist to 
 * 	demonstrate the concept of Position Based Dynamics (PBD) rather than rigid body dynamics.
 * This is based on the work from Matthias Muller-Fischer, and offers a simplification of mechanics for 
 * 	forms with volume (area in this 2D case) by extending Newtonian particle physics into complex
 * 	joined shapes by using distance constaints, rather than always preserving rigid form and
 * 	using Euler's work on rotations.
 * The user can build an NGon by typing a positive integer greater than 2 in the Text Field beside
 * 	the "Add Polygon" button.
 * The user can start the simulation -- merely applies a gravitational pull downward -- by clicking 
 * 	"Run/Pause", and can likewise pause a running simulation with the same button.
 * The Text Field with iterCount can be filled in with any integer greater than 0 to observe what happens
 * 	in a realtime constraint solver that relies on solving the same set of problems for some count in 
 * 	an attempt to converge to a global solution.
 * 		Warning here -- if the NGon is really large in size, more contacts will be found after collision with
 * 			the ground box, and will demonstrate quite well what happens with fewer or more iterations.
 * A FPS counter runs beside these border layer while the simulation is running to try to show how frequent
 * 	the simulation is being updated -- but for consistency the delta time used for the simulation is constant.
 * The Text Area at the bottom gets filled with success or error messages in response to using the GUI buttons.
 */

public class PositionBasedDynamics extends Application{

	//constants for button sizes
	static final private double uiWidth = 85.0;
	static final private double uiHeight = 25.0;
	//Need a few buttons and stuff
	private BorderPane ui;
	private Button addNGon, setIteration;
	private Button startPauseSimulation;
	private HBox uiButtons;
	private Label frameLabel, framerate;
	private TextArea messageBoard;
	private TextField nGonVerts, iterationCount;
	//Pane for simulation
	private Pane sandbox;
	private Collision sandboxBounds;
	private Vec2 sandboxMin, sandboxMax;
	//Objects for simulation
	//Timeline runSim;//Not sure if using a Timer object would afflict JavaFX Application, so using a Animations instead.
	private AnimationTimer runSim;
	private boolean simulationRunning = false;
	private NGon testGon;
	private NGon testGround;
	private long timePrevious, timeCurrent, frameCounter, frameRateSum;
	private int iterations = 10;
	//The list of constraints to solve
	ArrayList<Constraint> constraints = new ArrayList<Constraint>();
	
	
	protected void PositionBasedDynamics() {
		
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		uiButtons = new HBox(8);
		ui = new BorderPane();
		
		//Buttons initialized in order of appearance for my own sake of record keeping.
		addNGon = new Button();
		addNGon.setText("Add Polygon");
		addNGon.setMaxSize(uiWidth, uiHeight);
		
		nGonVerts = new TextField("nCount");
		nGonVerts.setMaxSize(uiWidth, uiHeight);
		
		startPauseSimulation = new Button("Run/Pause");
		startPauseSimulation.setMaxSize(uiWidth, uiHeight);
		
		setIteration = new Button("Set Iteration");
		setIteration.setMaxSize(uiWidth, uiHeight);
		iterationCount = new TextField("iterCount");
		iterationCount.setMaxSize(uiWidth, uiHeight);
		
		frameLabel = new Label("FPS");
		framerate = new Label("0000");
		
		//Message area at the bottom
		messageBoard = new TextArea("Messages will be relayed in here.");
		messageBoard.setMaxHeight(90.0);
		
		uiButtons.getChildren().add(addNGon);
		uiButtons.getChildren().add(nGonVerts);
		uiButtons.getChildren().add(startPauseSimulation);
		uiButtons.getChildren().add(setIteration);
		uiButtons.getChildren().add(iterationCount);
		uiButtons.getChildren().add(frameLabel);
		uiButtons.getChildren().add(framerate);
		
		sandbox = new Pane();
		sandbox.setStyle(" -fx-background-color: #888888;");
		sandboxMin = new Vec2(0.0, 0.0);//This is actually constant, the edge of the sandbox will always be considered {0,0}
		sandboxMax = new Vec2();//This is updated during the simulation run.
		sandboxBounds = new Collision(sandboxMin, sandboxMax);
		
		//Build the an infinite mass for objects to interact with.
		testGround = new NGon(4, 0, new Vec2(480, 780), 400);
		for (Line l : testGround.getLines()) {
			sandbox.getChildren().add(l);
		}
		
		//Apply the simulation interval.
		//EventHandler<ActionEvent> simEvent = event -> updateSim();
		//runSim = new Timeline(60, new KeyFrame(Duration.millis(16), simEvent));//The framerate desired argument is probably redundant
		//runSim.setCycleCount(Animation.INDEFINITE);
		//runSim.play();
		
		frameCounter = 0;
		frameRateSum = 0;
		runSim = new AnimationTimer() {
			@Override
			public void handle (long time) {
				timePrevious = (frameCounter == 0) ? time - 16 : timeCurrent;
				timeCurrent = time;
				frameCounter += 1;
				long delta = (timeCurrent - timePrevious) / 1000000;
				frameRateSum += 1000 / ((delta == 0) ? 1 : delta);
				updateSim(time);
			}
		};
		
		//User interface root node
		ui.setTop(uiButtons);
		ui.setCenter(sandbox);//Set the sandbox behind the other panes -- using toBack directly muddles stuff.
		ui.setBottom(messageBoard);
		
		testGon = null;
		
		addNGon.setOnAction(event -> addNGon());
		startPauseSimulation.setOnAction(event -> runSimulation());
		setIteration.setOnAction(event -> setIterations());
		
		Scene scene = new Scene(ui, 960, 540, true, SceneAntialiasing.DISABLED);
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("Position Based Dynamics");
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		//Creates an instance of PositionBasedDyanmics and runs the start method
		launch(args);
	}

	//This method builds a new NGon for use as the dynamic object within
	// the simulation.
	private void addNGon() {
		String text = nGonVerts.getText();
		try {
			int count = Integer.parseInt(text);
			NGon temp = new NGon(count);
			if (testGon != null) {
				//remove the old lines from the sandbox pane
				removeNGon(testGon);
				testGon = null;
			}
			testGon = temp;
			for (Line l : testGon.getLines()) {
				sandbox.getChildren().add(l);
			}
			messageBoard.setText("New N-Gon Created");
		} catch(NumberFormatException e) {
			//This is where the message board will get Error information.
			messageBoard.setText("This needs to be an integer");
		} catch(IllegalArgumentException e) {
			messageBoard.setText(e.getMessage());
		}
	}
	
	//This is where constraints are solved multiple times in one frame,
		//	allowing for violations created from one solution to be correct
		//	to approach a global solution to the set of constraints.
	private void iterativeSolver() {
		for (int i = 0; i < iterations; ++i) {
			for (int j = constraints.size() - 1; j >= 0; --j) {
				constraints.get(j).solve();
			}
		}
		testGon.setLinePositions();
		constraints.clear();
	}
	
	//This method removes old lines from the Panel,
	//	avoiding the preservation of objects not needing
	//	to be rendered.
	private void removeNGon(NGon obj) {
		for (Line l : obj.getLines()) {
			sandbox.getChildren().remove(l);
		}
	}
	
	
	//This method turns the AnimationTimer on and off.
	private void runSimulation() {
		//if (runSim.getStatus() == Animation.Status.STOPPED) {
		if (!simulationRunning) {
			//runSim.play();
			simulationRunning = true;
			runSim.start();
		} else {
			simulationRunning = false;
			frameCounter = 0;
			frameRateSum = 0;
			runSim.stop();
		}
	}
	
	//This method tries to assert a new iteration count based on
	// user information from the GUI.
	private void setIterations() {
		String itr = iterationCount.getText();
		iterations = 10;
		try {
			int i = Integer.parseInt(itr);
			if (i <= 0) {
				messageBoard.setText("This needs to be a positive integer");
			} else {
				iterations = i;
			}
		} catch(NumberFormatException e) {
			messageBoard.setText("This needs to be an integer");
		}
	}
	
	//This is where the simulation update is invoked through the AnimationTimer.
	private void updateSim(long curTime) {
		
		//Update the Maximum Bound of the sandbox Node
		sandboxMax.x = sandbox.getWidth();
		sandboxMax.y = sandbox.getHeight();
		
		//Update N-Gon
		if (testGon != null) {
			if (!testGon.update(2.0, sandboxBounds)) {
				//Remove the NGon
				removeNGon(testGon);
				testGon = null;
				//Stop the simulation
				runSimulation();
				messageBoard.setText("The N-Gon has gone out of view and is being culled");
			} else {
				framerate.setText(Long.toString(frameRateSum / frameCounter));
				//Test for collision with the ground NGon
				if (testGon.intersectionBroad((Mechanics) testGround)) {
					for (int v = 0; v < testGon.getVertices().length; v++) {
						constraints.add(new DistanceConstraint(testGon.getVertices()[v], testGon.getOrigin(), testGon.getRadius()));
						if (v == testGon.getVertices().length - 1) {
							constraints.add((Constraint) new DistanceConstraint(testGon.getVertices()[v], testGon.getVertices()[0], testGon.getVertexDistance()));
						} else {
							constraints.add((Constraint) new DistanceConstraint(testGon.getVertices()[v], testGon.getVertices()[v + 1], testGon.getVertexDistance()));
						}
						for (int i = 0; i < testGround.getVertices().length; ++i) {
							if (Mechanics.intersectionNarrow(testGon.getVertices()[v], 
									testGround.getVertices(), testGround.getOrigin(), i)) {
								if (i == testGround.getVertices().length - 1) {
									constraints.add(new Constraint(testGon.getVertices()[v], testGround.getVertices()[i], testGround.getVertices()[0]));//.solve();
								} else {
									constraints.add(new Constraint(testGon.getVertices()[v], testGround.getVertices()[i], testGround.getVertices()[i+1]));//.solve();
								}
							}
						}
					}
					iterativeSolver();
				} else {
					
				}
				testGon.verlet(2.0);
			}
		} else {
			//Stop the simulation, there is nothing for it to do
			runSimulation();
			messageBoard.setText("There is nothing to run in the simulation");
		}
	}
}
