package com.example.tspsystem.controllers;

import com.example.tspsystem.HelloApplication;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class LoginController {

    @FXML
    private void handleLoginButtonAction(javafx.event.ActionEvent event) throws IOException {
        // Ładuje layout dashboard
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("chat_layout.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleRegisterButtonAction(javafx.event.ActionEvent event) throws IOException {
        // Ładuje layout rejestracji
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("register-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
