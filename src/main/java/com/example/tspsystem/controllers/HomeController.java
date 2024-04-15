package com.example.tspsystem.controllers;

import com.example.tspsystem.HelloApplication;
import com.example.tspsystem.model.ChatGroup;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HomeController {

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
    private void handleSettingsButtonAction(javafx.scene.input.MouseEvent event) throws IOException {
        // Ładuje layout ustawień
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("settings.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleGroupsButtonAction(javafx.scene.input.MouseEvent event) throws IOException {
        // Ładuje layout grup
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("groups.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleChatButtonAction(javafx.scene.input.MouseEvent event) throws IOException {
        // Ładuje layout grup
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("chat_layout.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private ListView<String> groupsList;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
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
