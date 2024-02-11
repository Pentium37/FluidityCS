package com.fluidity.program.simulation;

import com.fluidity.program.simulation.fluid.Fluid;

public class Simulation {
	public int CELL_LENGTH;
	public double FPS, TPS;
	private Thread simulationThread;
	private Fluid fluid;
}
