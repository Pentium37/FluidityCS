package com.fluidity.program.ui.controllers;

import com.fluidity.program.ui.Manager;

public abstract class Controller {
	protected Manager manager;

	public void setManager(Manager manager) {
		this.manager = manager;
	}
}
