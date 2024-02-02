package com.fluidity.program.ui.controllers;

import com.fluidity.program.ui.ProgramState;
import com.fluidity.program.utilities.ExtraMath;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.net.URL;
import java.util.ResourceBundle;

public class SimulationController extends Controller implements Initializable {
	@FXML
	private CheckBox flowlinesCheckBox;
	@FXML
	private CheckBox sensorCheckbox;
	private boolean flowlinesOn;
	private boolean sensorOn;
	@FXML
	private Slider viscositySlider;
	@FXML
	private Label viscosityLabel;
	@FXML
	private Slider flowspeedSlider;
	@FXML
	private Label flowspeedLabel;
	@FXML
	private ChoiceBox<String> plotChoice;
	private double viscosity;
	private double flowspeed;

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		this.viscosity = 0;
		this.flowspeed = 0;
		createListeners();
	}

	@FXML
	public void onFlowLinesCheckBoxClick() {
		flowlinesOn = flowlinesCheckBox.isSelected();
	}

	@FXML
	public void onSensorCheckBoxClick() {
		sensorOn = sensorCheckbox.isSelected();
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

	private void createListeners() {
		viscositySlider.valueProperty()
				.addListener((obs, oldVal, newVal) -> {
					viscosity = ExtraMath.roundToTwoDecimalPlaces(viscositySlider.getValue());
					viscosityLabel.setText("Viscosity: " + viscosity);
				});
		flowspeedSlider.valueProperty()
				.addListener((obs, oldVal, newVal) -> {
					flowspeed = ExtraMath.roundToTwoDecimalPlaces(flowspeedSlider.getValue());
					flowspeedLabel.setText("Flow Speed: " + flowspeed);
				});
	}
}
