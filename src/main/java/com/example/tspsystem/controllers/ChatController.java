package com.example.tspsystem.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import com.example.tspsystem.model.ChatGroup;
import javafx.stage.Stage;

public class ChatController {

    @FXML
    private TextArea messageInput;

    @FXML
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @FXML
    private ListView<String> messageDisplay;

    @FXML
    private Button sendButton;

    @FXML
    private ChatGroup currentGroup; // Zmienna przechowująca aktualną grupę czatu

    @FXML
    private void initialize() {
        // Możesz zaimplementować logikę inicjalizacji ogólnej, jeśli potrzebna
    }

   @FXML
    public void initializeWithGroup(ChatGroup group) {
        this.currentGroup = group;
        updateChatTitle();
        loadChatHistory();
    }

    @FXML
    private void updateChatTitle() {
        // Możesz zaktualizować tytuł czatu na pasku aplikacji, jeśli jest taka potrzeba
        if (messageInput != null) {
            messageInput.setPromptText("Napisz wiadomość do: " + currentGroup.getName());
        }
    }

    @FXML
    private void loadChatHistory() {
        // Tutaj możesz dodać logikę do ładowania historii czatu dla grupy
    }

    @FXML
    private void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/tspsystem/home.fxml"));
            Parent homeView = loader.load();
            Stage stage = (Stage) sendButton.getScene().getWindow();
            stage.getScene().setRoot(homeView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            sendToServer(message);
            messageDisplay.getItems().add(message); // Można dodać użytkownika i czas do wyświetlanej wiadomości
            messageInput.clear();
        }
    }

    private void sendToServer(String message) {
        try {
            String jsonPayload = "{\"group_id\": " + currentGroup.getId() + ", \"message\":\"" + message.replace("\"", "\\\"") + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/messages")) // Upewnij się, że to jest właściwy endpoint
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(responseBody -> {
                        System.out.println("Odpowiedź serwera: " + responseBody);
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
