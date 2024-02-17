package com.fluidity.program.ui;

import com.fluidity.program.simulation.FluidInput;
import com.fluidity.program.simulation.fluid.Fluid;
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

	void render(double[] dens);
}