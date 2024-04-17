package com.example.tspsystem.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField languageField;

    @FXML
    private void handleLoginButtonAction(ActionEvent event) {
        // Logowanie użytkownika
    }

    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        try {
            // Ładowanie widoku rejestracji
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/tspsystem/register-view.fxml"));
            Parent registerView = loader.load();
            Scene scene = new Scene(registerView);

            // Uzyskanie obecnego stage (okna) z dowolnego kontrolera
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Obsługa błędów ładowania widoku
        }
    }
}
