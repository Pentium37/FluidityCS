package com.fluidity.program.ui.controllers;

import com.fluidity.program.simulation.FluidInput;
import com.fluidity.program.simulation.Simulation;
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
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

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
	Canvas canvas;

	@FXML
	private Button addSensorButton;
	private boolean sensorListeningOn;
	@FXML
	private Label sensorLocationLabel;
	@FXML
	private Label sensorDensityLabel;
	@FXML
	private Label sensorHorizontalVelocityLabel;
	@FXML
	private Label sensorVerticalVelocityLabel;

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
	private Queue<FluidInput> sourceQueue;
	private Instant startAdd;
	private int[] previousCoords;

	private Simulation simulation;
	private boolean simulationStarted;
	private Future<?> simulationFuture;

	private double viscosity, diffusionRate;
	private boolean dragFluid, addBarrier, removeBarrier;
	private int CELL_LENGTH;
	private boolean savingEnabled;
	private int ITERATIONS;
	private int FPS;

	@FXML
	private Button clearBarriersButton;

	private Map<KeyCode, FluidUIAction> keyBindMap;
	@FXML
	private Button rollback;
	@FXML
	private Button stepForward;

	@FXML
	AnchorPane simulationPane;

	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
		Thread thread = new Thread(r);
		thread.setDaemon(true);
		return thread;
	});

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		setEventFilters();
		this.viscosity = 0;
		this.diffusionRate = 0;
		clearBarriersButton.setDisable(true);
		addSensorButton.setDisable(true);
		sourceQueue = new Queue<>();

		startAdd = Instant.now();

		int IMAGE_WIDTH = 720;
		int IMAGE_HEIGHT = 480;

		keyBindMap = new HashMap<KeyCode, FluidUIAction>();
		simulation = new Simulation(canvas, this, IMAGE_WIDTH, IMAGE_HEIGHT);
		simulation.initialiseSensorLabels(sensorLocationLabel, sensorDensityLabel, sensorHorizontalVelocityLabel,
				sensorVerticalVelocityLabel);

		viscositySlider.setDisable(true);
		diffusionRateSlider.setDisable(true);
		densitySlider.setDisable(true);
		rollback.setDisable(true);
		stepForward.setDisable(true);

		createListeners();
		simulationStarted = false;
	}

	public void setEventFilters() {
		simulationPane.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {
			if (keyEvent.getCode()
					.isArrowKey() || keyEvent.getCode() == KeyCode.SPACE) {
				onKeyPressed(keyEvent);
				keyEvent.consume();
			}
		});
		startSimulationButton.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {
			if (keyEvent.getCode()
					.isArrowKey() || keyEvent.getCode() == KeyCode.SPACE) {
				keyEvent.consume();
			}
		});
		clearBarriersButton.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {
			if (keyEvent.getCode()
					.isArrowKey() || keyEvent.getCode() == KeyCode.SPACE) {
				keyEvent.consume();
			}
		});
		addDensityCheckbox.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {
			if (keyEvent.getCode()
					.isArrowKey() || keyEvent.getCode() == KeyCode.SPACE) {
				keyEvent.consume();
			}
		});
		densitySlider.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {
			if (keyEvent.getCode()
					.isArrowKey() || keyEvent.getCode() == KeyCode.SPACE) {
				keyEvent.consume();
			}
		});
		plotChoice.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {
			if (keyEvent.getCode()
					.isArrowKey() || keyEvent.getCode() == KeyCode.SPACE) {
				keyEvent.consume();
			}
		});
		containerType.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {
			if (keyEvent.getCode()
					.isArrowKey() || keyEvent.getCode() == KeyCode.SPACE) {
				keyEvent.consume();
			}
		});
		mouseAction.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {
			if (keyEvent.getCode()
					.isArrowKey() || keyEvent.getCode() == KeyCode.SPACE) {
				keyEvent.consume();
			}
		});
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

								case "Add barrier" -> {
									dragFluid = false;
									addBarrier = true;
									removeBarrier = false;
								}

								case "Delete barrier" -> {
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

	private void loadConfigurations() {
		List<String> configurations = FileHandler.readFile(SettingsController.configPath);
		for (String line : configurations) {

			String[] configuration = line.split(":", 2);
			if (configuration[1].equals("null")) {
				continue;
			}

			try {
				FluidUIAction action = FluidUIAction.getByPath(configuration[0]);
				keyBindMap.put(KeyCode.getKeyCode(configuration[1]), action);
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
		if (sensorListeningOn) {
			sensorListeningOn = false;
			addSensorButton.setText("Clear Sensor");
			simulation.addSensor((int) e.getX() / CELL_LENGTH, (int) e.getY() / CELL_LENGTH);
			System.out.println(
					"Sensor added at: " + (int) e.getX() / CELL_LENGTH + ", " + (int) e.getY() / CELL_LENGTH);
		} else if (dragFluid && simulationStarted) {
			previousCoords = new int[] { (int) e.getX(), (int) e.getY() };
			mouseHeld = true;
			startAdd = Instant.now();
		} else if (addBarrier && simulation.initialised) {
			simulation.addBarrier((int) e.getX() / CELL_LENGTH, (int) e.getY() / CELL_LENGTH);
		} else if (removeBarrier && simulation.initialised) {
			simulation.removeBarrier((int) e.getX() / CELL_LENGTH, (int) e.getY() / CELL_LENGTH);
		}
	}

	@Override
	@FXML
	public void mouseReleased() {
		if (dragFluid && simulationStarted) {
			mouseHeld = false;
		}
	}

	@Override
	@FXML
	public synchronized void mouseDragged(MouseEvent e) {
		if (dragFluid && simulationStarted) {
			double velocityX = 0;
			double velocityY = 0;
			double dragScalar = 2.0;

			if (previousCoords != null) {
				velocityX = dragScalar * ((int) e.getX() - previousCoords[0]);
				velocityY = dragScalar * ((int) e.getY() - previousCoords[1]);
			}

			startAdd = Instant.now();
			previousCoords = new int[] { (int) e.getX(), (int) e.getY() };
			sourceQueue.enqueue(
					new FluidInput(previousCoords[0] / CELL_LENGTH, previousCoords[1] / CELL_LENGTH, velocityX,
							velocityY, densityFactor * hypot(velocityX, velocityY)));
		}
	}

	@Override
	public synchronized void consumeSources(Consumer<FluidInput> sourceConsumer) {
		if (mouseHeld) {
			long timeHeld = Duration.between(startAdd, Instant.now())
					.toMillis();
			startAdd = Instant.now();
			sourceQueue.enqueue(new FluidInput(previousCoords[0] / CELL_LENGTH, previousCoords[1] / CELL_LENGTH, 0, 0,
					timeHeld * densityFactor * (20.0 / 6)));
		}

		while (!sourceQueue.isEmpty()) {
			FluidInput inputSource = sourceQueue.dequeue();
			sourceConsumer.accept(inputSource);
		}
	}

	public void validateInitialisation() {
		if (!simulation.initialised) {
			loadConfigurations();
			simulation.setQuantities(CELL_LENGTH, ITERATIONS, FPS, savingEnabled);
		}
	}

	private void startSimulation() {
		viscositySlider.setDisable(true);
		diffusionRateSlider.setDisable(true);
		clearBarriersButton.setDisable(false);
		addSensorButton.setDisable(false);
		rollback.setDisable(true);
		stepForward.setDisable(true);

		simulationFuture = EXECUTOR.submit(simulation);
		startSimulationButton.setText("Pause Simulation");
		simulationStarted = true;
	}

	private void endSimulation() {
		viscositySlider.setDisable(false);
		diffusionRateSlider.setDisable(false);
		clearBarriersButton.setDisable(true);
		rollback.setDisable(false);
		stepForward.setDisable(false);

		simulationStarted = false;
		simulationFuture.cancel(true);
		startSimulationButton.setText("Start Simulation");
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
	public void onAddSensorClick() {
		if (addSensorButton.getText()
				.equals("Add Sensor")) {
			addSensorButton.setText("Click a fluid location!");
			sensorListeningOn = true;
		} else {
			addSensorButton.setText("Add Sensor");
			simulation.removeSensor();
		}
	}

	@FXML
	public void onKeyPressed(KeyEvent keyEvent) {
		if (keyBindMap.containsKey(keyEvent.getCode())) {
			if (keyBindMap.get(keyEvent.getCode()) == FluidUIAction.PRIMARY_PAUSE
					|| keyBindMap.get(keyEvent.getCode()) == FluidUIAction.SECONDARY_PAUSE) {
				if (!simulationStarted) {
					startSimulation();
				} else {
					endSimulation();
				}
			} else if (keyBindMap.get(keyEvent.getCode()) == FluidUIAction.PRIMARY_STEP_BACKWARD
					|| keyBindMap.get(keyEvent.getCode()) == FluidUIAction.SECONDARY_STEP_BACKWARD) {
				if (savingEnabled && !simulationStarted) {
					simulation.rollback();
				}
			} else if (keyBindMap.get(keyEvent.getCode()) == FluidUIAction.PRIMARY_STEP_FORWARD
					|| keyBindMap.get(keyEvent.getCode()) == FluidUIAction.SECONDARY_STEP_FORWARD) {
				if (savingEnabled && !simulationStarted) {
					simulation.stepForward();
				}
			}
		}
	}

	@FXML
	public void onStepForwardClick() {
		if (savingEnabled && !simulationStarted) {
			simulation.stepForward();
		}
	}

	@FXML
	public void onRollbackClick() {
		if (savingEnabled && !simulationStarted) {
			simulation.rollback();
		}
	}

	@FXML
	public void onClearBarriersClick() {
		simulation.clearBarriers();
	}
}
