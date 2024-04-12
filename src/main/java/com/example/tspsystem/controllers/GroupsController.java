package com.example.tspsystem.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GroupsController {

    @FXML
    private ComboBox<String> comboBoxUsers;
    @FXML
    private TextField textFieldGroupName;
    @FXML
    private ListView<String> selectedUsersListView;
    @FXML
    private Button showSelectedUsersButton, removeSelectedUserButton, createGroupButton;
    @FXML
    private TableView<String> usersTableView;
    @FXML
    private TableColumn<String, String> userNameColumn;

    private ObservableList<String> selectedUsers = FXCollections.observableArrayList();
    private ObservableList<String> allUsers = FXCollections.observableArrayList("User1", "User2", "User3", "User4");
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper

    @FXML
    private void initialize() {
        selectedUsersListView.setItems(selectedUsers);
        comboBoxUsers.setItems(allUsers);
        userNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
    }

    @FXML
    private void onAddUser() {
        String selectedUser = comboBoxUsers.getSelectionModel().getSelectedItem();
        if (selectedUser != null && !selectedUsers.contains(selectedUser)) {
            selectedUsers.add(selectedUser);
            allUsers.remove(selectedUser);
            comboBoxUsers.setItems(allUsers);
        }
    }

    @FXML
    private void handleShowSelectedUsers() {
        usersTableView.getItems().clear();
        usersTableView.setItems(FXCollections.observableArrayList(selectedUsers));
    }

    @FXML
    private void handleCreateGroup() {
        if (!selectedUsers.isEmpty() && !textFieldGroupName.getText().trim().isEmpty()) {
            createGroup();
        } else {
            System.out.println("\n" +
                    "Dodaj co najmniej jednego użytkownika i nazwę grupy.");
        }
    }

    @FXML
    private void handleRemoveSelectedUser() {
        String selectedUser = usersTableView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            selectedUsers.remove(selectedUser);
            allUsers.add(selectedUser);
            comboBoxUsers.setItems(allUsers);
            handleShowSelectedUsers();
        }
    }

    private void createGroup() {
        try {
            String groupName = textFieldGroupName.getText();
            List<String> users = new ArrayList<>(selectedUsers);
            Map<String, Object> data = new HashMap<>();
            data.put("groupName", groupName);
            data.put("users", users);

            String json = objectMapper.writeValueAsString(data);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/groups"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(response -> Platform.runLater(() -> {
                        System.out.println("Grupa utworzona pomyślnie: " + response);
                    }))
                    .exceptionally(e -> {
                        e.printStackTrace();
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
