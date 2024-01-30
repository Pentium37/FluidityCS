package com.fluidity.program.ui.controllers;

import com.fluidity.program.ui.ProgramState;
import javafx.fxml.FXML;

public class ExitController extends Controller {

	@FXML
	public void onExitSimulationButtonClick() {
		manager.close();
	}

	@FXML
	public void onReturnToHomeButtonClick() {
		manager.loadScene(ProgramState.MAIN_MENU);
	}
}
