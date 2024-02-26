package com.fluidity.program.ui.controllers;

import com.fluidity.program.simulation.FluidInput;
import com.fluidity.program.simulation.SimulationThreaded;
import com.fluidity.program.simulation.fluid.Fluid;
import com.fluidity.program.ui.FluidUIAction;
import com.fluidity.program.ui.MouseListener;
import com.fluidity.program.ui.ProgramState;
import com.fluidity.program.utilities.ExtraMath;
import com.fluidity.program.utilities.FileHandler;
import com.fluidity.program.utilities.Queue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static java.lang.Math.hypot;

public class SimulationController extends Controller implements MouseListener {
	@FXML
	Canvas canvasX;

	@FXML
	private CheckBox sensorCheckbox;
	private boolean sensorOn;

	@FXML
	private CheckBox addDensityCheckbox;
	private boolean addDensity;

	@FXML
	private Slider densitySlider;
	@FXML
	private Label densityLabel;
	public double densityFactor;

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

	private boolean mouseHeld;
	// Change to Queue
	private Queue<FluidInput> sourceQueue;
	private Instant startAdd;
	private int[] previousCoords;

	private SimulationThreaded simulation;
	private boolean simulationStarted;
	private Future<?> simulationFuture;

	private double viscosity, diffusionRate;
	private boolean dragFluid, addBarrier, removeBarrier;
	private int CELL_LENGTH;
	private boolean savingEnabled;
	private int ITERATIONS;
	private int FPS;


	@FXML
	private Button startRecordingButton;
	private boolean recordingStarted;
	private Queue<Queue<FluidInput>> recordingQueue;
	private String initialFluidStateOutput;

	private Map<FluidUIAction, KeyCode> keyBindMap;

	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
		Thread thread = new Thread(r);
		thread.setDaemon(true);
		return thread;
	});

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		this.viscosity = 0;
		this.diffusionRate = 0;
		startRecordingButton.setDisable(true);
		sourceQueue = new Queue<>();

		startAdd = Instant.now();
		recordingStarted = false;

		int IMAGE_WIDTH = 720;
		int IMAGE_HEIGHT = 480;

		keyBindMap = new EnumMap<>(FluidUIAction.class);
		simulation = new SimulationThreaded(canvasX, this, IMAGE_WIDTH, IMAGE_HEIGHT);

		viscositySlider.setDisable(true);
		diffusionRateSlider.setDisable(true);
		densitySlider.setDisable(true);

		createListeners();
		simulationStarted = false;
	}

	private void loadConfigurations() {
		List<String> configurations = FileHandler.readFile(SettingsController.configPath);
		for (String line : configurations) {

			String[] configuration = line.split(":", 2);
			if (configuration[1].equals("null")) {
				continue;
			}

			try {
				FluidUIAction action = FluidUIAction.getByPath(configuration[0]);
				keyBindMap.put(action, KeyCode.getKeyCode(configuration[1]));
			} catch (IllegalArgumentException e) {
				switch (configuration[0]) {
					case "saving" -> {
						savingEnabled = configuration[1].equals("true");
					}
					case "FPS" -> {
						FPS = Integer.parseInt(configuration[1]);
					}
					case "iterations" -> {
						ITERATIONS = Integer.parseInt(configuration[1]);
					}
					case "cell-size" -> {
						String[] split = configuration[1].split(",");
						CELL_LENGTH = Integer.parseInt(split[0]);
					}
				}
			}
		}
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
			sourceQueue.enqueue(new FluidInput(previousCoords[0], previousCoords[1], velocityX, velocityY,
					densityFactor * hypot(velocityX, velocityY), CELL_LENGTH));
		}
	}

	@Override
	public synchronized void consumeSources(Consumer<FluidInput> sourceConsumer) {
		if (mouseHeld) {
			long timeHeld = Duration.between(startAdd, Instant.now())
					.toMillis();
			startAdd = Instant.now();
			sourceQueue.enqueue(
					new FluidInput(previousCoords[0], previousCoords[1], 0, 0, timeHeld * densityFactor * (20.0 / 6),
							CELL_LENGTH));
		}

		if (recordingStarted) {
			recordingQueue.enqueue(sourceQueue.copy());
		}

		while (!sourceQueue.isEmpty()) {
			FluidInput inputSource = sourceQueue.dequeue();
			sourceConsumer.accept(inputSource);
		}
	}

	@FXML
	public void onSensorCheckBoxClick() {
		sensorOn = sensorCheckbox.isSelected();
	}

	@FXML
	public void onAddDensityCheckBoxClick() {
		addDensity = addDensityCheckbox.isSelected();
		if (addDensity) {
			densitySlider.setDisable(false);
			densityFactor = densitySlider.getValue();
		} else {
			densitySlider.setDisable(true);
			densityFactor = 0;
		}
	}

	// slap in some text boxes for the number quantities
	@FXML
	public void onReturnToHomeButtonClick() {
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

	@FXML
	private void onStartRecordingClick() {
		if (!recordingStarted) {
			initialiseRecording();
			recordingStarted = true;
			startRecordingButton.setText("Stop Recording");
		} else {
			recordingStarted = false;
			startRecordingButton.setText("Start Recording");
			saveRecordingsToFile();
		}
	}

	private void createListeners() {
		viscositySlider.valueProperty()
				.addListener((obs, oldVal, newVal) -> {
					validateInitialisation();
					viscosity = ExtraMath.roundToTwoDecimalPlaces(viscositySlider.getValue());
					viscosityLabel.setText("Viscosity: " + viscosity);
					simulation.setViscosity(viscosity);
				});

		diffusionRateSlider.valueProperty()
				.addListener((obs, oldVal, newVal) -> {
					validateInitialisation();
					diffusionRate = ExtraMath.roundToTwoDecimalPlaces(diffusionRateSlider.getValue());
					diffusionRateLabel.setText("Diffusion Rate: " + diffusionRate);
					simulation.setDiffusionRate(diffusionRate);
				});

		densitySlider.valueProperty()
				.addListener((obs, oldVal, newVal) -> {
					densityFactor = ExtraMath.roundToTwoDecimalPlaces(densitySlider.getValue());
					densityLabel.setText("Density: " + densityFactor);
				});

		containerType.getSelectionModel()
				.selectedIndexProperty()
				.addListener((observable, oldValue, newValue) -> {
					validateInitialisation();
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
					validateInitialisation();
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
							validateInitialisation();
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
		mouseAction.getSelectionModel()
				.select(0);
	}

	public void validateInitialisation() {
		if (!simulation.initialised) {
			loadConfigurations();
			simulation.setQuantities(CELL_LENGTH, ITERATIONS, FPS);
		}
	}

	private void startSimulation() {
		viscositySlider.setDisable(true);
		diffusionRateSlider.setDisable(true);
		startRecordingButton.setDisable(false);
		simulationFuture = EXECUTOR.submit(simulation);
		startSimulationButton.setText("Pause Simulation");
		simulationStarted = true;
	}

	private void endSimulation() {
		viscositySlider.setDisable(false);
		diffusionRateSlider.setDisable(false);
		startRecordingButton.setDisable(true);
		simulationStarted = false;
		simulationFuture.cancel(true);
		startSimulationButton.setText("Start Simulation");

		if (recordingStarted) {
			saveRecordingsToFile();
		}
	}

	private void saveRecordingsToFile() {
		recordingStarted = false;

		List<String> recordingInfo = FileHandler.readFile("recordings-info.txt");
		StringBuilder recordingInfoOutput = new StringBuilder();
		String recordingFileName = "recording-";
		String propertiesFileName = "properties-";

		boolean check = true;
		for (int i = 0; i < recordingInfo.size(); i++) {

			if (recordingInfo.get(i).equals("") && check) {
				recordingFileName += i + ".txt";
				propertiesFileName += i + ".txt";
				recordingInfo.add(i, recordingFileName);
				check = false;
			}

			recordingInfoOutput.append(recordingInfo.get(i))
					.append("\n");
		}

		FileHandler.writeLine("recordings-info.txt", recordingInfoOutput.toString());
		FileHandler.writeLine(recordingFileName, recordingQueue.toString());
		FileHandler.writeLine(propertiesFileName, initialFluidStateOutput);
	}

	private void initialiseRecording() {
		recordingQueue = new Queue<>();
		recordingStarted = true;
		createOutputForInitialFluidState();
		recordingQueue.setMAX_SIZE(10800); // 3 minutes of recording at 60 FPS
	}

	private void createOutputForInitialFluidState() {
		Fluid fluid = simulation.getFluid();
		//Add fluid type soon
		initialFluidStateOutput = Arrays.toString(fluid.dens) + "\n" + Arrays.toString(fluid.u) + "\n" + Arrays.toString(fluid.v);
	}
}
