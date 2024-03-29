package com.fluidity.program;

import com.fluidity.program.test.Prototype3;
import com.fluidity.program.ui.Manager;
import com.fluidity.program.ui.ProgramState;
import com.fluidity.program.utilities.Deque;
import com.fluidity.program.utilities.FileHandler;
import javafx.application.Application;
import javafx.stage.Stage;

public class Fluidity extends Application {

	// All that is needed to create is a Manager object rather than creating multiple objects for all the different scenes
	@Override
	public void start(Stage stage) {
		FileHandler.createFile("configurations.txt");
		Manager sceneManager = new Manager(stage);
		sceneManager.loadScene(ProgramState.MAIN_MENU);

//		Prototype3 prototype3 = new Prototype3();
//		prototype3.test1();
		//		sceneManager.loadScene(ProgramState.TEST_SIMULATION);
	}

	public static void main(String[] args) {
		launch();
	}
}