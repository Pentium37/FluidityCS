package com.fluidity.program.ui.controllers;

import com.fluidity.program.ui.FluidUIAction;
import com.fluidity.program.ui.GraphicsHandler;
import com.fluidity.program.ui.ProgramState;
import com.fluidity.program.utilities.FileHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.*;

public class SettingsController extends Controller {
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
	public static final String configPath = "configurations.txt";
	@FXML
	private Button fluidSavingSetter;
	@FXML
	private Button iterationsSetter;
	@FXML
	private Button cellSizeSetter;
	@FXML
	private Button fpsSetter;
	@FXML
	private TabPane settingsPane;
	GraphicsHandler graphicsHandler;
	private boolean savingEnabled;
	private int iterations;
	private int[] cellSize;
	private int FPS;

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		settingsPane.addEventFilter(KeyEvent.ANY, keyEvent -> {
			if (keyEvent.getCode()
					.isArrowKey()) {
				onKeyPressed(keyEvent);
				keyEvent.consume();
			}
		});

		keyBindMap = new EnumMap<>(FluidUIAction.class);

		buttonMap = new EnumMap<>(FluidUIAction.class);
		buttonMap.put(FluidUIAction.PRIMARY_PAUSE, primaryPauseKeyBindSetter);
		buttonMap.put(FluidUIAction.SECONDARY_PAUSE, secondaryPauseKeyBindSetter);
		buttonMap.put(FluidUIAction.PRIMARY_STEP_FORWARD, primaryStepForwardKeyBindSetter);
		buttonMap.put(FluidUIAction.SECONDARY_STEP_FORWARD, secondaryStepForwardKeyBindSetter);
		buttonMap.put(FluidUIAction.PRIMARY_STEP_BACKWARD, primaryStepBackwardKeyBindSetter);
		buttonMap.put(FluidUIAction.SECONDARY_STEP_BACKWARD, secondaryStepBackwardKeyBindSetter);

		loadConfigurations();
		setKeyBindFlag = false;
		recentlyPressedAction = null;
	}

	@FXML
	public void onReturnToHomeButtonClick() {
		if (setKeyBindFlag) {
			changeButtonColor(recentlyPressedAction, primaryButtonColor);
		}

		saveConfigurations();
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

	private void saveConfigurations() {
		StringBuilder output = new StringBuilder();

		output.append(getFormattedKeyBindConfigurations());
		output.append(getFormattedGraphicsConfigurations());

		FileHandler.clearFile(configPath);
		FileHandler.writeLine(configPath, output.toString());
	}

	private void loadConfigurations() {
		List<String> configurations = FileHandler.readFile(configPath);
		for (String line : configurations) {

			String[] configuration = line.split(":", 2);
			if (configuration[1].equals("null")) {
				continue;
			}

			try {
				FluidUIAction action = FluidUIAction.getByPath(configuration[0]);
				keyBindMap.put(action, KeyCode.getKeyCode(configuration[1]));
				buttonMap.get(action)
						.setText(configuration[1].toUpperCase());
			} catch (IllegalArgumentException e) {
				switch (configuration[0]) {
					case "saving" -> {
						savingEnabled = configuration[1].equals("true");
						fluidSavingSetter.setText("Fluid Saving: " + ((savingEnabled) ? "Enabled" : "Disabled"));
					}
					case "FPS" -> {
						FPS = Integer.parseInt(configuration[1]);
						fpsSetter.setText("FPS: " + FPS);
					}
					case "iterations" -> {
						iterations = Integer.parseInt(configuration[1]);
						iterationsSetter.setText("Iterations: " + iterations);
					}
					case "cell-size" -> {
						String[] split = configuration[1].split(",");
						cellSize = new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]) };
						cellSizeSetter.setText("Cell Size: (" + cellSize[0] + "," + cellSize[1] + ")");
					}
				}
			}
		}
		graphicsHandler = new GraphicsHandler(FPS, cellSize, iterations);
	}

	private String getFormattedKeyBindConfigurations() {
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
		return output.toString();
	}

	private String getFormattedGraphicsConfigurations() {
		return "saving:" + savingEnabled + "\n" + "FPS:" + FPS + "\n" + "iterations:" + iterations + "\n"
				+ "cell-size:" + cellSize[0] + "," + cellSize[1] + "\n";
	}

	@FXML
	public void fluidSavingSetterAction() {
		savingEnabled = !savingEnabled;
		fluidSavingSetter.setText((savingEnabled) ? "Fluid Saving: Enabled" : "Fluid Saving: Disabled");
	}

	@FXML
	public void cellSizeSetterAction() {
		graphicsHandler.shiftCellSize();
		cellSize = graphicsHandler.getCurrentCellSize();
		cellSizeSetter.setText("Cell Size: (" + cellSize[0] + "," + cellSize[1] + ")");
	}

	@FXML
	public void iterationsSetterAction() {
		graphicsHandler.shiftIterations();
		iterations = graphicsHandler.getCurrentIterations();
		iterationsSetter.setText("Iterations: " + iterations);
	}

	@FXML
	public void fpsSetterAction() {
		graphicsHandler.shiftFPS();
		FPS = graphicsHandler.getCurrentFPS();
		fpsSetter.setText("FPS: " + FPS);
	}

}