package com.fluidity.program.simulation;

import com.fluidity.program.simulation.fluid.BoxFluid;
import com.fluidity.program.simulation.fluid.Fluid;
import com.fluidity.program.simulation.fluid.TunnelFluid;
import com.fluidity.program.ui.MouseListener;
import com.fluidity.program.utilities.Deque;
import com.fluidity.program.utilities.ExtraMath;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class Simulation implements Runnable {
	private final Canvas canvas;
	private Label sensorLocationLabel;
	private Label sensorDensityLabel;
	private Label sensorHorizontalVelocityLabel;
	private Label sensorVerticalVelocityLabel;
	private final MouseListener mouseAdapter;
	private final int IMAGE_WIDTH;
	private final int IMAGE_HEIGHT;

	private int FLUID_WIDTH;
	private int FLUID_HEIGHT;
	private Fluid fluid;
	public boolean running, densityPlot, xVelocityPlot, yVelocityPlot, initialised;
	private int plotFactor;
	private int CELL_LENGTH;
	private int ITERATIONS;
	private double DESIRED_FPS;
	private double TPS;
	private boolean sensorOn;
	private int sensorX, sensorY;
	private int dequeueCounter;
	private boolean savingEnabled;
	private Deque<FluidState> rollbackMemory;

	public Simulation(Canvas canvas, MouseListener mouseAdapter, int IMAGE_WIDTH, int IMAGE_HEIGHT) {
		this.canvas = canvas;
		this.mouseAdapter = mouseAdapter;
		this.IMAGE_WIDTH = IMAGE_WIDTH;
		this.IMAGE_HEIGHT = IMAGE_HEIGHT;
		this.running = false;
		this.initialised = false;
	}

	public void initialiseSensorLabels(Label sensorLocationLabel, Label sensorDensityLabel,
			Label sensorHorizontalVelocityLabel, Label sensorVerticalVelocityLabel) {
		this.sensorLocationLabel = sensorLocationLabel;
		this.sensorDensityLabel = sensorDensityLabel;
		this.sensorHorizontalVelocityLabel = sensorHorizontalVelocityLabel;
		this.sensorVerticalVelocityLabel = sensorVerticalVelocityLabel;
	}

	@Override
	public void run() {
		this.running = true;
		this.FLUID_WIDTH = fluid.WIDTH;
		this.FLUID_HEIGHT = fluid.HEIGHT;

		final double TIME_PER_TICK = 1.0 / TPS;
		final double TIME_PER_FRAME = 1.0 / DESIRED_FPS;
		long lastTime = System.nanoTime();
		double unprocessedTime = 0;
		double frameTime = 0;

		dequeueCounter = 0;

		int tick = 0;
		while (running) {
			long now = System.nanoTime();
			long passedTime = now - lastTime;
			lastTime = now;
			unprocessedTime += passedTime / 1_000_000_000.0;
			frameTime += passedTime / 1_000_000_000.0;

			// Update fluid and handle inputs as many times as needed to catch up with the target TPS
			while (unprocessedTime > TIME_PER_TICK) {
				fluid.step(TIME_PER_TICK);
				addSourcesFromUI(fluid);
				unprocessedTime -= TIME_PER_TICK;
			}

			// Render and save if it's time for a new frame
			if (frameTime >= TIME_PER_FRAME) {
				tick++;
				if (tick == DESIRED_FPS / 5) {
					tick = 0;

					if (savingEnabled) {
						saveFluidIntoRollback();
					}
					if (sensorOn) {
						Platform.runLater(this::updateSensorLabels);
					}
				}
				render();
				frameTime = 0;
			}

			// Sleep to cap the rendering FPS
			long sleepTime = (long) ((TIME_PER_FRAME - frameTime) * 1_000_000_000);
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

	public void render(double[] plotChoice) {
		int[] buffer = new int[IMAGE_WIDTH * IMAGE_HEIGHT];

		for (int y = 0; y < IMAGE_HEIGHT; y++) {
			for (int x = 0; x < IMAGE_WIDTH; x++) {
				double num = Math.abs(plotChoice[fluid.index(x / CELL_LENGTH, y / CELL_LENGTH)] * this.plotFactor);
				int color = (int) (num > 255 ? 255 : num);
				int rgb = (255 << 24) | (color << 16) | (color << 8) | color;// Set alpha channel to fully opaque

				if (fluid.barrierPresent[x / CELL_LENGTH][y / CELL_LENGTH]) {
					rgb = 0xFF99DDFF;
				}

				buffer[x + y * IMAGE_WIDTH] = rgb;
			}
		}

		if (sensorOn) {
			for (int x = 0; x < CELL_LENGTH; x++) {
				for (int y = 0; y < CELL_LENGTH; y++) {
					buffer[sensorX * CELL_LENGTH + x + (sensorY * CELL_LENGTH + y) * IMAGE_WIDTH] = 0xFFFF3333;
				}
			}
		}

		PixelWriter writer = canvas.getGraphicsContext2D()
				.getPixelWriter();
		writer.setPixels(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, PixelFormat.getIntArgbInstance(), buffer, 0, IMAGE_WIDTH);
	}

	private void addSourcesFromUI(Fluid fluid) {
		synchronized (fluid) {
			mouseAdapter.consumeSources(fluidInput -> {
				fluid.horizontalVelocity[fluid.index(fluidInput.x(), fluidInput.y())] += fluidInput.forceX();
				fluid.verticalVelocity[fluid.index(fluidInput.x(), fluidInput.y())] += fluidInput.forceY();
				fluid.horizontalVelocity[fluid.index(fluidInput.x() + 1, fluidInput.y())] += fluidInput.forceX();
				fluid.verticalVelocity[fluid.index(fluidInput.x() + 1, fluidInput.y())] += fluidInput.forceY();
				fluid.horizontalVelocity[fluid.index(fluidInput.x() - 1, fluidInput.y())] += fluidInput.forceX();
				fluid.verticalVelocity[fluid.index(fluidInput.x() - 1, fluidInput.y())] += fluidInput.forceY();
				fluid.horizontalVelocity[fluid.index(fluidInput.x(), fluidInput.y() + 1)] += fluidInput.forceX();
				fluid.verticalVelocity[fluid.index(fluidInput.x(), fluidInput.y() + 1)] += fluidInput.forceY();
				fluid.horizontalVelocity[fluid.index(fluidInput.x(), fluidInput.y() - 1)] += fluidInput.forceX();
				fluid.verticalVelocity[fluid.index(fluidInput.x(), fluidInput.y() - 1)] += fluidInput.forceY();
				fluid.density[fluid.index(fluidInput.x(), fluidInput.y())] += fluidInput.density();
			});
		}
	}

	public void setViscosity(double viscosity) {
		fluid.viscosity = viscosity;
	}

	public void setDiffusionRate(double diffusionRate) {
		fluid.diffusionRate = diffusionRate;
	}

	public Fluid getFluid() {
		return this.fluid;
	}

	public void setTunnelBoundaries() {
		this.fluid =
				new TunnelFluid(IMAGE_WIDTH / CELL_LENGTH, IMAGE_HEIGHT / CELL_LENGTH, CELL_LENGTH, 0, 0, 4, 3);
	}

	public void setBoxBoundaries() {
		this.fluid = new BoxFluid(IMAGE_WIDTH / CELL_LENGTH, IMAGE_HEIGHT / CELL_LENGTH, CELL_LENGTH, 0, 0, 4);
	}

	public void setQuantities(int CELL_LENGTH, int ITERATIONS, int FPS, boolean savingEnabled) {
		this.CELL_LENGTH = CELL_LENGTH;
		this.ITERATIONS = ITERATIONS;
		this.TPS = FPS;
		this.DESIRED_FPS = FPS;
		this.initialised = true;
		this.savingEnabled = savingEnabled;

		if (savingEnabled) {
			this.rollbackMemory = new Deque<>(100);
		}
	}

	public void setPlotType(String plotType) {
		densityPlot = false;
		xVelocityPlot = false;
		yVelocityPlot = false;
		if (plotType.equals("Density")) {
			plotFactor = 1;
			densityPlot = true;
		} else if (plotType.equals("x-Velocity")) {
			plotFactor = 1000;
			xVelocityPlot = true;
		} else if (plotType.equals("y-Velocity")) {
			plotFactor = 1000;
			yVelocityPlot = true;
		}
	}

	public void render() {
		if (densityPlot) {
			Platform.runLater(() -> render(fluid.density.clone())); // Render with the most recent fluid density
		} else if (xVelocityPlot) {
			Platform.runLater(
					() -> render(fluid.horizontalVelocity.clone())); // Render with the most recent fluid density
		} else if (yVelocityPlot) {
			Platform.runLater(
					() -> render(fluid.verticalVelocity.clone())); // Render with the most recent fluid density
		}
	}

	public void rollback() {
		if (!rollbackMemory.isEmpty()) {
			FluidState state = rollbackMemory.pop();
			fluid.density = state.density;
			fluid.horizontalVelocity = state.horizontalVelocity;
			fluid.verticalVelocity = state.verticalVeloctiy;
			dequeueCounter++;
		}
		System.out.println("Backtrack at time:" + System.currentTimeMillis());
		render();
		if (sensorOn) {
			updateSensorLabels();
		}
	}

	public void stepForward() {
		if (dequeueCounter == 0) {
			for (int i = 0; i < TPS / 5; i++) {
				fluid.step(1.0 / TPS);
			}
			if (savingEnabled) {
				saveFluidIntoRollback();
			}
		} else if (!rollbackMemory.isFull()) {
			rollbackMemory.retrievePush();

			FluidState state = rollbackMemory.peekStack();
			fluid.density = state.density;
			fluid.horizontalVelocity = state.horizontalVelocity;
			fluid.verticalVelocity = state.verticalVeloctiy;

			dequeueCounter--;
		}
		render();
		if (sensorOn) {
			updateSensorLabels();
		}
	}

	public void saveFluidIntoRollback() {
		if (!rollbackMemory.isFull()) {
			rollbackMemory.enqueue(new FluidState(fluid.density.clone(), fluid.horizontalVelocity.clone(),
					fluid.verticalVelocity.clone()));
		} else {
			rollbackMemory.dequeue();
			rollbackMemory.enqueue(new FluidState(fluid.density.clone(), fluid.horizontalVelocity.clone(),
					fluid.verticalVelocity.clone()));
		}
	}

	public void addSensor(int x, int y) {
		this.sensorOn = true;
		this.sensorX = x;
		this.sensorY = y;
	}

	public void removeSensor() {
		this.sensorOn = false;
	}

	public void updateSensorLabels() {
		this.sensorLocationLabel.setText("Location: (" + sensorX + "," + sensorY + ")");
		this.sensorDensityLabel.setText(
				"Density: " + ExtraMath.roundToTwoDecimalPlaces(fluid.density[fluid.index(sensorX, sensorY)]));
		this.sensorHorizontalVelocityLabel.setText("Horizontal Velocity: " + ExtraMath.roundToFourDecimalPlaces(
				fluid.horizontalVelocity[fluid.index(sensorX, sensorY)]));
		this.sensorVerticalVelocityLabel.setText("Vertical Velocity: " + ExtraMath.roundToFourDecimalPlaces(
				fluid.verticalVelocity[fluid.index(sensorX, sensorY)]));
	}

	public void addBarrier(int x, int y) {
		int baseX = (x / 2) * 2;
		int baseY = (y / 2) * 2;
		fluid.barrierPresent[baseX][baseY] = true;
		fluid.barrierPresent[baseX + 1][baseY] = true;
		fluid.barrierPresent[baseX][baseY + 1] = true;
		fluid.barrierPresent[baseX + 1][baseY + 1] = true;
		System.out.println(baseX + " " + baseY);
		render();
	}

	public void removeBarrier(int x, int y) {
		int baseX = (x / 2) * 2;
		int baseY = (y / 2) * 2;
		fluid.barrierPresent[baseX][baseY] = false;
		fluid.barrierPresent[baseX + 1][baseY] = false;
		fluid.barrierPresent[baseX][baseY + 1] = false;
		fluid.barrierPresent[baseX + 1][baseY + 1] = false;
		System.out.println(baseX + " " + baseY);
		render();
	}

	public void clearBarriers() {
		fluid.barrierPresent = new boolean[FLUID_WIDTH][FLUID_HEIGHT];
	}
}