package com.fluidity.program.ui.controllers;

import com.fluidity.program.simulation.FluidInput;
import com.fluidity.program.simulation.SimulationThreaded;
import com.fluidity.program.ui.MouseListener;
import com.fluidity.program.ui.ProgramState;
import com.fluidity.program.utilities.ExtraMath;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static java.lang.Math.hypot;

public class SimulationController extends Controller implements MouseListener {
	@FXML
	private CheckBox sensorCheckbox;
	private boolean sensorOn;
	@FXML
	private Slider viscositySlider;
	@FXML
	private Label viscosityLabel;
	@FXML
	private Slider diffusionRateSlider;
	@FXML
	private Label diffusionRateLabel;
	@FXML
	private Button startSimulationButton;
	@FXML
	private ChoiceBox<String> containerType;
	@FXML
	private ChoiceBox<String> plotChoice;
	@FXML
	private ChoiceBox<String> mouseAction;
	private double viscosity, diffusionRate;
	private boolean dragFluid, addBarrier, removeBarrier;
	@FXML
	Canvas canvasX;
	private boolean mouseHeld;
	private List<FluidInput> sourceQueue;
	private Instant startAdd;
	private int[] previousCoords;
	private int CELL_LENGTH;
	private SimulationThreaded simulation;
	private boolean simulationStarted;
	private Future<?> simulationFuture;

	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
		Thread thread = new Thread(r);
		thread.setDaemon(true);
		return thread;
	});

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		this.viscosity = 0;
		this.diffusionRate = 0;
		sourceQueue = new ArrayList<>();
		startAdd = Instant.now();

		int IMAGE_WIDTH = 720;
		int IMAGE_HEIGHT = 480;
		CELL_LENGTH = 5;

		viscositySlider.setDisable(true);
		diffusionRateSlider.setDisable(true);
		simulation = new SimulationThreaded(canvasX, this, IMAGE_WIDTH, IMAGE_HEIGHT, CELL_LENGTH);
		createListeners();
		simulationStarted = false;
	}

	@Override
	@FXML
	public void mousePressed(MouseEvent e) {
		if (dragFluid) {
			previousCoords = new int[] { (int) e.getX(), (int) e.getY() };
			mouseHeld = true;
			startAdd = Instant.now();
		}
	}

	@Override
	@FXML
	public void mouseReleased() {
		if (dragFluid) {
			mouseHeld = false;
		}
	}

	@Override
	@FXML
	public synchronized void mouseDragged(MouseEvent e) {
		if (dragFluid) {
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
	public void onSensorCheckBoxClick() {
		sensorOn = sensorCheckbox.isSelected();
	}

	// slap in some text boxes for the number quantities
	@FXML
	public void onReturnToHomeButtonClick() {
		endSimulation();
		manager.loadScene(ProgramState.MAIN_MENU);
	}

	@FXML
	private void onGoToSettingsClick() {
		endSimulation();
		manager.loadScene(ProgramState.SETTINGS);
	}

	@FXML
	private void onStartSimulationClick() {
		if (!simulationStarted) {
			startSimulation();
		} else {
			endSimulation();
		}
	}

	private void createListeners() {
		viscositySlider.valueProperty()
				.addListener((obs, oldVal, newVal) -> {
					viscosity = ExtraMath.roundToTwoDecimalPlaces(viscositySlider.getValue());
					viscosityLabel.setText("Viscosity: " + viscosity);
					simulation.setViscosity(viscosity);
				});
		diffusionRateSlider.valueProperty()
				.addListener((obs, oldVal, newVal) -> {
					diffusionRate = ExtraMath.roundToTwoDecimalPlaces(diffusionRateSlider.getValue());
					diffusionRateLabel.setText("Flow Speed: " + diffusionRate);
					simulation.setDiffusionRate(diffusionRate);
				});

		containerType.getSelectionModel()
				.selectedIndexProperty()
				.addListener((observable, oldValue, newValue) -> {
					// Get the selected index
					int selectedIndex = newValue.intValue();

					// Get the selected item based on the index
					String selectedContainerType = containerType.getItems()
							.get(selectedIndex);

					// Check the selected item and perform actions accordingly
					switch (selectedContainerType) {
						case "Box" -> {
							// Handle selection of "Box"
							viscositySlider.setValue(0);
							diffusionRateSlider.setValue(0);
							if (!simulation.running) {
								viscositySlider.setDisable(false);
								diffusionRateSlider.setDisable(false);
							}
							simulation.setBoxBoundaries();
						}
						case "Tunnel" -> {
							// Handle selection of "Tunnel"
							viscositySlider.setValue(0);
							diffusionRateSlider.setValue(0);
							if (!simulation.running) {
								viscositySlider.setDisable(false);
								diffusionRateSlider.setDisable(false);
							}
							simulation.setTunnelBoundaries();
						}
					}
				});

		plotChoice.getSelectionModel()
				.selectedIndexProperty()
				.addListener((observable, oldValue, newValue) -> {
					// Get the selected index
					int selectedIndex = newValue.intValue();

					// Get the selected item based on the index
					String selectedContainerType = plotChoice.getItems()
							.get(selectedIndex);

					// Check the selected item and perform actions accordingly
					switch (selectedContainerType) {
						case "Density", "x-Velocity", "y-Velocity" -> simulation.setPlotType(selectedContainerType);
					}
				});

		mouseAction.getSelectionModel()
				.selectedIndexProperty()
				.addListener((observable, oldValue, newValue) -> {
							// Get the selected index
							int selectedIndex = newValue.intValue();

							// Get the selected item based on the index
							String selectedContainerType = mouseAction.getItems()
									.get(selectedIndex);

							// Check the selected item and perform actions accordingly
							switch (selectedContainerType) {
								case "Drag Fluid" -> {
									dragFluid = true;
									addBarrier = false;
									removeBarrier = false;
								}

								case "Add Barrier" -> {
									dragFluid = false;
									addBarrier = true;
									removeBarrier = false;
								}

								case "Remove Barrier" -> {
									dragFluid = false;
									addBarrier = false;
									removeBarrier = true;
								}
							}
						}

				);
		mouseAction.getSelectionModel().select(0);
	}

	private void startSimulation() {
		viscositySlider.setDisable(true);
		diffusionRateSlider.setDisable(true);
		simulationFuture = EXECUTOR.submit(simulation);
		startSimulationButton.setText("Pause Simulation");
		simulationStarted = true;
	}

	private void endSimulation() {
		viscositySlider.setDisable(false);
		diffusionRateSlider.setDisable(false);
		simulationFuture.cancel(true);
		createListeners();
		startSimulationButton.setText("Start Simulation");
	}

}
