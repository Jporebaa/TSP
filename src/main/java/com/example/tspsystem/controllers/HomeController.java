package com.example.tspsystem.controllers;

import com.example.tspsystem.HelloApplication;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

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
    private void handleChatButtonAction(javafx.event.ActionEvent event) throws IOException {
        // Ładuje layout grup
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("chat_layout.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }




}
