package com.fluidity.structure;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;

// Would be preferable to name it Scene/View, but it would override JFX classes
public enum ProgramState {
	MAIN_MENU("/MainMenu.fxml"),
	SETTINGS("/Settings.fxml"),
	ABOUT("/About.fxml"),
	EXIT("/Exit.fxml"),
	RECORDINGS("/Recordings.fxml");

	// path of .fxml file to load later
	private final String path;
	private Scene scene = null;
	private Controller controller = null;

	ProgramState(String path) {
		this.path = path;
	}

	public Scene getScene() throws IOException {
		if (scene == null) {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(this.path));
			this.scene = new Scene(loader.load(), 1280, 720);
			this.controller = loader.getController();
		}
		return scene;
	}

}
