package com.example.tastifyapp;

public class CategoryModel {
    private int id;
    private String categoryName;

    public CategoryModel(int id, String categoryName) {
        this.id = id;
        this.categoryName = categoryName;
    }

    public int getId() {
        return id;
    }

    public String getCategoryName() {
        return categoryName;
    }
}
