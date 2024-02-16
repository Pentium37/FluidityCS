package com.fluidity.program.simulation;

import com.fluidity.program.simulation.fluid.BoxFluid;
import com.fluidity.program.simulation.fluid.Fluid;
import com.fluidity.program.ui.MouseAdapter;
import com.fluidity.program.ui.controllers.TestSimulationController;

import java.util.concurrent.TimeUnit;

public class Simulation implements Runnable, DataProvider {
	public int CELL_LENGTH;
	public int FPS, TPS;
	private Thread simulationThread;
	private Fluid fluid;
	private final MouseAdapter mouseAdapter;

	public Simulation(MouseAdapter mouseAdapter) {
		this.mouseAdapter = mouseAdapter;
		this.CELL_LENGTH = 3;
		this.FPS = 30;
		this.TPS = 30;
		fluid = new BoxFluid();
	}

	@Override
	public byte provideData(final int x, final int y) {
		double num = fluid.dens[fluid.index(x / CELL_LENGTH, y / CELL_LENGTH)];
		return (byte) ((num > 255) ? 255 : num);
	}

	public void startSimulation() {
		startSimulationThread();
	}

	private void startSimulationThread() {
		simulationThread = new Thread(this);
		simulationThread.start();
	}

	@Override
	public void run() {
		double deltaTimeSeconds = 1.0 / TPS;
		double lastFrameTime = nanosToSeconds(System.nanoTime());
		double secondsToConsume = 0.0;

		while (simulationThread != null) {
			double currentFrameTime = nanosToSeconds(System.nanoTime());
			double lastFrameNeeded = currentFrameTime - lastFrameTime;
			lastFrameTime = currentFrameTime;

			secondsToConsume += lastFrameNeeded;
			while (secondsToConsume >= deltaTimeSeconds) {
				update(deltaTimeSeconds);
				secondsToConsume -= deltaTimeSeconds;
			}

			render();

			double currentFPS = 1.0 / lastFrameNeeded;
			if (currentFPS > FPS) {
				double targetSecondsPerFrame = 1.0 / FPS;
				double secondsToWaste = Math.abs(targetSecondsPerFrame - lastFrameNeeded) / 1000000;
				try {
					TimeUnit.SECONDS.sleep(secondsToMillis(secondsToWaste));
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public void update(double deltaTime) {
		addSourcesFromUI();
		fluid.step(deltaTime);
	}

	public void addSourcesFromUI() {
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

	public void render() {
		mouseAdapter.render(this);
	}

	private static double nanosToSeconds(long nanos) {
		return nanos / 1E9;
	}

	private static long secondsToMillis(double seconds) {
		return (long) (seconds * 1E3);
	}
}