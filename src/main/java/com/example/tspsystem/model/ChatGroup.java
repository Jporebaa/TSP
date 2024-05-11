package com.example.tspsystem.model;

public class ChatGroup {
    private int id;
    private String name;

    // No-argument constructor
    public ChatGroup() {
    }

    // Constructor that takes an ID and a name
    public ChatGroup(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name; // This will help in displaying the name in ListView
    }
}
