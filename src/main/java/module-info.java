module com.example.fluidity {
	requires javafx.controls;
	requires javafx.fxml;

	exports com.fluidity.structure;
	opens com.fluidity.structure to javafx.fxml;
}