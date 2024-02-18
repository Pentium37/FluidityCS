package com.fluidity.program.simulation;

import com.fluidity.program.simulation.fluid.BoxFluid;
import com.fluidity.program.simulation.fluid.Fluid;
import com.fluidity.program.ui.MouseAdapter;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import java.util.concurrent.TimeUnit;

public class SimulationThreaded implements Runnable {
	private final Canvas canvas;
	private final MouseAdapter mouseAdapter;
	private int CELL_LENGTH;
//	public static final double FPS = 10, TPS = 10;

	public SimulationThreaded(Canvas canvas, MouseAdapter mouseAdapter, int CELL_LENGTH) {
		this.canvas = canvas;
		this.mouseAdapter = mouseAdapter;
		this.CELL_LENGTH = CELL_LENGTH;
	}

		@Override
		public void run() {
			Fluid fluid = new BoxFluid(120 / CELL_LENGTH, 120 / CELL_LENGTH, CELL_LENGTH, 2, 2, 4);

			long current = System.nanoTime();
			while (true) {
				long l = System.nanoTime();
				double deltaMillis = (l - current) / 1_000_000_000.0;
				fluid.step(deltaMillis);

				Platform.runLater(() -> {
					addSourcesFromUI(fluid);
					PixelWriter writer = canvas.getGraphicsContext2D()
							.getPixelWriter();

					for (int y = 0; y < fluid.HEIGHT; y++) {
						for (int x = 0; x < fluid.WIDTH; x++) {
							double num = fluid.dens[fluid.index(x, y)];
							int color = (byte) (num > 255 ? 255 : num) & 0xFF;
							for (int i = 0; i < CELL_LENGTH; i++) {
								for (int j = 0; j < CELL_LENGTH; j++) {
									writer.setColor(x * CELL_LENGTH + i, y * CELL_LENGTH + j, Color.grayRgb(color));
								}
							}
						}
					}
				});

				current = l;
			}
		}

//	@Override
//	public void run() {
//		double deltaTimeSeconds = 1.0 / TPS;
//		double lastFrameTime = nanosToSeconds(System.nanoTime());
//		double secondsToConsume = 0.0;
//		Fluid fluid = new BoxFluid(120 / CELL_LENGTH, 120 / CELL_LENGTH, CELL_LENGTH, 2, 2, 4);
//
//		while (true) {
//			double currentFrameTime = nanosToSeconds(System.nanoTime());
//			double lastFrameNeeded = currentFrameTime - lastFrameTime;
//			lastFrameTime = currentFrameTime;
//
//			secondsToConsume += lastFrameNeeded;
//			while (secondsToConsume >= deltaTimeSeconds) {
//				fluid.step(deltaTimeSeconds);
//				secondsToConsume -= deltaTimeSeconds;
//			}
//
//			Platform.runLater(() -> {
//				addSourcesFromUI(fluid);
//				PixelWriter writer = canvas.getGraphicsContext2D()
//						.getPixelWriter();
//
//				for (int y = 0; y < fluid.HEIGHT; y++) {
//					for (int x = 0; x < fluid.WIDTH; x++) {
//						double num = fluid.dens[fluid.index(x, y)];
//						int color = (byte) (num > 255 ? 255 : num) & 0xFF;
//						for (int i = 0; i < CELL_LENGTH; i++) {
//							for (int j = 0; j < CELL_LENGTH; j++) {
//								writer.setColor(x * CELL_LENGTH + i, y * CELL_LENGTH + j, Color.grayRgb(color));
//							}
//						}
//					}
//				}
//			});
//
//			double currentFPS = 1.0 / lastFrameNeeded;
//			if (currentFPS > FPS) {
//				double targetSecondsPerFrame = 1.0 / FPS;
//				double secondsToWaste = Math.abs(targetSecondsPerFrame - lastFrameNeeded) / 1000000;
//				try {
//					TimeUnit.SECONDS.sleep(secondsToMillis(secondsToWaste));
//				} catch (InterruptedException e) {
//					throw new RuntimeException(e);
//				}
//			}
//		}
//	}
//
//	private static double nanosToSeconds(long nanos) {
//		return nanos / 1E9;
//	}
//
//	private static long secondsToMillis(double seconds) {
//		return (long) (seconds * 1E3);
//	}

	//for (int y = 0; y < canvas.getHeight(); y++) {
	//		for (int x = 0; x < canvas.getWidth(); x++) {
	//			double num = fluid.dens[fluid.index(x / CELL_LENGTH, y / CELL_LENGTH)];
	//			int color = (byte) (num > 255 ? 255 : num) & 0xFF;
	//			writer.setColor(x, y, Color.grayRgb(color));
	//		}
	//	}
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
}