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
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Budowa obiektu JSON do wysłania
        String jsonPayload = String.format("{\"login\":\"%s\", \"password\":\"%s\"}", username, password);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/users/login"))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .header("Content-Type", "application/json")
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    if (response.contains("Użytkownik zalogowany")) {
                        redirectToHome(); // Przekierowanie do widoku głównego.
                    } else {
                        // Wyświetlenie komunikatu o błędzie logowania w konsoli.
                        System.out.println("Błąd logowania: " + response);
                    }
                })
                .exceptionally(e -> {
                    // Wyświetlenie stosu wywołań błędu w konsoli, jeśli żądanie się nie powiedzie.
                    e.printStackTrace();
                    return null;
                });
    }
    private void redirectToHome() {
        try {
            Parent homeView = FXMLLoader.load(getClass().getResource("/com/example/tspsystem/home.fxml"));
            Scene homeScene = new Scene(homeView);
            Stage window = (Stage) usernameField.getScene().getWindow();
            window.setScene(homeScene);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/tspsystem/register-view.fxml"));
            Parent registerView = fxmlLoader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(registerView));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}