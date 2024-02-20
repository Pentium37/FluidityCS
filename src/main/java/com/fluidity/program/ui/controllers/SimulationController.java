package com.fluidity.program.ui.controllers;

import com.fluidity.program.simulation.FluidInput;
import com.fluidity.program.simulation.SimulationThreaded;
import com.fluidity.program.ui.MouseListener;
import com.fluidity.program.ui.ProgramState;
import com.fluidity.program.utilities.ExtraMath;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static java.lang.Math.hypot;

public class SimulationController extends Controller implements MouseListener {
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

	@FXML
	Canvas canvasX;
	private boolean mouseHeld;
	private List<FluidInput> sourceQueue;
	private Instant startAdd;
	private int[] previousCoords;
	private int CELL_LENGTH;
	private SimulationThreaded simulation;

	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
		Thread thread = new Thread(r);
		thread.setDaemon(true);
		return thread;
	});

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		this.viscosity = 0;
		this.flowspeed = 0;

		createListeners();
		sourceQueue = new ArrayList<>();
		startAdd = Instant.now();

		int IMAGE_WIDTH = 720;
		int IMAGE_HEIGHT = 480;
		CELL_LENGTH = 5;

		simulation = new SimulationThreaded(canvasX, this, IMAGE_WIDTH, IMAGE_HEIGHT, CELL_LENGTH);
		EXECUTOR.submit(simulation);
	}

	@Override
	@FXML
	public void mousePressed(MouseEvent e) {
		previousCoords = new int[] { (int) e.getX(), (int) e.getY() };
		mouseHeld = true;
		startAdd = Instant.now();
	}

	@Override
	@FXML
	public void mouseReleased() {
		mouseHeld = false;
	}

	@Override
	@FXML
	public synchronized void mouseDragged(MouseEvent e) {
		double velocityX = 0;
		double velocityY = 0;
		double dragScalar = 5.0;

		if (previousCoords != null) {
			velocityX = dragScalar * ((int) e.getX() - previousCoords[0]);
			velocityY = dragScalar * ((int) e.getY() - previousCoords[1]);
		}

		startAdd = Instant.now();
		previousCoords = new int[] { (int) e.getX(), (int) e.getY() };
		sourceQueue.add(new FluidInput(previousCoords[0], previousCoords[1], velocityX, velocityY,
				6.0 * hypot(velocityX, velocityY), CELL_LENGTH));
	}

	@Override
	public synchronized void consumeSources(Consumer<FluidInput> sourceConsumer) {
		if (mouseHeld) {
			long timeHeld = Duration.between(startAdd, Instant.now())
					.toMillis();
			startAdd = Instant.now();
			sourceQueue.add(new FluidInput(previousCoords[0], previousCoords[1], 0, 0, timeHeld * 20, CELL_LENGTH));
		}

		for (FluidInput source : sourceQueue) {
			sourceConsumer.accept(source);
		}

		sourceQueue.clear();
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
					simulation.setViscosity(viscosity);
				});
		flowspeedSlider.valueProperty()
				.addListener((obs, oldVal, newVal) -> {
					flowspeed = ExtraMath.roundToTwoDecimalPlaces(flowspeedSlider.getValue());
					flowspeedLabel.setText("Flow Speed: " + flowspeed);
					simulation.setDiffusionRate(flowspeed);
				});
	}
}
