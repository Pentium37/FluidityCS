package com.fluidity.program.ui;

public enum FluidUIAction {
	PRIMARY_PAUSE("primary-pause"),
	PRIMARY_STEP_FORWARD("primary-step-forward"),
	PRIMARY_STEP_BACKWARD("primary-step-backward"),
	SECONDARY_PAUSE("secondary-pause"),
	SECONDARY_STEP_FORWARD("secondary-step-forward"),
	SECONDARY_STEP_BACKWARD("secondary-step-backward");

	private final String path;

	FluidUIAction(String path) {
		this.path = path;
	}
}
