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

public class GroupsController {

    @FXML
    private ComboBox<String> comboBoxUsers; // ComboBox do wyboru użytkowników
    @FXML
    private TextField textFieldGroupName; // Pole tekstowe do wprowadzania nazwy grupy
    @FXML
    private ListView<String> selectedUsersListView; // ListView do wyświetlania wybranych użytkowników
    @FXML
    private Button showSelectedUsersButton, removeSelectedUserButton, createGroupButton; // Przyciski
    @FXML
    private TableView<String> usersTableView; // TableView do wyświetlania wybranych użytkowników
    @FXML
    private TableColumn<String, String> userNameColumn; // Kolumna z nazwami użytkowników

    private ObservableList<String> selectedUsers = FXCollections.observableArrayList(); // Lista wybranych użytkowników
    private ObservableList<String> allUsers = FXCollections.observableArrayList("User1", "User2", "User3", "User4"); // Lista wszystkich użytkowników
    private final HttpClient httpClient = HttpClient.newHttpClient(); // Obiekt klienta HTTP

    @FXML
    private void initialize() {
        selectedUsersListView.setItems(selectedUsers); // Ustawienie listy wybranych użytkowników w ListView
        comboBoxUsers.setItems(allUsers); // Ustawienie wszystkich użytkowników w ComboBox

        userNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue())); // Ustawienie wartości w kolumnie TableView
    }

    @FXML
    private void onAddUser() {
        String selectedUser = comboBoxUsers.getSelectionModel().getSelectedItem(); // Pobranie wybranego użytkownika
        if (selectedUser != null && !selectedUsers.contains(selectedUser)) { // Sprawdzenie czy użytkownik jest wybrany i czy nie jest już na liście
            selectedUsers.add(selectedUser); // Dodanie użytkownika do listy wybranych
            allUsers.remove(selectedUser); // Usunięcie użytkownika z listy wszystkich
            comboBoxUsers.setItems(allUsers); // Odświeżenie z dostępnymi użytkownikami
        }
    }

    @FXML
    private void handleShowSelectedUsers() {
        usersTableView.getItems().clear(); // Wyczyszczenie tabeli przed dodaniem nowych danych
        usersTableView.setItems(FXCollections.observableArrayList(selectedUsers)); // Dodanie wybranych użytkowników do tabeli
    }

    @FXML
    private void handleCreateGroup() {
        if (!selectedUsers.isEmpty() && !textFieldGroupName.getText().trim().isEmpty()) { // Sprawdzenie czy wybrano użytkowników i czy wprowadzono nazwę grupy
            createGroup();
        } else {
            System.out.println("Proszę dodaj co najmniej jednego użytkownika i nazwę grupy.");
        }
    }

    @FXML
    private void handleRemoveSelectedUser() {
        String selectedUser = usersTableView.getSelectionModel().getSelectedItem(); // Pobranie zaznaczonego użytkownika z tabeli
        if (selectedUser != null) { // Sprawdzenie czy użytkownik jest zaznaczony
            selectedUsers.remove(selectedUser); // Usunięcie użytkownika z listy wybranych
            allUsers.add(selectedUser); // Opcjonalnie dodanie użytkownika z powrotem do listy wszystkich
            comboBoxUsers.setItems(allUsers);
            handleShowSelectedUsers(); // Odświeżenie tabeli
        }
    }

    private void createGroup() {
        String groupName = textFieldGroupName.getText(); // Pobranie nazwy grupy
        ArrayList<String> users = new ArrayList<>(selectedUsers); // Konwersja listy wybranych użytkowników na ArrayList

        // JSON
        String json = "{\"groupName\":\"" + groupName + "\",\"users\":" + users.toString() + "}"; // Tworzenie JSONa z nazwą grupy i listą użytkowników

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/groups"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    // Obsługa
                    Platform.runLater(() -> {
                        System.out.println("Grupa utworzona pomyślnie: " + response);
                    });
                })
                .exceptionally(e -> {
                   // Wyjatki
                    e.printStackTrace(); // Wypisanie informacji o wyjątku
                    return null;
                });
    }
}
