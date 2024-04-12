package com.example.tspsystem.controllers;

import com.example.tspsystem.model.ChatGroup;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListView;

import javax.websocket.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

import com.example.tspsystem.model.ChatGroup;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

@ClientEndpoint
public class ChatController {
    @FXML
    private TextArea messageInput;

    @FXML
    private ListView<String> messageDisplay;

    @FXML
    private Button sendButton;

    private Session session;


    @FXML
    private void handleLogoutButtonAction(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/path/to/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    private void connectWebSocket() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            URI uri = URI.create("ws://localhost:8080/chat");
            session = container.connectToServer(this, uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty() && session != null) {
            try {
                session.getBasicRemote().sendText(message);
                messageInput.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnMessage
    public void onMessage(String message) {
        javafx.application.Platform.runLater(() -> messageDisplay.getItems().add(message));
    }



    @FXML
    private ListView<String> groupsList;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        connectWebSocket();
        loadGroups();
    }

    private void loadGroups() {
        HttpRequest request = HttpRequest.newBuilder()  // Użyj HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/groups"))
                .GET()  // Metoda GET jest domyślna, ale jej jawnie wskazanie jest w porządku.
                .build();  // Poprawne zakończenie budowania zapytania

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseGroups)
                .thenAccept(groups -> {
                    javafx.application.Platform.runLater(() -> {
                        groupsList.getItems().setAll(groups);
                    });
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }


    private List<String> parseGroups(String jsonResponse) {
        try {
            List<ChatGroup> groups = objectMapper.readValue(jsonResponse, new TypeReference<List<ChatGroup>>() {});
            return groups.stream().map(ChatGroup::getName).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
