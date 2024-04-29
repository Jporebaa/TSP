package com.example.tspsystem.controllers;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.net.URISyntaxException;

public class ChatWebSocketClient extends WebSocketClient {

    public ChatWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to the server.");
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
        // Możesz także dodawać wiadomości do interfejsu użytkownika tutaj, pamiętając o wątkach JavaFX
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed by " + (remote ? "remote peer" : "us") + ", reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("An error occurred:" + ex.getMessage());
    }
}
