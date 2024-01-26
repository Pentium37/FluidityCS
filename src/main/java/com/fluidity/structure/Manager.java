package com.fluidity.structure;

import javafx.stage.Stage;

import java.io.IOException;

public class Manager {
	private final Stage window;

	public Manager(final Stage window){
		this.window = window;
	}

	public void loadMainMenu() throws IOException {
		ProgramState programStateToAdd = ProgramState.MAIN_MENU;
		switchScene(programStateToAdd);
	}

	private void switchScene(ProgramState programState) throws IOException {
		window.setTitle(programState.name());
		window.setResizable(false);
		window.setScene(programState.getScene());
		window.show();
	}
}
