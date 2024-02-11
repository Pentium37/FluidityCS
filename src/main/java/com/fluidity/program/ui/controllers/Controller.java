package com.fluidity.program.ui.controllers;

import com.fluidity.program.ui.Manager;
import javafx.fxml.Initializable;

public abstract class Controller implements Initializable {
	protected Manager manager;

	public void setManager(Manager manager) {
		this.manager = manager;
	}
}
