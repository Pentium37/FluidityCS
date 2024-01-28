package com.fluidity.structure;

// Would be preferable to name it Scene/View, but it would override JFX classes
public enum ProgramState {
	// Values of each scene that is a constant
	MAIN_MENU("/MainMenu.fxml"),
	SETTINGS("/Settings.fxml"),
	ABOUT("/About.fxml"),
	//	EXIT("/Exit.fxml"),
	SIMULATION("/Simulation.fxml"),
	RECORDINGS("/Recordings.fxml");

	// path of .fxml file to load later
	private final String path;

	// The enums use this constructor to define themselves before the compilation of the program
	ProgramState(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
}