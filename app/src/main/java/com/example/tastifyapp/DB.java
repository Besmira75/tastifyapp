package com.example.tastifyapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "recipes.db";
    private static final int DATABASE_VERSION = 1;

    // Constructor
    public DB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // User Table
        db.execSQL("CREATE TABLE User (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "email TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL);");
        db.execSQL("CREATE INDEX idx_username ON User (username);");
        db.execSQL("CREATE INDEX idx_email ON User (email);");

        // Recipe Table
        db.execSQL("CREATE TABLE Recipe (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "instructions TEXT NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES User (id));");
        db.execSQL("CREATE INDEX idx_user_id ON Recipe (user_id);");
        db.execSQL("CREATE INDEX idx_title ON Recipe (title);");

        // RecipeIngredient Table
        db.execSQL("CREATE TABLE RecipeIngredient (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, " +
                "ingredient_id INTEGER NOT NULL, " +
                "sasia REAL NOT NULL, " +
                "FOREIGN KEY (recipe_id) REFERENCES Recipe (id), " +
                "FOREIGN KEY (ingredient_id) REFERENCES Ingredient (id));");
        db.execSQL("CREATE INDEX idx_recipe_id ON RecipeIngredient (recipe_id);");
        db.execSQL("CREATE INDEX idx_ingredient_id ON RecipeIngredient (ingredient_id);");

        // Ingredient Table
        db.execSQL("CREATE TABLE Ingredient (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "emri TEXT NOT NULL, " +
                "category_id INTEGER NOT NULL, " +
                "FOREIGN KEY (category_id) REFERENCES Category (id));");
        db.execSQL("CREATE INDEX idx_emri ON Ingredient (emri);");
        db.execSQL("CREATE INDEX idx_category_id ON Ingredient (category_id);");

        // Category Table
        db.execSQL("CREATE TABLE Category (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category TEXT NOT NULL UNIQUE);");
        db.execSQL("CREATE INDEX idx_category ON Category (category);");

        // Image Table
        db.execSQL("CREATE TABLE Image (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, " +
                "image_url TEXT NOT NULL, " +
                "FOREIGN KEY (recipe_id) REFERENCES Recipe (id));");
        db.execSQL("CREATE INDEX idx_recipe_id ON Image (recipe_id);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop tables if they exist and recreate them
        db.execSQL("DROP TABLE IF EXISTS User");
        db.execSQL("DROP TABLE IF EXISTS Category");

        onCreate(db);
    }

}
