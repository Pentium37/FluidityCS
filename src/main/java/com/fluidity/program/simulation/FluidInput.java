package com.fluidity.program.simulation;

public record FluidInput(int x, int y, double forceX, double forceY, double density) {
	@Override
	public String toString() {
		return "{" + x + "," + y + "," + forceX + "," + forceY + "," + density + "}";
	}
}