package com.fluidity.program.ui.controllers;

import com.fluidity.program.ui.ProgramState;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.ResourceBundle;

public class ExitController extends Controller {

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {

	}

	@FXML
	public void onExitSimulationButtonClick() {
		manager.close();
	}

	@FXML
	public void onReturnToHomeButtonClick() {
		manager.loadScene(ProgramState.MAIN_MENU);
	}
}
