package com.fluidity.program.simulation;

/**
 * @author Yalesan Thayalan {@literal <yalesan2006@outlook.com>}
 */
public class FluidState {
	public double[] dens, u, v; // change to horizontal, vertical and density

	public FluidState(double[] dens, double[] u, double[] v) {
		this.dens = dens;
		this.u = u;
		this.v = v;
	}
}
