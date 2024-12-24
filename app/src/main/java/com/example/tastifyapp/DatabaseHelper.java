//KY FILE PERDORET PER ME SHTU TE DHENA NE DATABAZE PERMES KODIT - PER SHKAQE TESTUESE

package com.example.tastifyapp;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Name and Version
    private static final String DATABASE_NAME = "recipes.db"; // Your existing database
    private static final int DATABASE_VERSION = 2; // Keep this matching your schema

    // Table Name and Columns
    private static final String TABLE_CATEGORY = "Category"; // Name of your table
    private static final String COLUMN_ID = "id"; // Primary Key column
    private static final String COLUMN_CATEGORY = "category"; // Category name column

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Do nothing here as the database already exists
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if necessary
    }

    // Method to insert categories into the existing table
    public void insertCategories() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Array of categories to insert
        String[] categories = {"Desserts", "Main Course", "Appetizers", "Beverages", "Snacks"};

        for (int i = 0; i < categories.length; i++) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, i + 1); // Assuming IDs start from 1
            values.put(COLUMN_CATEGORY, categories[i]);

            long result = db.insert(TABLE_CATEGORY, null, values);
            if (result == -1) {
                Log.d("DBInsert", "Failed to insert category: " + categories[i]);
            } else {
                Log.d("DBInsert", "Successfully inserted category: " + categories[i]);
            }
        }

        db.close();
    }
}
