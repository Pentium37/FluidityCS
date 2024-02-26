package com.fluidity.program.simulation;

import java.util.StringJoiner;

public class FluidInput {
	double forceX, forceY;
	double density;
	int x;
	int y;

	public FluidInput(int x, int y, double forceX, double forceY, double density, int cellLength) {
		this.x = (x / cellLength);
		this.y = (y / cellLength);
		this.forceX = forceX;
		this.forceY = forceY;
		this.density = density;
	}

	@Override
	public String toString() {
		return "{" + x + "," + y + "," + forceX + "," + forceY + "," + density + "}";
	}
}