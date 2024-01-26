package com.fluidity.structure;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Fluidity extends Application {
	@Override
	public void start(Stage stage) throws IOException {
		Manager sceneManager = new Manager(stage);
		sceneManager.loadMainMenu();
	}

	public static void main(String[] args) {
		launch();
	}
}