package com.example.tspsystem.controllers;

import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> languageComboBox;

    private final Gson gson = new Gson();

    @FXML
    private void initialize() {
        fetchLanguages();
    }

    private void fetchLanguages() {
        String uri = "http://localhost:8080/api/users/languages";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
        HttpClient client = HttpClient.newHttpClient();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(languagesJson -> {
                    System.out.println("Odpowiedź JSON z serwera: " + languagesJson);
                    if (languagesJson != null && !languagesJson.isEmpty()) {
                        List<String> languageList = gson.fromJson(languagesJson, new TypeToken<List<String>>() {}.getType());
                        Platform.runLater(() -> languageComboBox.setItems(FXCollections.observableArrayList(languageList)));
                    } else {
                        Platform.runLater(() -> showAlert("Network Error", "Otrzymano pustą odpowiedź z serwera."));
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace(); // Dodano logowanie błędów
                    Platform.runLater(() -> showAlert("Network Error", "Nie można pobrać listy języków. Błąd: " + e.getMessage()));
                    return null;
                });
    }
    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/tspsystem/login.fxml")); // Wstaw prawidłową ścieżkę do pliku login.fxml
            Parent loginView = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(loginView));
            stage.setTitle("Logowanie");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        if (validateInput()) {
            sendRegistrationRequest();
        }
    }

    private boolean validateInput() {
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
        System.out.println("Sending registration request");

        String json = String.format("{ \"login\": \"%s\", \"password\": \"%s\", \"language\": \"%s\" }",
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
        System.out.println("Odpowiedź z serwera: " + responseBody);
        Platform.runLater(() -> showAlert("Odpowiedź z serwera", responseBody));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
