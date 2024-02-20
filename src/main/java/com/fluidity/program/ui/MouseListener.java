package com.fluidity.program.ui;

import com.fluidity.program.simulation.FluidInput;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

public interface MouseListener {
	@FXML
	void mousePressed(MouseEvent e);

	@FXML
	void mouseReleased();

	@FXML
	void mouseDragged(MouseEvent e);

	void consumeSources(Consumer<FluidInput> sourceConsumer);
}