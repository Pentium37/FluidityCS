package com.fluidity.program.simulation.fluid;

public class BoxFluid extends Fluid{

	public BoxFluid(final int WIDTH, final int HEIGHT, final int CELL_LENGTH, final double viscosity,
			final double diffusionRate, final int ITERATIONS) {
		super(WIDTH, HEIGHT, CELL_LENGTH, viscosity, diffusionRate, ITERATIONS);
	}

	@Override
	void setBoundary(final int b, final double[] destination) {
		// make a global factor for -1 or 1
		for (int i = 1; i < WIDTH + 1; i++) {
			if (i <= HEIGHT) {
				destination[index(0, i)] = (b == 1) ? -destination[index(1, i)] : destination[index(1, i)];
				destination[index(WIDTH + 1, i)] = (b == 1) ? -destination[index(WIDTH, i)] : destination[index(WIDTH, i)];
			}
			destination[index(i, 0)] = (b == 2) ? -destination[index(i, 1)] : destination[index(i, 1)];
			destination[index(i, HEIGHT + 1)] =
					(b == 2) ? -destination[index(i, HEIGHT)] : destination[index(i, HEIGHT)];
		}
		destination[index(0, 0)] = 0.5 * (destination[index(1, 0)] + destination[index(0, 1)]);
		destination[index(0, HEIGHT + 1)] = 0.5 * (destination[index(1, HEIGHT + 1)] + destination[index(0, HEIGHT)]);
		destination[index(WIDTH + 1, 0)] = 0.5 * (destination[index(WIDTH, 0)] + destination[index(WIDTH + 1, 1)]);
		destination[index(WIDTH + 1, HEIGHT + 1)] =
				0.5 * (destination[index(WIDTH, HEIGHT + 1)] + destination[index(WIDTH + 1, HEIGHT)]);

	}
}
