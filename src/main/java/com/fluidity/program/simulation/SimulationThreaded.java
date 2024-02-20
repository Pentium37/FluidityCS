package com.fluidity.program.simulation;

import com.fluidity.program.simulation.fluid.BoxFluid;
import com.fluidity.program.simulation.fluid.Fluid;
import com.fluidity.program.ui.MouseListener;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;

public class SimulationThreaded implements Runnable {
	private final Canvas canvas;
	private final MouseListener mouseAdapter;
	private final int IMAGE_WIDTH;
	private final int IMAGE_HEIGHT;
	private final int CELL_LENGTH;
	private int FLUID_WIDTH;
	private int FLUID_HEIGHT;
	private Fluid fluid;
	//	public static final double FPS = 10, TPS = 10;

	public SimulationThreaded(Canvas canvas, MouseListener mouseAdapter, int IMAGE_WIDTH, int IMAGE_HEIGHT,
			int CELL_LENGTH) {
		this.canvas = canvas;
		this.mouseAdapter = mouseAdapter;
		this.CELL_LENGTH = CELL_LENGTH;
		this.IMAGE_WIDTH = IMAGE_WIDTH;
		this.IMAGE_HEIGHT = IMAGE_HEIGHT;
		this.fluid = new BoxFluid(IMAGE_WIDTH / CELL_LENGTH, IMAGE_HEIGHT / CELL_LENGTH, CELL_LENGTH, 1, 1, 4);
	}

//	@Override
//	public void run() {
//		Fluid fluid = new BoxFluid(IMAGE_WIDTH / CELL_LENGTH, IMAGE_HEIGHT / CELL_LENGTH, CELL_LENGTH, 2, 2, 4);
//		this.FLUID_WIDTH = fluid.WIDTH;
//		this.FLUID_HEIGHT = fluid.HEIGHT;
//
//		long current = System.nanoTime();
//		while (true) {
//			long l = System.nanoTime();
//			double deltaMillis = (l - current) / 1_000_000_000.0;
//
//			fluid.step(deltaMillis);
//			addSourcesFromUI(fluid);
//
//			render(fluid.dens.clone()); //run later
//
//			current = l;
//		}
//	}

	@Override
	public void run() {

		this.FLUID_WIDTH = fluid.WIDTH;
		this.FLUID_HEIGHT = fluid.HEIGHT;

		final double TPS = 60; // Ticks Per Second
		final double TIME_PER_TICK = 1.0 / TPS;
		final double DESIRED_FPS = 60; // Desired Frames Per Second
		final double TIME_PER_FRAME = 1.0 / DESIRED_FPS;
		long lastTime = System.nanoTime();
		double unprocessedTime = 0;
		double frameTime = 0;

		while (true) {
			long now = System.nanoTime();
			long passedTime = now - lastTime;
			lastTime = now;
			unprocessedTime += passedTime / 1_000_000_000.0;
			frameTime += passedTime / 1_000_000_000.0;

			// Update fluid and handle inputs as many times as needed to catch up with the target TPS
			while (unprocessedTime > TIME_PER_TICK) {
				double deltaTime = TIME_PER_TICK; // Delta time is constant for each tick
				fluid.step(deltaTime);
				addSourcesFromUI(fluid);
				unprocessedTime -= TIME_PER_TICK;
			}

			// Render if it's time for a new frame
			if (frameTime >= TIME_PER_FRAME) {
				render(fluid.dens.clone()); // Render with the most recent fluid density
				frameTime = 0;
			}

			// Sleep to cap the rendering FPS
			long sleepTime = (long) ((TIME_PER_FRAME - frameTime) * 1_000_000_000);
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void render(double[] dens) {
		int[] buffer = new int[IMAGE_WIDTH * IMAGE_HEIGHT];

		for (int y = 0; y < IMAGE_HEIGHT; y++) {
			for (int x = 0; x < IMAGE_WIDTH; x++) {
				double num = dens[index(x/CELL_LENGTH, y/CELL_LENGTH)];
				int color = (int) (num > 255 ? 255 : num);
				int rgb = (255 << 24) | (color << 16) | (color << 8) | color; // Set alpha channel to fully opaque
				buffer[x + y * IMAGE_WIDTH] = rgb;
			}
		}

		PixelWriter writer = canvas.getGraphicsContext2D().getPixelWriter();
		writer.setPixels(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT,
				PixelFormat.getIntArgbInstance(), buffer, 0, IMAGE_WIDTH);
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
}