package com.fluidity.program.ui.controllers;

import com.fluidity.program.ui.ProgramState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.util.converter.NumberStringConverter;

import java.net.URL;
import java.util.ResourceBundle;

public class SimulationController extends Controller implements Initializable {
	@FXML
	private CheckBox flowlinesCheckBox;
	@FXML
	private CheckBox sensorCheckbox;
	@FXML
	private Slider viscositySlider;
	@FXML
	private Slider flowspeedSlider;
	@FXML
	private ChoiceBox<String> plotChoice;
	private double viscosity;

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		viscosity = 0;
	}

	// slap in some text boxes for the number quantities
	@FXML
	public void onReturnToHomeButtonClick() {
		manager.loadScene(ProgramState.MAIN_MENU);
	}

	@FXML
	private void onGoToSettingsClick() {
		manager.loadScene(ProgramState.SETTINGS);
	}
}
