package com.fluidity.program.ui.controllers;

import com.fluidity.program.simulation.FluidInput;
import com.fluidity.program.simulation.SimulationThreaded;
import com.fluidity.program.simulation.fluid.Fluid;
import com.fluidity.program.ui.MouseAdapter;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.net.URL;
import java.nio.IntBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static java.lang.Math.hypot;

public class TestSimulationController extends Controller implements MouseAdapter {
	@FXML
	Canvas canvas;
	private boolean mouseHeld;
	private List<FluidInput> sourceQueue;
	private Instant startAdd;
	private int[] previousCoords;
	private int IMAGE_WIDTH;
	private int IMAGE_HEIGHT;

	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
		Thread thread = new Thread(r);
		thread.setDaemon(true);
		return thread;
	});
	@Override
	public void initialize(final URL url, final ResourceBundle resourceBundle) {
		sourceQueue = new ArrayList<>();
		startAdd = Instant.now();
		IMAGE_WIDTH = 300;
		IMAGE_HEIGHT = 300;
		this.canvas.setHeight(IMAGE_HEIGHT);
		this.canvas.setWidth(IMAGE_WIDTH);
		EXECUTOR.submit(new SimulationThreaded(canvas, this));
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
				6.0 * hypot(velocityX, velocityY), 3));
	}

	@Override
	public synchronized void consumeSources(Consumer<FluidInput> sourceConsumer) {
		if (mouseHeld) {
			long timeHeld = Duration.between(startAdd, Instant.now())
					.toMillis();
			startAdd = Instant.now();
			sourceQueue.add(
					new FluidInput(previousCoords[0], previousCoords[1], 0, 0, timeHeld * 20, 3));
		}

		for (FluidInput source : sourceQueue) {
			sourceConsumer.accept(source);
		}
		sourceQueue.clear();
	}
}