package com.example.tspsystem.model;

public class User {
    private Integer userId;
    private String login;
    private String password;
    private Integer languageId;

    // Constructors
    public User() {}

    public User(Integer userId, String login, String password, Integer languageId) {
        this.userId = userId;
        this.login = login;
        this.password = password;
        this.languageId = languageId;
    }

    // Getters and setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    // Override toString for better display
    @Override
    public String toString() {
        return login;
    }
}
