package com.fluidity.program.simulation.fluid;

public class TunnelFluid extends Fluid {
	private double inflowDensity;
	private double inflowVelocity;
	public TunnelFluid(int WIDTH, int HEIGHT, int CELL_LENGTH, double viscosity, double diffusionRate, int ITERATIONS, double inflowDensity, double inflowVelocity) {
		// Set the fluid's properties
		super(WIDTH, HEIGHT, CELL_LENGTH, viscosity, diffusionRate, ITERATIONS);
		this.inflowDensity = inflowDensity;
		this.inflowVelocity = inflowVelocity;
	}

	@Override
	void setBoundary(final int b, final double[] destination) {
		// setting the ceiling and floor boundaries
		for (int i = 1; i < WIDTH + 1; i++) {
			destination[index(i, 0)] = (b == 2) ? -destination[index(i, 1)] : destination[index(i, 1)];
			destination[index(i, HEIGHT + 1)] =
					(b == 2) ? -destination[index(i, HEIGHT)] : destination[index(i, HEIGHT)];
		}

		// Setting the corner boundaries
		destination[index(0, 0)] = 0.5 * (destination[index(1, 0)] + destination[index(0, 1)]);
		destination[index(0, HEIGHT + 1)] = 0.5 * (destination[index(1, HEIGHT + 1)] + destination[index(0, HEIGHT)]);
		destination[index(WIDTH + 1, 0)] = 0.5 * (destination[index(WIDTH, 0)] + destination[index(WIDTH + 1, 1)]);
		destination[index(WIDTH + 1, HEIGHT + 1)] =
				0.5 * (destination[index(WIDTH, HEIGHT + 1)] + destination[index(WIDTH + 1, HEIGHT)]);

		setBarriers(b, destination);
	}

	@Override
	void conditions() {
		for (int i = 0; i < HEIGHT + 1; i++) {
			horizontalVelocity[index(0, i)] = inflowVelocity;
		}
	}
}
