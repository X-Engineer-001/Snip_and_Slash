module snip_and_slash {
	requires java.desktop;
	requires transitive javafx.graphics;
	requires javafx.controls;
	requires javafx.fxml;
	opens server to javafx.fxml;
	opens client to javafx.fxml;
	exports server;
	exports client;
}
