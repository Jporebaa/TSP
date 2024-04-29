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
import org.java_websocket.client.WebSocketClient;
import java.net.URI;


import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.net.URISyntaxException;

public class ChatController {

    @FXML
    private TextArea messageInput;

    @FXML
    private ListView<String> messageDisplay;

    @FXML
    private Button sendButton;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String userName; // Przechowuje nazwę użytkownika czatu
    private WebSocketClient webSocketClient;

    @FXML
    private void initialize() {
        try {
            webSocketClient = new ChatWebSocketClient(new URI("ws://localhost:8080/ws"));
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        sendButton.setOnAction(e -> sendMessage());
    }


    public void initData(String userName) {
        this.userName = userName;
        // Możliwość zaktualizowania UI, np. ustawienie tytułu okna czatu
        messageDisplay.getItems().clear(); // Czyszczenie listy na wypadek nowego użytkownika
        System.out.println("Rozpoczęto czat z użytkownikiem: " + userName);
        // Możliwość załadowania historii czatu z serwera
    }

    private void connectWebSocket() {
        try {
            webSocketClient = new ChatWebSocketClient(new URI("ws://localhost:8080/ws"));
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty() && webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message); // Wysyłanie wiadomości przez WebSocket
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
