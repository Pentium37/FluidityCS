package com.fluidity.structure.controllers;

import com.fluidity.structure.ProgramState;
import javafx.fxml.FXML;

public class HomePageController extends Controller{

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

	}



	@Override
	void initialise() {

	}
}
