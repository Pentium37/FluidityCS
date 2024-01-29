module com.example.fluidity {
	requires javafx.controls;
	requires javafx.fxml;

	exports com.fluidity.structure;
	opens com.fluidity.structure to javafx.fxml;
	exports com.fluidity.structure.ui.controllers;
	opens com.fluidity.structure.ui.controllers to javafx.fxml;
	exports com.fluidity.structure.utilities;
	opens com.fluidity.structure.utilities to javafx.fxml;
	exports com.fluidity.structure.ui;
	opens com.fluidity.structure.ui to javafx.fxml;
}