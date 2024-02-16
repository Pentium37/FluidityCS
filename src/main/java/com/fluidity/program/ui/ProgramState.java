package com.fluidity.program.ui;

/* Would be preferable to name it Scene/View, but it would override JFX classes
Hence I gave it the name ProgramState */
public enum ProgramState {
	// Values of each scene that is a constant
	MAIN_MENU("/MainMenu.fxml"),
	SETTINGS("/Settings.fxml"),
	ABOUT("/About.fxml"),
	EXIT("/Exit.fxml"),
	SIMULATION("/Simulation.fxml"),
	RECORDINGS("/Recordings.fxml"),
	TEST_SIMULATION("/TestSimulation.fxml");

	// Stores path of .fxml file to load later
	/* The string path is a private attribute because classes only need to retrieve a path rather than changing it.
	   Because enums are static, setting a value for path from an instantiated context will be abusing the property.
	   This is because enums provide an abstraction layer to represent a set of related values.
	   Adding specific attribute values from an instantiated context can break this abstraction.
	   This makes the code more tightly coupled to implementation details.*/
	private final String path;

	// The enums use this constructor to define themselves before the compilation of the program
	ProgramState(String path) {
		this.path = path;
	}

	// Used to retrieve the path if needed for loading
	public String getPath() {
		return path;
	}
}