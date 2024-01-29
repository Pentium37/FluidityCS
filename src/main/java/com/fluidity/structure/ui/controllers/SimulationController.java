package com.fluidity.structure.ui.controllers;

import com.fluidity.structure.ui.ProgramState;
import javafx.fxml.FXML;

public class SimulationController extends Controller{

	@FXML
	public void onReturnToHomeButtonClick() {
		manager.loadScene(ProgramState.MAIN_MENU);
	}

	@Override
	void initialise() {

	}
}
