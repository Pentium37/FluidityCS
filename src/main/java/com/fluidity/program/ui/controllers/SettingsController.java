package com.fluidity.program.ui.controllers;

import com.fluidity.program.ui.FluidUIAction;
import com.fluidity.program.ui.ProgramState;
import com.fluidity.program.utilities.FileHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.*;

public class SettingsController extends Controller implements Initializable {
	@FXML
	private Button primaryPauseKeyBindSetter;
	@FXML
	private Button secondaryPauseKeyBindSetter;
	@FXML
	private Button primaryStepForwardKeyBindSetter;
	@FXML
	private Button secondaryStepForwardKeyBindSetter;
	@FXML
	private Button primaryStepBackwardKeyBindSetter;
	@FXML
	private Button secondaryStepBackwardKeyBindSetter;
	private Map<FluidUIAction, KeyCode> keyBindMap;
	private Map<FluidUIAction, Button> buttonMap;
	private boolean setKeyBindFlag;
	private FluidUIAction recentlyPressedAction;
	private static final String primaryButtonColor = "#262626";
	private static final String secondaryButtonColor = "#313131";
	private static final String configPath = "configurations.txt";
	@FXML
	private Button fluidSavingSetter;
	private boolean savingEnabled;

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		// change later
		savingEnabled = false;

		keyBindMap = new EnumMap<>(FluidUIAction.class);

		buttonMap = new EnumMap<>(FluidUIAction.class);
		buttonMap.put(FluidUIAction.PRIMARY_PAUSE, primaryPauseKeyBindSetter);
		buttonMap.put(FluidUIAction.SECONDARY_PAUSE, secondaryPauseKeyBindSetter);
		buttonMap.put(FluidUIAction.PRIMARY_STEP_FORWARD, primaryStepForwardKeyBindSetter);
		buttonMap.put(FluidUIAction.SECONDARY_STEP_FORWARD, secondaryStepForwardKeyBindSetter);
		buttonMap.put(FluidUIAction.PRIMARY_STEP_BACKWARD, primaryStepBackwardKeyBindSetter);
		buttonMap.put(FluidUIAction.SECONDARY_STEP_BACKWARD, secondaryStepBackwardKeyBindSetter);

		loadKeyBindsFromConfigFile();
		setKeyBindFlag = false;
		recentlyPressedAction = null;
	}

	@FXML
	public void onReturnToHomeButtonClick() {
		if (setKeyBindFlag) {
			changeButtonColor(recentlyPressedAction, primaryButtonColor);
		}

		saveKeyBindsToConfigFile();
		manager.loadScene(ProgramState.MAIN_MENU);
	}

	// GRAPHICS SETTINGS
	@FXML
	public void onPrimaryPauseKeyBindSetterAction() {
		keyBindSetterAction(FluidUIAction.PRIMARY_PAUSE);
	}

	@FXML
	public void onSecondaryPauseKeyBindSetterAction() {
		keyBindSetterAction(FluidUIAction.SECONDARY_PAUSE);
	}

	@FXML
	public void onPrimaryStepForwardKeyBindSetterAction() {
		keyBindSetterAction(FluidUIAction.PRIMARY_STEP_FORWARD);
	}

	@FXML
	public void onSecondaryStepForwardKeyBindSetterAction() {
		keyBindSetterAction(FluidUIAction.SECONDARY_STEP_FORWARD);
	}

	@FXML
	public void onPrimaryStepBackwardKeyBindSetterAction() {
		keyBindSetterAction(FluidUIAction.PRIMARY_STEP_BACKWARD);
	}

	@FXML
	public void onSecondaryStepBackwardKeyBindSetterAction() {
		keyBindSetterAction(FluidUIAction.SECONDARY_STEP_BACKWARD);
	}

	public void keyBindSetterAction(FluidUIAction action) {
		resetPreviousListening();
		raiseKeyBindFlag(action);
		changeButtonColor(action, secondaryButtonColor);
	}

	public boolean resetPreviousListening() {
		if (setKeyBindFlag) {
			changeButtonColor(recentlyPressedAction, primaryButtonColor);
			return true;
		}
		return false;
	}

	public void raiseKeyBindFlag(FluidUIAction action) {
		setKeyBindFlag = true;
		recentlyPressedAction = action;
	}

	public void changeButtonColor(FluidUIAction action, String color) {
		buttonMap.get(action)
				.setStyle("-fx-background-color: " + color + ";");
	}

	@FXML
	public void onKeyPressed(KeyEvent keyEvent) {
		if (setKeyBindFlag) {
			keyBindMap.put(recentlyPressedAction, keyEvent.getCode());
			buttonMap.get(recentlyPressedAction)
					.setText(keyEvent.getCode()
							.getName()
							.toUpperCase());
			changeButtonColor(recentlyPressedAction, primaryButtonColor);
			setKeyBindFlag = false;
		}
	}

	private void saveKeyBindsToConfigFile() {
		StringBuilder output = new StringBuilder();
		for (FluidUIAction action : FluidUIAction.values()) {
			if (keyBindMap.get(action) != null) {
				output.append(action.getPath())
						.append(":")
						.append(keyBindMap.get(action)
								.getName())
						.append("\n");
			}
		}
		FileHandler.clearFile(configPath);
		FileHandler.writeLine(configPath, output.toString());
	}

	private void loadKeyBindsFromConfigFile() {
		List<String> configurations = FileHandler.readFile(configPath);
		for (String line : configurations) {

			String[] configuration = line.split(":", 2);
			if (configuration[1].equals("null")) {
				continue;
			}
			FluidUIAction action = FluidUIAction.getByPath(configuration[0]);
			buttonMap.get(action)
					.setText(configuration[1]);
		}
	}

	@FXML
	public void fluidSavingSetterAction() {
		savingEnabled = !savingEnabled;
		fluidSavingSetter.setText((savingEnabled) ? "Fluid Cache: Enabled" : "Fluid Cache: Disabled");
	}
}
