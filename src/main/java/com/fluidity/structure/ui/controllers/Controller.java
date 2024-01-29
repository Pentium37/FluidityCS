package com.fluidity.structure.ui.controllers;

import com.fluidity.structure.ui.Manager;

public abstract class Controller {
	protected Manager manager;

	public void setManager(Manager manager) {
		this.manager = manager;
	}

	abstract void initialise();
}
