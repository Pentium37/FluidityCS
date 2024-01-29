package com.fluidity.structure.ui.controllers;

import com.fluidity.structure.ui.ProgramState;
import javafx.fxml.FXML;

public class MainMenuController extends Controller {

	@FXML
	public void onStartSimulationButtonClick() {
		manager.loadScene(ProgramState.SIMULATION);
	}

	@FXML
	public void onViewRecordingsButtonClick() {
		manager.loadScene(ProgramState.RECORDINGS);
	}

	@FXML
	public void onSettingsButtonClick() {
		manager.loadScene(ProgramState.SETTINGS);
	}

	@FXML
	public void onAboutButtonClick() {
		manager.loadScene(ProgramState.ABOUT);
	}

	@FXML
	public void onExitButtonClick() {
		manager.loadScene(ProgramState.EXIT);
	}

	@Override
	void initialise() {

	}
}
