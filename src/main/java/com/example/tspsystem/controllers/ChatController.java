package com.example.tspsystem.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListView;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class ChatController {

    @FXML
    private TextArea messageInput;

    @FXML
    private ListView<String> messageDisplay;

    @FXML
    private Button sendButton;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String userName; // Przechowuje nazwę użytkownika czatu

    @FXML
    private void initialize() {
        // Inicjalizacja, jeśli potrzebna
        // Ustawienie akcji dla przycisku wysyłania
        sendButton.setOnAction(e -> sendMessage());
    }

    public void initData(String userName) {
        this.userName = userName;
        // Możliwość zaktualizowania UI, np. ustawienie tytułu okna czatu
        messageDisplay.getItems().clear(); // Czyszczenie listy na wypadek nowego użytkownika
        System.out.println("Rozpoczęto czat z użytkownikiem: " + userName);
        // Możliwość załadowania historii czatu z serwera
    }

    @FXML
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            sendToServer(message); // Wysyłanie wiadomości do serwera
            messageDisplay.getItems().add("Ja: " + message); // Dodanie wiadomości do UI
            messageInput.clear(); // Czyszczenie pola wprowadzania
        }
    }

    private void sendToServer(String message) {
        try {
            String jsonPayload = "{\"message\":\"" + message.replace("\"", "\\\"") + "\", \"user\":\"" + userName + "\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:5432/api/Chat_messages")) // Adres URL serwera
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(responseBody -> System.out.println("Odpowiedź serwera: " + responseBody))
                    .exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        try {
            // Załaduj widok home.fxml
            Parent homeView = FXMLLoader.load(getClass().getResource("/com/example/tspsystem/home.fxml"));
            Scene homeScene = new Scene(homeView);

            // Pobierz bieżący stage i ustaw nową scenę
            Stage window = (Stage) ((Node) messageInput).getScene().getWindow();
            window.setScene(homeScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
