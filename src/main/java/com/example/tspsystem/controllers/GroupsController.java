package com.example.tspsystem.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class GroupsController {

    @FXML
    private ComboBox<String> comboBoxUsers;
    @FXML
    private TextField textFieldGroupName;
    @FXML
    private ListView<String> selectedUsersListView;
    @FXML
    private Button showSelectedUsersButton, removeSelectedUserButton, createGroupButton, backButton;
    @FXML
    private TableView<String> usersTableView;
    @FXML
    private TableColumn<String, String> userNameColumn;

    private ObservableList<String> selectedUsers = FXCollections.observableArrayList();
    private ObservableList<String> allUsers = FXCollections.observableArrayList();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper

    @FXML
    private void initialize() {
        loadAllUsersData();
        selectedUsersListView.setItems(selectedUsers);
        userNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        usersTableView.setItems(allUsers); // This line might be not needed if we populate allUsers elsewhere
    }

    private void loadAllUsersData() {
        String url = "jdbc:postgresql://localhost:5432/TSP";
        String dbUser = "postgres"; // Twoja nazwa użytkownika bazy danych
        String dbPassword = "12345"; // Twoje hasło bazy danych
        try {
            Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);
            Statement stmt = conn.createStatement();
            String query = "SELECT login FROM users"; // Query to fetch user names
            ResultSet rs = stmt.executeQuery(query);

            allUsers.clear(); // Clear the existing user data
            while (rs.next()) {
                allUsers.add(rs.getString("login")); // Add each user to the list
            }
            comboBoxUsers.setItems(allUsers); // Set items in comboBox

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            System.out.println("Dodaj co najmniej jednego użytkownika i nazwę grupy.");
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

    @FXML
    private void handleBackAction(ActionEvent event) {
        try {
            Parent homeView = FXMLLoader.load(getClass().getResource("/com/example/tspsystem/home.fxml"));
            Scene homeScene = new Scene(homeView);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(homeScene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createGroup() {
        try {
            String groupName = textFieldGroupName.getText();
            List<String> users = new ArrayList<>(selectedUsers);
            Map<String, Object> data = new HashMap<>();
            data.put("group_name", groupName);
            data.put("login", users);

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
                        // Po utworzeniu grupy możesz chcieć zaktualizować interfejs użytkownika lub wykonać inne czynności
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
