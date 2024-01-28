package com.fluidity.structure.controllers;

import com.fluidity.structure.ProgramState;
import javafx.fxml.FXML;

public class RecordingsController extends Controller{
	@FXML
	public void onReturnToHomeButtonClick() {
		manager.loadScene(ProgramState.MAIN_MENU);
	}
	@Override
	void initialise() {

	}
}
