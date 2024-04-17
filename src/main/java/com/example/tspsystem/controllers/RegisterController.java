package com.example.tspsystem.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> languageComboBox;

    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        if (validateInput()) {
            sendRegistrationRequest();
        }
    }

    private boolean validateInput() {
        // Debugging output
        System.out.println("Username: " + usernameField.getText());
        System.out.println("Password: " + passwordField.getText());
        System.out.println("Language: " + languageComboBox.getValue());

        if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty() || languageComboBox.getValue() == null) {
            showAlert("Błąd walidacji", "Proszę wypełnić wszystkie pola.");
            return false;
        }
        return true;
    }

    private void sendRegistrationRequest() {
        // Debug log
        System.out.println("Sending registration request");

        String json = String.format("{ \"login\": \"%s\", \"password\": \"%s\", \"language_id\": \"%s\" }",
                usernameField.getText(),
                passwordField.getText(),
                languageComboBox.getValue());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/users/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::handleResponse)
                .exceptionally(e -> {
                    Platform.runLater(() -> showAlert("Network Error", "Nie można połączyć się z serwerem."));
                    return null;
                });
    }

    private void handleResponse(String responseBody) {
        // Debug log
        System.out.println("Odpowiedź z serwera\n: " + responseBody);
        Platform.runLater(() -> {
            showAlert("Odpowiedź z serwera\n", responseBody);
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Platform.runLater(alert::showAndWait);
    }
}
