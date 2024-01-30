package com.fluidity.program.ui.controllers;

import com.fluidity.program.ui.FluidUIAction;
import com.fluidity.program.ui.ProgramState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

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

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		keyBindMap = new EnumMap<>(FluidUIAction.class);

		buttonMap = new EnumMap<>(FluidUIAction.class);
		buttonMap.put(FluidUIAction.PRIMARY_PAUSE, primaryPauseKeyBindSetter);
		buttonMap.put(FluidUIAction.SECONDARY_PAUSE, secondaryPauseKeyBindSetter);
		buttonMap.put(FluidUIAction.PRIMARY_STEP_FORWARD, primaryStepForwardKeyBindSetter);
		buttonMap.put(FluidUIAction.SECONDARY_STEP_FORWARD, secondaryStepForwardKeyBindSetter);
		buttonMap.put(FluidUIAction.PRIMARY_STEP_BACKWARD, primaryStepBackwardKeyBindSetter);
		buttonMap.put(FluidUIAction.SECONDARY_STEP_BACKWARD, secondaryStepBackwardKeyBindSetter);


		setKeyBindFlag = false;
		recentlyPressedAction = null;
	}

	@FXML
	public void onReturnToHomeButtonClick() {
		for (FluidUIAction action : FluidUIAction.values()) {
			System.out.println(keyBindMap.get(action));
		}
		manager.loadScene(ProgramState.MAIN_MENU);
	}

	// GRAPHICS SETTINGS
	@FXML
	public void onPrimaryPauseKeyBindSetterAction() {
		raiseKeyBindFlag(FluidUIAction.PRIMARY_PAUSE);
	}
	@FXML
	public void onSecondaryPauseKeyBindSetterAction() {
		raiseKeyBindFlag(FluidUIAction.SECONDARY_PAUSE);
	}
	@FXML
	public void onPrimaryStepForwardKeyBindSetterAction() {
		raiseKeyBindFlag(FluidUIAction.PRIMARY_STEP_FORWARD);
	}
	@FXML
	public void onSecondaryStepForwardKeyBindSetterAction() {
		raiseKeyBindFlag(FluidUIAction.SECONDARY_STEP_FORWARD);
	}
	@FXML
	public void onPrimaryStepBackwardKeyBindSetterAction() {
		raiseKeyBindFlag(FluidUIAction.PRIMARY_STEP_BACKWARD);
	}
	@FXML
	public void onSecondaryStepBackwardKeyBindSetterAction() {
		raiseKeyBindFlag(FluidUIAction.SECONDARY_STEP_BACKWARD);
	}

	public void raiseKeyBindFlag(FluidUIAction action) {
		setKeyBindFlag = true;
		recentlyPressedAction = action;
	}

	@FXML
	public void onKeyPressed(KeyEvent keyEvent) {
		if (setKeyBindFlag) {
			keyBindMap.put(recentlyPressedAction, keyEvent.getCode());
			buttonMap.get(recentlyPressedAction).setText(keyEvent.getText().toUpperCase());
			setKeyBindFlag = false;
		}
	}
}
