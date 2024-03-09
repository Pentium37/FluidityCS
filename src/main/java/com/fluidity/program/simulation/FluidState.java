package com.fluidity.program.simulation;

/**
 * @author Yalesan Thayalan {@literal <yalesan2006@outlook.com>}
 */
public class FluidState {
	public double[] density, horizontalVelocity, verticalVeloctiy; // change to horizontal, vertical and density

	public FluidState(double[] density, double[] horizontalVelocity, double[] verticalVelocity) {
		this.density = density;
		this.horizontalVelocity = horizontalVelocity;
		this.verticalVeloctiy = verticalVelocity;
	}
}
