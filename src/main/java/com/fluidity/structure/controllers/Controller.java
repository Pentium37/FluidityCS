package com.fluidity.structure.controllers;

import com.fluidity.structure.Manager;

public abstract class Controller {
	protected Manager manager;

	public void setManager(Manager manager) {
		this.manager = manager;
	}

	abstract void initialise();
}
