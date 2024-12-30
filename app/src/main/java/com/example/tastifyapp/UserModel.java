package com.example.tastifyapp;

public class UserModel {
    private int id;
    private String name;
    private String email;

    // Constructor
    public UserModel(int id, String name, String email){
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    // Setters (if needed)
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
}
