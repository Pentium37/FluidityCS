package com.fluidity.program.ui.controllers;

import com.fluidity.program.ui.ProgramState;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.ResourceBundle;

public class MainMenuController extends Controller {

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {

	}

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
}
