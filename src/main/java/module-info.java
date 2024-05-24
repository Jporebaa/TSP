module com.example.tspsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires org.jline.style;
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;
    requires spring.boot;
    requires spring.beans;
    requires spring.web;
    requires spring.context;
    requires nd4j.api;
    requires org.postgresql.jdbc;
    requires java.net.http;
    requires commons.math3;
    requires deeplearning4j.nn;
    requires java.desktop;

    // Open packages for reflection if needed
    opens com.example.tspsystem to javafx.fxml;
    opens com.example.tspsystem.controllers to javafx.fxml, com.google.gson;
    opens com.example.tspsystem.model to com.fasterxml.jackson.databind;

    // Export packages
    exports com.example.tspsystem;
    exports com.example.tspsystem.controllers;
    exports com.example.tspsystem.model;
}
