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
import java.util.function.Consumer;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import com.example.tspsystem.controllers.LoginController.SessionManager;
import com.example.tspsystem.controllers.LoginController.UserSession;
@Component
public class SettingsController implements Initializable {
    @FXML
    private ComboBox<String> languageComboBox;
    @FXML
    private Button saveChangesButton;

    private static final String BASE_SERVER_URL = "http://localhost:8080/api";
    private final Gson gson = new Gson();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fetchLanguages();
    }

    private Long getCurrentUserId() {
        UserSession currentSession = LoginController.SessionManager.getCurrentUserSession();
        if (currentSession != null) {
            return currentSession.getUserId().longValue();
        } else {
            return null;
        }
    }
    @FXML
    public void handleSaveChangesAction() {
        String selectedLanguage = languageComboBox.getValue();
        if (selectedLanguage == null) {
            showAlert("Error", "Wybierz język.");
            return;
        }

        getSelectedLanguageId(selectedLanguage, selectedLanguageId -> {
            Long userId = getCurrentUserId(); // Pobierz ID zalogowanego użytkownika
            if (userId != null) {
                sendLanguageUpdateRequest(userId, selectedLanguageId);
            } else {

                showAlert("Error", "Błąd ID użytkownika. Upewnij się, że jesteś zalogowany.");
            }
        });
    }

    private void sendLanguageUpdateRequest(Long userId, Long languageId) {
        String jsonPayload = gson.toJson(new LanguageUpdateRequest(languageId));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_SERVER_URL + "/languages/users/" + userId + "/language"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
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
    private static class LanguageUpdateRequest {
        private final Long languageId;

        public LanguageUpdateRequest(Long languageId) {
            this.languageId = languageId;
        }
    }
    private void fetchLanguages() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_SERVER_URL + "/languages/names"))
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
                .uri(URI.create(BASE_SERVER_URL + "/languages/names/id?name=" + URLEncoder.encode(selectedLanguage, StandardCharsets.UTF_8)))
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

