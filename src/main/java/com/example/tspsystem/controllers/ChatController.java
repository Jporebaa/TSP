package com.example.tspsystem.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListView;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;


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
    private void initialize() {
        // Inicjalizacja, jeśli potrzebna
    }

    @FXML
    private void sendMessage() {
        // Pobranie wiadomości z pola tekstowego
        String message = messageInput.getText().trim();

        if (!message.isEmpty()) {
            // Tutaj można zaimplementować logikę wysyłania wiadomości do serwera
            System.out.println("Wiadomość do wysłania: " + message);

            // Dodanie wiadomości do listy w interfejsie użytkownika
            messageDisplay.getItems().add(message);

            // Wyczyszczenie pola tekstowego
            messageInput.clear();
        }
    }


    private void sendToServer(String message) {
        try {
            // Zakładamy, że serwer oczekuje JSON-a, więc konstruujemy proste ciało JSON
            String jsonPayload = "{\"message\":\"" + message.replace("\"", "\\\"") + "\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:5432/api/messages")) // Adres URL serwera
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .build();

            // Asynchroniczne wysyłanie wiadomości do serwera
            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(responseBody -> {
                        // Logika po otrzymaniu odpowiedzi
                        System.out.println("Odpowiedź serwera: " + responseBody);
                    });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

