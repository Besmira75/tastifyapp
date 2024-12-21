package com.example.tastifyapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "recipes.db";
    private static final int DATABASE_VERSION = 2;

    // Constructor
    public DB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // User Table

            // User Table
            db.execSQL("CREATE TABLE User (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " + // Add the name column
                    "email TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL);");
            db.execSQL("CREATE INDEX idx_username ON User (name);");
            db.execSQL("CREATE INDEX idx_email ON User (email);");

            // Add other table creations here...



        // Check if the index exists before creating it
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_username';", null);
        if (cursor.getCount() == 0) {
            db.execSQL("CREATE INDEX idx_username ON User (username);");
        }
        cursor.close();

        cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_email';", null);
        if (cursor.getCount() == 0) {
            db.execSQL("CREATE INDEX idx_email ON User (email);");
        }
        cursor.close();

        // Recipe Table
        db.execSQL("CREATE TABLE IF NOT EXISTS Recipe (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "instructions TEXT NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES User (id));");

        // Check if the index exists before creating it
        cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_user_id';", null);
        if (cursor.getCount() == 0) {
            db.execSQL("CREATE INDEX idx_user_id ON Recipe (user_id);");
        }
        cursor.close();

        cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_title';", null);
        if (cursor.getCount() == 0) {
            db.execSQL("CREATE INDEX idx_title ON Recipe (title);");
        }
        cursor.close();

        // RecipeIngredient Table
        db.execSQL("CREATE TABLE IF NOT EXISTS RecipeIngredient (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, " +
                "ingredient_id INTEGER NOT NULL, " +
                "sasia REAL NOT NULL, " +
                "FOREIGN KEY (recipe_id) REFERENCES Recipe (id), " +
                "FOREIGN KEY (ingredient_id) REFERENCES Ingredient (id));");

        // Check if the index exists before creating it
        cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_recipe_id';", null);
        if (cursor.getCount() == 0) {
            db.execSQL("CREATE INDEX idx_recipe_id ON RecipeIngredient (recipe_id);");
        }
        cursor.close();

        cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_ingredient_id';", null);
        if (cursor.getCount() == 0) {
            db.execSQL("CREATE INDEX idx_ingredient_id ON RecipeIngredient (ingredient_id);");
        }
        cursor.close();

        // Ingredient Table
        db.execSQL("CREATE TABLE IF NOT EXISTS Ingredient (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "emri TEXT NOT NULL, " +
                "category_id INTEGER NOT NULL, " +
                "FOREIGN KEY (category_id) REFERENCES Category (id));");

        // Check if the index exists before creating it
        cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_emri';", null);
        if (cursor.getCount() == 0) {
            db.execSQL("CREATE INDEX idx_emri ON Ingredient (emri);");
        }
        cursor.close();

        cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_category_id';", null);
        if (cursor.getCount() == 0) {
            db.execSQL("CREATE INDEX idx_category_id ON Ingredient (category_id);");
        }
        cursor.close();

        // Category Table
        db.execSQL("CREATE TABLE IF NOT EXISTS Category (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category TEXT NOT NULL UNIQUE);");

        // Check if the index exists before creating it
        cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_category';", null);
        if (cursor.getCount() == 0) {
            db.execSQL("CREATE INDEX idx_category ON Category (category);");
        }
        cursor.close();

        // Image Table
        db.execSQL("CREATE TABLE IF NOT EXISTS Image (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, " +
                "image_url TEXT NOT NULL, " +
                "FOREIGN KEY (recipe_id) REFERENCES Recipe (id));");

        // Check if the index exists before creating it
        cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='index' AND name='idx_recipe_id_image';", null);
        if (cursor.getCount() == 0) {
            db.execSQL("CREATE INDEX idx_recipe_id_image ON Image (recipe_id);");
        }
        cursor.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop tables if they exist and recreate them
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE User ADD COLUMN name TEXT;");
        }

        // Drop tables if they exist and recreate them for the newer version
        db.execSQL("DROP TABLE IF EXISTS User");
        db.execSQL("DROP TABLE IF EXISTS Category");

        onCreate(db);
    }
    public boolean insertUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name); // Add name to values
        contentValues.put("email", email);
        contentValues.put("password", password); // Store hashed password

        long result = db.insert("User", null, contentValues);
        db.close();
        return result != -1; // Return true if insert is successful
    }




    public boolean checkUserEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM User WHERE email = ?", new String[]{email});

        // If a record with the given email exists, cursor will have a result
        boolean userExists = cursor.getCount() > 0;
        cursor.close();
        db.close();

        return userExists; // Return true if user exists, false if not
    }

}
