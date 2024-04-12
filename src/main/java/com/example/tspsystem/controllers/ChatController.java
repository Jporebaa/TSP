package com.example.tspsystem.controllers;

<<<<<<< HEAD
import com.example.tspsystem.HelloApplication;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class ChatController {
    @FXML
    private void handleLogoutButtonAction(javafx.event.ActionEvent event) throws IOException {
        // Ładuje layout dashboard
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

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

