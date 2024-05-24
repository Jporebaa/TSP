package com.example.tspsystem.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.example.tspsystem.model.ChatGroup;

public class HomeController {

    @FXML
    private ListView<ChatGroup> groupListView;

    private ObservableList<ChatGroup> groupList = FXCollections.observableArrayList();

    @FXML
    private Button userButton;

    @FXML
    public void initialize() {
        loadGroupData();
        groupListView.setItems(groupList);
        groupListView.setOnMouseClicked(this::handleGroupClick);
        fetchAndSetUserName();
    }

    @FXML
    public void handleGroupClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            ChatGroup selectedGroup = groupListView.getSelectionModel().getSelectedItem();
            if (selectedGroup != null) {
                openChatWindow(selectedGroup);
            }
        }
    }

    @FXML
    private void openChatWindow(ChatGroup selectedGroup) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/tspsystem/chat_layout.fxml"));
            Parent chatView = loader.load();

            ChatController chatController = loader.getController();
            chatController.initializeWithGroup(selectedGroup);

            Stage mainStage = (Stage) groupListView.getScene().getWindow();
            mainStage.getScene().setRoot(chatView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadGroupData() {
        String url = "jdbc:postgresql://localhost:5432/TSP";
        String user = "postgres";
        String password = "zaq1";

        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, name FROM chat_group")) { // Adjusted query
                while (rs.next()) {
                    int id = rs.getInt("id"); // Adjusted column name
                    String name = rs.getString("name"); // Adjusted column name
                    groupList.add(new ChatGroup(id, name));
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void fetchAndSetUserName() {
        Platform.runLater(() -> {
            LoginController.UserSession userSession = LoginController.SessionManager.getCurrentUserSession();
            if (userSession != null) {
                String username = userSession.getUsername();
                Integer userId = userSession.getUserId();
                System.out.println("Fetched user session: " + userSession);
                System.out.println("Fetched username: " + username);
                System.out.println("Fetched user ID: " + userId);
                userButton.setText("Użytkownik: " + username );
                userButton.setStyle("-fx-text-fill: red;");
            } else {
                System.out.println("User session is null. No user is currently logged in.");
                userButton.setText("Nie zalogowano");
            }
        });
    }

    @FXML
    private void handleSettingsButtonAction(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/tspsystem/settings.fxml"));
            Parent settingsView = fxmlLoader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(settingsView));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePlusButtonAction(ActionEvent event) {
        try {
            Parent groupsView = FXMLLoader.load(getClass().getResource("/com/example/tspsystem/groups.fxml"));
            Scene groupsScene = new Scene(groupsView);

            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(groupsScene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogoutAction(ActionEvent event) {
        try {
            LoginController.SessionManager.clearSession();
            Parent loginView = FXMLLoader.load(getClass().getResource("/com/example/tspsystem/login.fxml"));
            Scene loginScene = new Scene(loginView);

            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(loginScene);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
