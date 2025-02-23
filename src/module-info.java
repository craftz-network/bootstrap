module bootstrap {
	
	requires javafx.controls;
	requires java.logging;
	requires java.management;
	
	opens net.xxathyx.craftz.bootstrap to javafx.graphics, javafx.fxml;
}
