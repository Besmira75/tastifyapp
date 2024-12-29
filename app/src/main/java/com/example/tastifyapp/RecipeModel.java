package com.example.tastifyapp;

public class RecipeModel {
    private int id;
    private int userId;
    private String title;
    private String description;
    private String instructions;
    private int categoryId;
    private String imageUrl; // New field

    // Constructor including imageUrl
    public RecipeModel(int id, int userId, String title, String description, String instructions, int categoryId, String imageUrl) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.instructions = instructions;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getInstructions() {
        return instructions;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Optionally, add setters if needed
}
