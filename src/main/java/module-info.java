module com.example.tspsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires java.net.http;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens com.example.tspsystem to javafx.fxml;
    exports com.example.tspsystem;
    exports com.example.tspsystem.controllers;
    opens com.example.tspsystem.controllers to javafx.fxml;

}