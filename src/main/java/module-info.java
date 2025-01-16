module routmmariusionel.productapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;

    opens routmmariusionel.productapp to javafx.fxml;
    exports routmmariusionel.productapp;
}