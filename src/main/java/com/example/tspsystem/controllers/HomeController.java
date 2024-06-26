package com.example.tspsystem.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import com.example.tspsystem.model.ChatGroup;
import java.sql.SQLException;

public class HomeController {

    @FXML
    private ListView<ChatGroup> groupListView;

    private ObservableList<ChatGroup> groupList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadGroupData();
        groupListView.setItems(groupList);
        groupListView.setOnMouseClicked(this::handleGroupClick);
        updateUserButton("Nazwa Uzytkownika");
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
            chatController.initializeWithGroup(selectedGroup);  // This method should set the group in the ChatController

            // Uzyskanie dostępu do głównej sceny, zakładając, że 'mainStage' to referencja do głównego okna
            Stage mainStage = (Stage) groupListView.getScene().getWindow();
            mainStage.getScene().setRoot(chatView);  // Ustawienie nowego widoku jako głównego
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadGroupData() {
        String url = "jdbc:postgresql://localhost:5432/TSP";
        String user = "postgres";
        String password = "12345";

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

    @FXML
    private Button userButton;


    public void updateUserButton(String userName) {
        userButton.setText(userName);
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
            Parent loginView = FXMLLoader.load(getClass().getResource("/com/example/tspsystem/login.fxml"));
            Scene loginScene = new Scene(loginView);

            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(loginScene);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
