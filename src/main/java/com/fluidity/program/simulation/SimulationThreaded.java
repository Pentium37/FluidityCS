package com.fluidity.program.simulation;

import com.fluidity.program.simulation.fluid.BoxFluid;
import com.fluidity.program.simulation.fluid.Fluid;
import com.fluidity.program.simulation.fluid.TunnelFluid;
import com.fluidity.program.ui.MouseListener;
import com.fluidity.program.utilities.Deque;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;

public class SimulationThreaded implements Runnable {
	private final Canvas canvas;
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

	private Deque<FluidState> rollbackMemory;

	public SimulationThreaded(Canvas canvas, MouseListener mouseAdapter, int IMAGE_WIDTH, int IMAGE_HEIGHT) {
		this.canvas = canvas;
		this.mouseAdapter = mouseAdapter;
		this.IMAGE_WIDTH = IMAGE_WIDTH;
		this.IMAGE_HEIGHT = IMAGE_HEIGHT;
		this.running = false;
		this.initialised = false;
		rollbackMemory = new Deque<>(30);
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

			// Render if it's time for a new frame

			tick++;
			if (tick == DESIRED_FPS/5) {
				tick = 0;

				if (!rollbackMemory.isFull()) {
					rollbackMemory.enqueue(new FluidState(fluid.dens.clone(), fluid.u.clone(), fluid.v.clone()));
				} else {
					rollbackMemory.dequeue();
					rollbackMemory.enqueue(new FluidState(fluid.dens.clone(), fluid.u.clone(), fluid.v.clone()));
				}
			}
			if (frameTime >= TIME_PER_FRAME) {
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
				double num = Math.abs(plotChoice[index(x / CELL_LENGTH, y / CELL_LENGTH)] * this.plotFactor);
				int color = (int) (num > 255 ? 255 : num);
				int rgb = (255 << 24) | (color << 16) | (color << 8) | color; // Set alpha channel to fully opaque
				buffer[x + y * IMAGE_WIDTH] = rgb;
			}
		}

		PixelWriter writer = canvas.getGraphicsContext2D()
				.getPixelWriter();
		writer.setPixels(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, PixelFormat.getIntArgbInstance(), buffer, 0, IMAGE_WIDTH);
	}

	public int index(int i, int j) {
		if (i < 0) {
			i = 0;
		}
		if (i > FLUID_WIDTH + 1) {
			i = FLUID_WIDTH + 1;
		}
		if (j < 0) {
			j = 0;
		}
		if (j > FLUID_HEIGHT + 1) {
			j = FLUID_HEIGHT + 1;
		}
		return (i + j * (FLUID_WIDTH + 2));
	}

	private void addSourcesFromUI(Fluid fluid) {
		synchronized (fluid) {
			mouseAdapter.consumeSources(fluidInput -> {
				fluid.u[fluid.index(fluidInput.x, fluidInput.y)] += fluidInput.forceX;
				fluid.v[fluid.index(fluidInput.x, fluidInput.y)] += fluidInput.forceY;
				fluid.u[fluid.index(fluidInput.x + 1, fluidInput.y)] += fluidInput.forceX;
				fluid.v[fluid.index(fluidInput.x + 1, fluidInput.y)] += fluidInput.forceY;
				fluid.u[fluid.index(fluidInput.x - 1, fluidInput.y)] += fluidInput.forceX;
				fluid.v[fluid.index(fluidInput.x - 1, fluidInput.y)] += fluidInput.forceY;
				fluid.u[fluid.index(fluidInput.x, fluidInput.y + 1)] += fluidInput.forceX;
				fluid.v[fluid.index(fluidInput.x, fluidInput.y + 1)] += fluidInput.forceY;
				fluid.u[fluid.index(fluidInput.x, fluidInput.y - 1)] += fluidInput.forceX;
				fluid.v[fluid.index(fluidInput.x, fluidInput.y - 1)] += fluidInput.forceY;
				fluid.dens[fluid.index(fluidInput.x, fluidInput.y)] += fluidInput.density;
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
		this.fluid = new TunnelFluid(IMAGE_WIDTH / CELL_LENGTH, IMAGE_HEIGHT / CELL_LENGTH, CELL_LENGTH, 0, 0, 4);
	}

	public void setBoxBoundaries() {
		this.fluid = new BoxFluid(IMAGE_WIDTH / CELL_LENGTH, IMAGE_HEIGHT / CELL_LENGTH, CELL_LENGTH, 0, 0, 4);
	}

	public void setQuantities(int CELL_LENGTH, int ITERATIONS, int FPS) {
		this.CELL_LENGTH = CELL_LENGTH;
		this.ITERATIONS = ITERATIONS;
		this.TPS = FPS;
		this.DESIRED_FPS = FPS;
		this.initialised = true;
	}

	public void setPlotType(String plotType) {
		densityPlot = false;
		xVelocityPlot = false;
		yVelocityPlot = false;
		if (plotType.equals("Density")) {
			plotFactor = 1;
			densityPlot = true;
		} else if (plotType.equals("x-Velocity")) {
			plotFactor = 100;
			xVelocityPlot = true;
		} else if (plotType.equals("y-Velocity")) {
			plotFactor = 100;
			yVelocityPlot = true;
		}
	}

	public void render() {
		if (densityPlot) {
			render(fluid.dens.clone()); // Render with the most recent fluid density
		} else if (xVelocityPlot) {
			render(fluid.u.clone()); // Render with the most recent fluid density
		} else if (yVelocityPlot) {
			render(fluid.v.clone()); // Render with the most recent fluid density
		}
	}
	public void rollback() {
		if (!rollbackMemory.isEmpty()) {
			FluidState state = rollbackMemory.pop();
			fluid.dens = state.dens;
			fluid.u = state.u;
			fluid.v = state.v;
		}
		render();
	}

	public void step() {
		for (int i = 0; i < DESIRED_FPS; i++) {
			fluid.step(1.0 / TPS);
		}
		render();
	}
}