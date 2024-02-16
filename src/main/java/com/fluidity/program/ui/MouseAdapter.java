package com.fluidity.program.ui;

import com.fluidity.program.simulation.DataProvider;
import com.fluidity.program.simulation.FluidInput;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

public interface MouseAdapter {
	@FXML
	void mousePressed(MouseEvent e);

	@FXML
	void mouseReleased();

	@FXML
	void mouseDragged(MouseEvent e);

	void consumeSources(Consumer<FluidInput> sourceConsumer);

	void render(DataProvider pixelStream);
}