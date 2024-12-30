// com/example/tastifyapp/RecipeModel.java
package com.example.tastifyapp;

public class RecipeModel {
    private int id;
    private int userId; // Owner's user ID
    private String title;
    private String description;
    private String instructions;
    private int categoryId;
    private String imageUrl;
    private String name; // New field for creator's username

    // Constructor including username
    public RecipeModel(int id, int userId, String title, String description, String instructions, int categoryId, String imageUrl, String name) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.instructions = instructions;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
        this.name = name;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getInstructions() { return instructions; }
    public int getCategoryId() { return categoryId; }
    public String getImageUrl() { return imageUrl; }
    public String getName() { return name; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
