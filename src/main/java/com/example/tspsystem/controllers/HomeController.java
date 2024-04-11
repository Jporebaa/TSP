package com.example.tspsystem.controllers;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class HomeController {

    @FXML
    private void handlePlusButtonAction(ActionEvent event) {
        try {
            Parent groupsView = FXMLLoader.load(getClass().getResource("/com/example/tspsystem/groups.fxml"));

            Scene groupsScene = new Scene(groupsView);

            Stage window = (Stage) ((Node)event.getSource()).getScene().getWindow();
            window.setScene(groupsScene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}