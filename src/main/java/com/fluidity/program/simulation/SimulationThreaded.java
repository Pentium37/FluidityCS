package com.fluidity.program.simulation;

import com.fluidity.program.simulation.fluid.BoxFluid;
import com.fluidity.program.simulation.fluid.Fluid;
import com.fluidity.program.ui.MouseAdapter;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class SimulationThreaded implements Runnable {
	private final Canvas canvas;
	private final MouseAdapter mouseAdapter;

	public SimulationThreaded(Canvas canvas, MouseAdapter mouseAdapter) {
		this.canvas = canvas;
		this.mouseAdapter = mouseAdapter;
	}

	@Override
	public void run() {
		Fluid fluid = new BoxFluid();
		long current = System.nanoTime();
		while (true) {
			long l = System.nanoTime();
			double deltaMillis = (l - current) / 1_000_000_000.0;
			fluid.step(deltaMillis);
			Platform.runLater(() -> {
				addSourcesFromUI(fluid);
				PixelWriter writer = canvas.getGraphicsContext2D().getPixelWriter();
				for (int y = 0; y < canvas.getHeight(); y++) {
					for (int x = 0; x < canvas.getWidth(); x++) {
						double num = fluid.dens[fluid.index(x / 3, y / 3)];
						int color = (byte) (num > 255 ? 255 : num) & 0xFF;
						writer.setColor(x, y, Color.grayRgb(color));
					}
				}
			});
			current = l;
		}
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
}