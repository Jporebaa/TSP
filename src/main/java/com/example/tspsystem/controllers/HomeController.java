package com.example.tspsystem.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class HomeController {
    @FXML
    private ListView<String> groupList; // Referencja do ListView grup.

    // Lista obserwowalna przechowująca dane grup.
    private ObservableList<String> groupData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadGroupData();
        setupGroupListClickListener(); // Ustawienie obsługi kliknięć dla listy grup
    }

    private void loadGroupData() {
        String url = "jdbc:postgresql://localhost:5432/TSP";
        String user = "postgres";
        String password = "jesthaslo123";
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            String query = "SELECT group_name FROM chat_groups";
            ResultSet rs = stmt.executeQuery(query);
            groupData.clear();
            while (rs.next()) {
                groupData.add(rs.getString("group_name"));
            }
            groupList.setItems(groupData);
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupGroupListClickListener() {
        groupList.setOnMouseClicked((MouseEvent click) -> {
            if (click.getClickCount() == 2) {
                String selectedGroup = groupList.getSelectionModel().getSelectedItem();
                if (selectedGroup != null) {
                    switchToGroupChatView(selectedGroup);
                }
            }
        });
    }

    private void switchToGroupChatView(String groupName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/tspsystem/chat_layout.fxml"));
            Parent groupChatView = loader.load();
            ChatController groupChatController = loader.getController();
            groupChatController.initData(groupName);
            Scene groupChatScene = new Scene(groupChatView);
            Stage window = (Stage) groupList.getScene().getWindow();
            window.setScene(groupChatScene);
            window.show();
        } catch (Exception e) {
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
}
