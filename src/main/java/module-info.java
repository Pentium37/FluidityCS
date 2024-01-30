module com.example.fluidity {
	requires javafx.controls;
	requires javafx.fxml;

	exports com.fluidity.program;
	opens com.fluidity.program to javafx.fxml;
	exports com.fluidity.program.ui.controllers;
	opens com.fluidity.program.ui.controllers to javafx.fxml;
	exports com.fluidity.program.utilities;
	opens com.fluidity.program.utilities to javafx.fxml;
	exports com.fluidity.program.ui;
	opens com.fluidity.program.ui to javafx.fxml;
}