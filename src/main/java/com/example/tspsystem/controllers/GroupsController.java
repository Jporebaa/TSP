package com.example.tspsystem.controllers;

import com.example.tspsystem.HelloApplication;
import com.example.tspsystem.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupsController {

    @FXML
    private ComboBox<User> comboBoxUsers;
    @FXML
    private TextField textFieldGroupName;
    @FXML
    private ListView<User> selectedUsersListView;
    @FXML
    private TableView<User> usersTableView;
    @FXML
    private TableColumn<User, String> userNameColumn;

    private ObservableList<User> selectedUsers = FXCollections.observableArrayList();
    private ObservableList<User> allUsers = FXCollections.observableArrayList();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    private void handleHomeButtonAction(javafx.event.ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("home.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void initialize() {
        selectedUsersListView.setItems(selectedUsers);
        selectedUsersListView.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? null : item.getName());
            }
        });

        fetchAllUsers();
        comboBoxUsers.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? null : item.getName());
            }
        });
        comboBoxUsers.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? null : item.getName());
            }
        });

        userNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        usersTableView.setItems(FXCollections.observableArrayList(selectedUsers));
        usersTableView.getColumns().clear(); // Clear existing columns
        usersTableView.getColumns().add(userNameColumn); // Add the user name column
    }


    @FXML
    private void handleCreateGroup() {
        if (textFieldGroupName.getText().isEmpty() || selectedUsers.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Nazwa grupy i przynajmniej jeden użytkownik są wymagani.");
            alert.showAndWait();
            return;
        }

        List<Integer> userIds = selectedUsers.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
        String groupName = textFieldGroupName.getText();

        // Tworzenie obiektu JSON do wysłania
        Map<String, Object> groupInfo = new HashMap<>();
        groupInfo.put("groupName", groupName);
        groupInfo.put("userIds", userIds);

        String json = "";
        try {
            json = objectMapper.writeValueAsString(groupInfo);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/groups/create"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Grupa została utworzona: " + response);
                    alert.showAndWait();
                }))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }


    private void fetchAllUsers() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/users"))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseUsers)
                .thenAccept(users -> Platform.runLater(() -> {
                    allUsers.setAll(users);
                    comboBoxUsers.setItems(allUsers);
                }))
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    private List<User> parseUsers(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<User>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    private void onAddUser() {
        User selectedUser = comboBoxUsers.getSelectionModel().getSelectedItem();
        if (selectedUser != null && !selectedUsers.stream().anyMatch(u -> u.getName().equals(selectedUser.getName()))) {
            selectedUsers.add(selectedUser);
            allUsers.remove(selectedUser);
            comboBoxUsers.setItems(FXCollections.observableArrayList(allUsers));
        }
    }

    @FXML
    private void handleRemoveSelectedUser() {
        User selectedUser = usersTableView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            selectedUsers.remove(selectedUser);
            allUsers.add(selectedUser);
            comboBoxUsers.setItems(FXCollections.observableArrayList(allUsers));
            handleShowSelectedUsers();
        }
    }


    @FXML
    private void handleShowSelectedUsers() {
        usersTableView.setItems(FXCollections.observableArrayList(selectedUsers));
    }
}
