package com.fluidity.program.test;

import java.util.Arrays;

public class Prototype3 {
	// Testing gauss-seidel

	int WIDTH;
	int HEIGHT;

	public void test1() {
		double[] destination = new double[] { 0, 0, 0 };
		double[] source = new double[] { 0, 4, 6};
		double a = 0.5;
		double factor = 2.0;
		gaussSeidel(0, destination, source, a, factor);
		System.out.println(Arrays.toString(destination));
	}

	public void gaussSeidel(int b, double[] destination, double[] source, double a, double factor) {
		// iterate through the grid and update the destination array using the source array
		for (int k = 0; k < 20; k++) {
			for (int i = 1; i < WIDTH + 1; i++) {
				for (int j = 1; j < HEIGHT + 1; j++) {
					// Update the destination array using the source array and its surrounding fluid cells
					destination[index(i, j)] =
							(source[index(i, j)] + a * (destination[index(i - 1, j)] + destination[index(i + 1, j)]
									+ destination[index(i, j - 1)] + destination[index(i, j + 1)])) / (factor);
				}
			}
			System.out.println(Arrays.toString(destination));
		}
	}

	public int index(int i, int j) {
		if (i < 0) {
			i = 0;
		}
		if (i > +1) {
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

}
