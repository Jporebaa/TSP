package com.example.tspsystem.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private static class SessionManager {
        private static UserSession currentUserSession;

        public static UserSession getCurrentUserSession() {
            return currentUserSession;
        }

        public static void setCurrentUserSession(UserSession userSession) {
            SessionManager.currentUserSession = userSession;
        }

        public static void clearSession() {
            currentUserSession = null;
        }
    }

    public static class UserSession {
        private Integer userId;

        public UserSession(Integer userId) {
            this.userId = userId;
        }

        public Integer getUserId() {
            return userId;
        }
    }

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
                    // Przekształcanie odpowiedzi JSON na mapę
                    Map<String,Object> responseMap = new Gson().fromJson(response, new TypeToken<Map<String,Object>>(){}.getType());

                    if (!responseMap.containsKey("error")) {
                        // Pobranie userId z odpowiedzi i ustawienie sesji
                        Number userId = (Number) responseMap.get("userId");
                        SessionManager.setCurrentUserSession(new UserSession(userId.intValue()));
                        redirectToHome();
                    } else {
                        Platform.runLater(() -> showErrorMessage("Nieprawidłowy login lub hasło."));
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    Platform.runLater(() -> showErrorMessage("Błąd połączenia z serwerem."));
                    return null;
                });
    }

    private void redirectToHome() {
        Platform.runLater(() -> {
            try {

                Parent homeView = FXMLLoader.load(getClass().getResource("/com/example/tspsystem/home.fxml"));
                Scene homeScene = new Scene(homeView);


                Stage window = (Stage) usernameField.getScene().getWindow();
                window.setScene(homeScene);
                window.show();
            } catch (IOException e) {

                e.printStackTrace();
            }
        });
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

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
