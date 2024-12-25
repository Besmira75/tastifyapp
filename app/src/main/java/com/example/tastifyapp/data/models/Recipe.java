package com.example.tastifyapp.data.models;

public class Recipe {
    private int id;
    private int userId;
    private String title;
    private String description;
    private String instructions;

    // Constructor
    public Recipe(int id, int userId, String title, String description, String instructions) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.instructions = instructions;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getInstructions() { return instructions; }
}
