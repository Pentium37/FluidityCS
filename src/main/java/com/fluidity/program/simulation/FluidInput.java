package com.fluidity.program.simulation;

import java.util.StringJoiner;

public class FluidInput {
	double forceX, forceY;
	double density;
	int x;
	int y;
	double cellLength;

	public FluidInput(int x, int y, double forceX, double forceY, double density, int cellLength) {
		this.x = (x / cellLength);
		this.y = (y / cellLength);
		this.forceX = forceX;
		this.forceY = forceY;
		this.density = density;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", FluidInput.class.getSimpleName() + "[", "]").add("forceX=" + forceX)
				.add("forceY=" + forceY)
				.add("density=" + density)
				.add("x=" + x)
				.add("y=" + y)
				.toString();
	}
}