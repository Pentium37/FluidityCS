package com.fluidity.program.simulation.fluid;

import java.util.List;

public abstract class Fluid {
	public int WIDTH;
	public int HEIGHT;
	private final int CELL_LENGTH;
	private final int size;
	private final int ITERATIONS;
	public double[] dens, u, v; // change to horizontal, vertical and density
	public double diffusionRate, viscosity;
	private double dt; // change to delta time
	public int deltaTimeFactor;
	List<int[]> barrierCoords;

	protected Fluid(int WIDTH, int HEIGHT, int CELL_LENGTH, double viscosity, double diffusionRate, int ITERATIONS) {
		this.WIDTH = WIDTH;
		this.HEIGHT = HEIGHT;
		this.CELL_LENGTH = CELL_LENGTH;
		this.size = (WIDTH + 2) * (HEIGHT + 2);

		this.viscosity = viscosity;
		this.diffusionRate = diffusionRate;
		this.ITERATIONS = ITERATIONS;

		dens = new double[size];
		u = new double[size];
		v = new double[size];

		this.deltaTimeFactor = 1;

		for (int i = 0; i < WIDTH + 1; i++) {
			for (int j = 0; j < HEIGHT + 1; j++) {
				dens[index(i, j)] = 0;
				u[index(i, j)] = 0;
				v[index(i, j)] = 0;
			}
		}
	}

	public void step(double deltaTime) {
		conditions();
		this.dt = deltaTime*deltaTimeFactor;
		double[] u_prev = new double[size];
		double[] v_prev = new double[size];
		double[] dens_prev = new double[size];

		diffuse(1, u_prev, u, viscosity);
		diffuse(2, v_prev, v, viscosity);

		project(u_prev, v_prev);

		advect(1, u, u_prev, u_prev, v_prev);
		advect(2, v, v_prev, u_prev, v_prev);
		project(u, v);

		diffuse(0, dens_prev, dens, diffusionRate);
		advect(0, dens, dens_prev, u, v);
	}

	public void diffuse(int b, double[] destination, double[] source, double diffusionRate) {
		double a = dt * diffusionRate;
		gaussSeidel(b, destination, source, a, 1 + 4 * a);
	}

	public void advect(int b, double[] destination, double[] source, double[] u, double[] v) {
		int i0, j0, i1, j1;
		double x, y, s0, t0, s1, t1, dt0_i, dt0_j;

		dt0_i = dt * WIDTH;
		dt0_j = dt * HEIGHT;
		for (int i = 1; i < WIDTH + 1; i++) {
			for (int j = 1; j < HEIGHT + 1; j++) {
				x = i - dt0_i * u[index(i, j)];
				y = j - dt0_j * v[index(i, j)];
				if (x < 0.5) {
					x = 0.5;
				} else if (x > WIDTH + 0.5) {
					x = WIDTH + 0.5;
				}

				i0 = (int) x;
				i1 = i0 + 1;

				if (y < 0.5) {
					y = 0.5;
				} else if (y > HEIGHT + 0.5) {
					y = HEIGHT + 0.5;
				}

				j0 = (int) y;
				j1 = j0 + 1;

				s1 = x - i0;
				s0 = 1 - s1;
				t1 = y - j0;
				t0 = 1 - t1;

				destination[index(i, j)] = s0 * (t0 * source[index(i0, j0)] + t1 * source[index(i0, j1)]) + s1 * (
						t0 * source[index(i1, j0)] + t1 * source[index(i1, j1)]);
			}
		}
		setBoundary(b, destination);
	}

	public void project(double[] u, double[] v) {
		double[] pressure = new double[size];
		double[] divergenceField = new double[size];
		double h = CELL_LENGTH;

		for (int i = 1; i < WIDTH + 1; i++) {
			for (int j = 1; j < HEIGHT + 1; j++) {
				divergenceField[index(i, j)] =
						-0.5 * h * (u[index(i + 1, j)] - u[index(i - 1, j)] + v[index(i, j + 1)] - v[index(i, j - 1)]);
				pressure[index(i, j)] = 0;
			}
		}
		setBoundary(0, divergenceField);
		setBoundary(0, pressure);
		gaussSeidel(0, pressure, divergenceField, 1, 4.0);

		for (int i = 1; i < WIDTH + 1; i++) {
			for (int j = 1; j < HEIGHT + 1; j++) {
				u[index(i, j)] -= 0.5 * (pressure[index(i + 1, j)] - pressure[index(i - 1, j)]) / h;
				v[index(i, j)] -= 0.5 * (pressure[index(i, j + 1)] - pressure[index(i, j - 1)]) / h;
			}
		}
		//potentially need to change this
		setBoundary(1, u);
		setBoundary(2, v);
	}

	public void gaussSeidel(int b, double[] destination, double[] source, double a, double factor) {
		for (int k = 0; k < ITERATIONS; k++) {
			for (int i = 1; i < WIDTH + 1; i++) {
				for (int j = 1; j < HEIGHT + 1; j++) {
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

	abstract void setBoundary(int b, double[] destination);
	abstract  void conditions();
	public void setBarriers(final int b, final double[] destination) {
		for (int[] barrierCoord : barrierCoords) {
			int x = barrierCoord[0];
			int y = barrierCoord[1];
			destination[index(x, y)] = (b == 2) ? -destination[index(x, y)] : destination[index(x, y)];
		}
	}
}
