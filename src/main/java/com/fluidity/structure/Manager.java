package com.fluidity.structure;

import com.fluidity.structure.controllers.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class Manager {
	// The stage which the entirety of the program will be on
	private final Stage window;
	private final Map<ProgramState, Scene> scenes = new EnumMap<>(ProgramState.class);
	private final Map<ProgramState, Controller> controllers = new EnumMap<>(ProgramState.class);

	// The manager is the main class which contains the stage
	// It controls the switching between scenes
	public Manager(final Stage window) {
		this.window = window;
		window.setTitle("Fluidity - The Eulerian Fluid Simulator");
		for (ProgramState programState : ProgramState.values()) {
			initialiseUI(programState);
			setManager(programState);
		}
	}

	public void loadScene(ProgramState programState) {
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
			scenes.put(programState, new Scene(loader.load(), 1280, 720));
			controllers.put(programState, loader.getController());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void setManager(ProgramState programState) {
		controllers.get(programState)
				.setManager(this);
	}
}

// Checks if the scene hasn't been fetched yet
// if it hasn't been fetched, it attempts to fetch it from the corresponding fxml path