package com.fluidity.program.ui;

import com.fluidity.program.ui.controllers.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class Manager {
	// The stage which the entirety of the program will be on
	// My window is private because I don't want any class other than this manager class to modify it
	// The only way I can then make changes to the window is through the public methods provided through this class
	private final Stage window;

	/*Enum maps containing the scenes and controllers corresponding to each program state .
	The controller is of the type of the abstract class Controller as every other controller in my program extends off this
	and can therefore be classified as part of the class. */
	private final Map<ProgramState, Scene> scenes;
	private final Map<ProgramState, Controller> controllers;

	// The manager is the main class which contains the stage
	// It controls the switching between scenes
	public Manager(final Stage window) {
		scenes = new EnumMap<>(ProgramState.class);
		controllers = new EnumMap<>(ProgramState.class);
		this.window = window;
		window.setTitle("Fluidity - The Eulerian Fluid Simulator");
		for (ProgramState programState : ProgramState.values()) {
			initialiseUI(programState);
			setManager(programState);
		}
	}

	public void loadScene(ProgramState programState) {
		// Loads the Scene stored in the programState Enum
		// Makes sure the window is not resizable
		window.setResizable(false);
		window.setScene(scenes.get(programState));
		window.show();
	}

	/*Uses the fxml loader to load the scene from the path corresponding to the object and then
	 whenever the function is called after this, loading does not need to happen as the scene is loaded*/
	private void initialiseUI(ProgramState programState) {
		try {
			// Object which loads the FXML file in the path
			// If there is no path, an exception is thrown and the catch block is run -> The program terminates
			FXMLLoader loader = new FXMLLoader(getClass().getResource(programState.getPath()));

			// Creates a scene and controller corresponding to the FXML loader found
			// Places the objects into the map so that they can be retrieved if needed
			//			scenes.put(programState, new Scene(loader.load(), 1280, 720));
			scenes.put(programState, new Scene(loader.load(), 200, 200));
			controllers.put(programState, loader.getController());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// Handler method which initialises the manager for the controllers of each of the program state
	private void setManager(ProgramState programState) {
		// Passing in the current instance of the manager as the instance for the controllers to provide data to
		controllers.get(programState)
				.setManager(this);
	}

	// Closes the stage in the case that the program needs to be safely exited from
	public void close() {
		window.close();
	}
}