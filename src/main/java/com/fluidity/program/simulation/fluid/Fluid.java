package com.fluidity.program.simulation.fluid;

public abstract class Fluid {
	public int WIDTH;
	public int HEIGHT;
	private final int CELL_LENGTH;
	private final int size;
	private final int ITERATIONS;
	public double[] density, horizontalVelocity, verticalVelocity, pressure;
	public double diffusionRate, viscosity;
	private double dt; // change to delta time
	public int deltaTimeFactor;
	public boolean[][] barrierPresent;

	protected Fluid(int WIDTH, int HEIGHT, int CELL_LENGTH, double viscosity, double diffusionRate, int ITERATIONS) {
		this.WIDTH = WIDTH;
		this.HEIGHT = HEIGHT;
		this.CELL_LENGTH = CELL_LENGTH;
		this.size = (WIDTH + 2) * (HEIGHT + 2);

		this.viscosity = viscosity;
		this.diffusionRate = diffusionRate;
		this.ITERATIONS = ITERATIONS;

		barrierPresent = new boolean[WIDTH][HEIGHT];

		density = new double[size];
		horizontalVelocity = new double[size];
		verticalVelocity = new double[size];

		for (int i = 0; i < WIDTH; i++) {//
			for (int j = 0; j < HEIGHT; j++) {
				barrierPresent[i][j] = false;
			}
		}

		// delta time factor for simulation speed control
		this.deltaTimeFactor = 1;

		// set initial fluid state and conditions (everything is 0 to start with as it is a still fluid)
		for (int i = 0; i < WIDTH + 1; i++) {
			for (int j = 0; j < HEIGHT + 1; j++) {
				density[index(i, j)] = 0;
				horizontalVelocity[index(i, j)] = 0;
				verticalVelocity[index(i, j)] = 0;
			}
		}
	}

	public void step(double deltaTime) {
		this.dt = deltaTime * deltaTimeFactor;
		conditions();
		// create arrays to store states of the fluid fields after each processing stage (diffusion, advection, projection)
		double[] previousHorizontalVelocity = new double[size];
		double[] previousVerticalVelocity = new double[size];
		double[] previousDensity = new double[size];

		diffuse(1, previousHorizontalVelocity, horizontalVelocity, viscosity);
		diffuse(2, previousVerticalVelocity, verticalVelocity, viscosity);

		project(previousHorizontalVelocity, previousVerticalVelocity);

		advect(1, horizontalVelocity, previousHorizontalVelocity, previousHorizontalVelocity, previousVerticalVelocity);
		advect(2, verticalVelocity, previousVerticalVelocity, previousHorizontalVelocity, previousVerticalVelocity);
		project(horizontalVelocity, verticalVelocity);

		diffuse(0, previousDensity, density, diffusionRate);
		advect(0, density, previousDensity, horizontalVelocity, verticalVelocity);
	}

	private void diffuse(int b, double[] destination, double[] source, double diffusionRate) {
		double a = dt * diffusionRate;
		gaussSeidel(b, destination, source, a, 1 + 4 * a);
	}

	private void advect(int b, double[] destination, double[] source, double[] u, double[] v) {
		int i0, j0, i1, j1;
		double x, y, s0, t0, s1, t1, dt0_i, dt0_j;

		// calculate time step for the x-axis
		dt0_i = dt * WIDTH;
		// calculate time step for the y-axis
		dt0_j = dt * HEIGHT;

		for (int i = 1; i < WIDTH + 1; i++) {
			for (int j = 1; j < HEIGHT + 1; j++) {
				x = i - dt0_i * u[index(i, j)];
				y = j - dt0_j * v[index(i, j)];

				// limit the x values to the grid
				if (x < 0.5) {
					x = 0.5;
				} else if (x > WIDTH + 0.5) {
					x = WIDTH + 0.5;
				}

				// closest x coordinate (floor) fluid cell to the "particle"
				i0 = (int) x;
				// closest x coordinate (ceiling) fluid cell to the "particle"
				i1 = i0 + 1;

				// limit the y values to the grid`
				if (y < 0.5) {
					y = 0.5;
				} else if (y > HEIGHT + 0.5) {
					y = HEIGHT + 0.5;
				}

				// closest y coordinate (floor) fluid cell to the "particle"
				j0 = (int) y;
				// closest y coordinate (ceiling) fluid cell to the "particle"
				j1 = j0 + 1;

				// fractional distances relative to cell length for linear interpolation
				s1 = x - i0;
				s0 = 1 - s1;
				t1 = y - j0;
				t0 = 1 - t1;

				// linear interpolation of the source field to the destination field
				destination[index(i, j)] = s0 * (t0 * source[index(i0, j0)] + t1 * source[index(i0, j1)]) + s1 * (
						t0 * source[index(i1, j0)] + t1 * source[index(i1, j1)]);
			}
		}
		setBoundary(b, destination);
	}

	private void project(double[] u, double[] v) {
		// initialise divergence field and pressure field to new arrays
		double[] pressure = new double[size];
		double[] divergenceField = new double[size];
		double h = CELL_LENGTH;

		// calculate divergence field using finite difference method
		for (int i = 1; i < WIDTH + 1; i++) {
			for (int j = 1; j < HEIGHT + 1; j++) {
				divergenceField[index(i, j)] =
						-0.5 * h * (u[index(i + 1, j)] - u[index(i - 1, j)] + v[index(i, j + 1)] - v[index(i, j - 1)]);
				pressure[index(i, j)] = 0;
			}
		}
		setBoundary(0, divergenceField);
		setBoundary(0, pressure);

		// solve for pressure field using Gauss-Seidel method (poisson equation)
		gaussSeidel(0, pressure, divergenceField, 1, 4.0);

		for (int i = 1; i < WIDTH + 1; i++) {
			for (int j = 1; j < HEIGHT + 1; j++) {
				// subtract pressure gradient from velocity component fields to finally make the velocity field incompressible
				u[index(i, j)] -= 0.5 * (pressure[index(i + 1, j)] - pressure[index(i - 1, j)]) / h;
				v[index(i, j)] -= 0.5 * (pressure[index(i, j + 1)] - pressure[index(i, j - 1)]) / h;
			}
		}

		setBoundary(1, u);
		setBoundary(2, v);
	}

	public void gaussSeidel(int b, double[] destination, double[] source, double a, double factor) {
		// iterate through the grid and update the destination array using the source array
		for (int k = 0; k < ITERATIONS; k++) {
			for (int i = 1; i < WIDTH + 1; i++) {
				for (int j = 1; j < HEIGHT + 1; j++) {
					// Update the destination array using the source array and its surrounding fluid cells
					destination[index(i, j)] =
							(source[index(i, j)] + a * (destination[index(i - 1, j)] + destination[index(i + 1, j)]
									+ destination[index(i, j - 1)] + destination[index(i, j + 1)])) / (factor);
				}
			}
			setBoundary(b, destination);
		}
	}

	public int index(int i, int j) {
		if (i < 0) {
			i = 0;
		}
		if (i > WIDTH + 1) {
			i = WIDTH + 1;
		}
		if (j < 0) {
			j = 0;
		}
		if (j > HEIGHT + 1) {
			j = HEIGHT + 1;
		}
		return (i + j * (WIDTH + 2));
	}

	public void setBarriers(final int fieldType, final double[] destinationGrid) {
		// Loop over every cell in the grid
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				// Check if the current cell is a barrier
				if (barrierPresent[x][y]) {
					// Depending on the field type (velocity or density), apply different treatments
					if (fieldType == 0) { // Density
						// For density, attempt to "borrow" the density from neighboring non-barrier cells
						// This is a simplistic way to avoid density artifacts at barrier edges
						double averageProperty = 0;
						int validNeighbors = 0;

						// Iterate through each neighboring cell surrounding the current cell (x, y)
						for (int dx = -1; dx <= 1; dx++) {
							for (int dy = -1; dy <= 1; dy++) {
								// Skip the current cell itself
								if (dx == 0 && dy == 0) continue;

								// Calculate the coordinates of the neighboring cell
								int neighborX = x + dx;
								int neighborY = y + dy;

								// Check if the neighboring cell is within the bounds of the grid and not a barrier cell
								if (neighborX >= 0 && neighborX < WIDTH && neighborY >= 0 && neighborY < HEIGHT && !barrierPresent[neighborX][neighborY]) {
									// Add the density of the neighboring cell to the average density
									averageProperty += destinationGrid[index(neighborX + 1, neighborY + 1)];
									validNeighbors++;
								}
							}
						}

						// Calculate a mean value for the property
						if (validNeighbors > 0) {
							destinationGrid[index(x + 1, y + 1)] = averageProperty / validNeighbors;
						}
					} else if (fieldType == 1 || fieldType == 2) { // Horizontal or Vertical velocity component
						// Set velocity to zero at barrier edges
						destinationGrid[index(x + 1, y + 1)] = 0.0;
					}
				}
			}
		}
	}

	abstract void setBoundary(int b, double[] destination);

	abstract void conditions();
}
