package com.example.tspsystem.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;

@Component
public class SettingsController implements Initializable {
    @FXML
    private ComboBox<String> languageComboBox;
    @FXML
    private Button saveChangesButton;

    private static final String SERVER_URL = "http://localhost:8080/api/languages/names";
    private final Gson gson = new Gson();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fetchLanguages();
    }

    @FXML
    public void handleSaveChangesAction() {

        Long selectedLanguageId = 1L;
        Long userId = 1L;

        if(selectedLanguageId == null || userId == null) {
            showAlert("Error", "Wybierz język lub błąd ID użytkownika.");
            return;
        }

        sendLanguageUpdateRequest(userId, selectedLanguageId);
    }

    private void sendLanguageUpdateRequest(Long userId, Long languageId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "/users/" + userId + "/language"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(languageId.toString()))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        Platform.runLater(() -> showAlert("Success", "Język został zaktualizowany."));
                    } else {
                        Platform.runLater(() -> showAlert("Error", "Nie udało się zaktualizować języka."));
                    }
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> showAlert("Error", "Błąd komunikacji z serwerem: " + e.getMessage()));
                    return null;
                });
    }

    private void fetchLanguages() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(languagesJson -> {
                    if (languagesJson != null && !languagesJson.isEmpty()) {
                        List<String> languageList = gson.fromJson(languagesJson, new TypeToken<List<String>>() {}.getType());
                        Platform.runLater(() -> languageComboBox.setItems(FXCollections.observableArrayList(languageList)));
                    } else {
                        Platform.runLater(() -> showAlert("Network Error", "Otrzymano pustą odpowiedź z serwera."));
                    }
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> showAlert("Network Error", "Nie można pobrać listy języków. Błąd: " + e.getMessage()));
                    return null;
                });
    }
    private void getSelectedLanguageId(String selectedLanguage, Consumer<Long> languageIdConsumer) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + "http://localhost:8080/api/languages/names" + selectedLanguage))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(languageIdJson -> {
                    Long languageId = gson.fromJson(languageIdJson, Long.class);
                    Platform.runLater(() -> languageIdConsumer.accept(languageId));
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> showAlert("Error", "Błąd podczas pobierania ID języka: " + e.getMessage()));
                    return null;
                });
    }





    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}