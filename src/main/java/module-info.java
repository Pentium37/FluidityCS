module com.example.fluidity {
	requires javafx.controls;
	requires javafx.fxml;

	exports com.fluidity.structure;
	opens com.fluidity.structure to javafx.fxml;
	exports com.fluidity.structure.controllers;
	opens com.fluidity.structure.controllers to javafx.fxml;
}