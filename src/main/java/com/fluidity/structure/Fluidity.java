package com.fluidity.structure;

import com.fluidity.structure.ui.Manager;
import com.fluidity.structure.ui.ProgramState;
import javafx.application.Application;
import javafx.stage.Stage;

public class Fluidity extends Application {

	// All that is needed to create is a Manager object rather than creating multiple objects for all the different scenes
	@Override
	public void start(Stage stage) {
		Manager sceneManager = new Manager(stage);
		sceneManager.loadScene(ProgramState.MAIN_MENU);
	}

	public static void main(String[] args) {
		launch();
	}
}