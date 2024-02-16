package com.fluidity.program.ui.controllers;

import com.fluidity.program.simulation.DataProvider;
import com.fluidity.program.simulation.FluidInput;
import com.fluidity.program.simulation.Simulation;
import com.fluidity.program.ui.MouseAdapter;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;


import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static java.lang.Math.hypot;

public class TestSimulationController extends Controller implements MouseAdapter {
	@FXML
	Canvas canvas;
	Simulation simulation;
	private boolean mouseHeld;
	private List<FluidInput> sourceQueue;
	private Instant startAdd;
	private int[] previousCoords;

	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		sourceQueue = new ArrayList<>();
		startAdd = Instant.now();
		simulation = new Simulation(this);
		simulation.startSimulation();
		this.canvas.setHeight(300);
		this.canvas.setWidth(300);
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
				6.0 * hypot(velocityX, velocityY), simulation.CELL_LENGTH));
	}

	@Override
	public synchronized void consumeSources(Consumer<FluidInput> sourceConsumer) {
		if (mouseHeld) {
			long timeHeld = Duration.between(startAdd, Instant.now())
					.toMillis();
			startAdd = Instant.now();
			sourceQueue.add(
					new FluidInput(previousCoords[0], previousCoords[1], 0, 0, timeHeld * 20, simulation.CELL_LENGTH));
		}

		for (FluidInput source : sourceQueue) {
			sourceConsumer.accept(source);
		}
		sourceQueue.clear();
	}

	@Override
	public synchronized void render(DataProvider pixelStream) {
		GraphicsContext gc = canvas.getGraphicsContext2D();

		for (int y = 0; y <  canvas.getHeight() ; y++) {
			for (int x = 0; x < canvas.getWidth(); x++) {
				// Obtain the pixel value from the pixelStream
				byte pixelValue = pixelStream.provideData(x, y);

				// Convert pixel value to grayscale color (0-255)
				int color = pixelValue & 0xFF;

				// Set color and draw a pixel
				gc.setFill(javafx.scene.paint.Color.rgb(color, color, color));
				gc.fillRect(x, y, 1, 1);
			}
		}
	}

}