package com.example.tspsystem.model;

public class User {
    private Integer userId;
    private String name;
    private String password; // W praktyce nie przechowuj hasła w kliencie, chyba że jest to absolutnie konieczne
    private Integer languageId;
    private String login;

    // Konstruktor bezparametrowy
    public User() {
    }

    // Konstruktor z wszystkimi parametrami
    public User(Integer userId, String name, String password, Integer languageId, String login) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.languageId = languageId;
        this.login = login;
    }

    // Getters and setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    // toString metoda dla wygodniejszego debugowania i wyświetlania danych
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", languageId=" + languageId +
                ", login='" + login + '\'' +
                '}';
    }
}
