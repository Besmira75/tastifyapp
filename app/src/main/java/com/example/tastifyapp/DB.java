package com.example.tastifyapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class DB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "recipes.db";
    private static final int DATABASE_VERSION = 12; // Incremented version for database schema update

    // Constructor
    public DB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // User Table
        db.execSQL("CREATE TABLE IF NOT EXISTS User (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL);"
//                "salt TEXT NOT NULL);"
        );

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_username ON User (name);");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_email ON User (email);");

        // 2FA Table
        db.execSQL("CREATE TABLE IF NOT EXISTS TwoFactorAuth (" +
                "email TEXT PRIMARY KEY, " +
                "two_fa_code TEXT NOT NULL);");

        // Password Reset Table
        db.execSQL("CREATE TABLE IF NOT EXISTS PasswordReset (" +
                "email TEXT PRIMARY KEY, " +
                "reset_code TEXT NOT NULL);");
// Recipe Table
        db.execSQL("CREATE TABLE IF NOT EXISTS Recipe (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "instructions TEXT NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES User (id));");

        // RecipeIngredient Table
        db.execSQL("CREATE TABLE IF NOT EXISTS RecipeIngredient (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, " +
                "ingredient_id INTEGER NOT NULL, " +
                "sasia REAL NOT NULL, " +
                "FOREIGN KEY (recipe_id) REFERENCES Recipe (id), " +
                "FOREIGN KEY (ingredient_id) REFERENCES Ingredient (id));");

        // Ingredient Table
        db.execSQL("CREATE TABLE IF NOT EXISTS Ingredient (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "emri TEXT NOT NULL, " +
                "category_id INTEGER NOT NULL, " +
                "FOREIGN KEY (category_id) REFERENCES Category (id));");

        // Category Table
        db.execSQL("CREATE TABLE IF NOT EXISTS Category (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category TEXT NOT NULL UNIQUE);");

        // Image Table
        db.execSQL("CREATE TABLE IF NOT EXISTS Image (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, " +
                "image_url TEXT NOT NULL, " +
                "FOREIGN KEY (recipe_id) REFERENCES Recipe (id));");




    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            db.execSQL("CREATE TABLE IF NOT EXISTS TwoFactorAuth (" +
                    "email TEXT PRIMARY KEY, " +
                    "two_fa_code TEXT NOT NULL);");
        }
        if (oldVersion < 6) {
            db.execSQL("CREATE TABLE IF NOT EXISTS PasswordReset (" +
                    "email TEXT PRIMARY KEY, " +
                    "reset_code TEXT NOT NULL);");
        }
        if (oldVersion < 7) {
            db.execSQL("CREATE TABLE IF NOT EXISTS Recipe (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "instructions TEXT NOT NULL, " +
                    "FOREIGN KEY (user_id) REFERENCES User (id));");

            db.execSQL("CREATE TABLE IF NOT EXISTS RecipeIngredient (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "recipe_id INTEGER NOT NULL, " +
                    "ingredient_id INTEGER NOT NULL, " +
                    "sasia REAL NOT NULL, " +
                    "FOREIGN KEY (recipe_id) REFERENCES Recipe (id), " +
                    "FOREIGN KEY (ingredient_id) REFERENCES Ingredient (id));");

            db.execSQL("CREATE TABLE IF NOT EXISTS Ingredient (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "emri TEXT NOT NULL, " +
                    "category_id INTEGER NOT NULL, " +
                    "FOREIGN KEY (category_id) REFERENCES Category (id));");

            db.execSQL("CREATE TABLE IF NOT EXISTS Category (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "category TEXT NOT NULL UNIQUE);");



            db.execSQL("CREATE TABLE IF NOT EXISTS Image (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "recipe_id INTEGER NOT NULL, " +
                    "image_url TEXT NOT NULL, " +
                    "FOREIGN KEY (recipe_id) REFERENCES Recipe (id));");
        }
        if (oldVersion < 8) {
            // Remove category_id from Ingredient table
            db.execSQL("ALTER TABLE Ingredient RENAME TO Ingredient_old;");
            db.execSQL("CREATE TABLE Ingredient (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "emri TEXT NOT NULL);");

            // Add category_id to Recipe table
            db.execSQL("ALTER TABLE Recipe ADD COLUMN category_id INTEGER;");

            // Modify sasia column in RecipeIngredient table to TEXT
            db.execSQL("ALTER TABLE RecipeIngredient RENAME TO RecipeIngredient_old;");
            db.execSQL("CREATE TABLE RecipeIngredient (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "recipe_id INTEGER NOT NULL, " +
                    "ingredient_id INTEGER NOT NULL, " +
                    "sasia TEXT NOT NULL, " +
                    "FOREIGN KEY (recipe_id) REFERENCES Recipe (id), " +
                    "FOREIGN KEY (ingredient_id) REFERENCES Ingredient (id));");

            // Drop the old tables since there's no data to preserve
            db.execSQL("DROP TABLE Ingredient_old;");
            db.execSQL("DROP TABLE RecipeIngredient_old;");
        }

        if (oldVersion < 9) {
            // Add salt column to User table if it doesn't exist
            db.execSQL("ALTER TABLE User ADD COLUMN salt TEXT;");
        }
        if(oldVersion < 10) {
            insertCategory(db, "Breakfast");
            insertCategory(db, "Lunch");
            insertCategory(db, "Dinner");
            insertCategory(db, "Dessert");
        }
    }

    // Generate a random salt
    private String generateSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] saltBytes = new byte[16];
        secureRandom.nextBytes(saltBytes);
        return Base64.encodeToString(saltBytes, Base64.NO_WRAP);
    }

    private void insertCategory(SQLiteDatabase db, String category) {
        ContentValues values = new ContentValues();
        values.put("category", category);
        db.insert("Category", null, values);
    }

    public boolean insertCategory(String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("Category", new String[]{"id"}, "category = ?", new String[]{category}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (!exists) {
            ContentValues values = new ContentValues();
            values.put("category", category);
            long result = db.insert("Category", null, values);
            db.close();
            return result != -1;
        }

        db.close();
        return false;
    }

    // Hash the password with the salt
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes());
            byte[] hashedPassword = digest.digest(password.getBytes());
            return Base64.encodeToString(hashedPassword, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteIngredientById(int ingredientId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete("RecipeIngredient", "id = ?", new String[]{String.valueOf(ingredientId)});
        db.close();
        return deletedRows > 0;
    }

    // Insert a new user into the database
    public boolean insertUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        String salt = generateSalt();
        String saltedHash = hashPassword(password, salt);

        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("email", email);
        contentValues.put("password", saltedHash);
        contentValues.put("salt", salt);

        long result = db.insert("User", null, contentValues);
        db.close();
        return result != -1;
    }

    // Inside DB.java

    public int getUserId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM User WHERE email = ?", new String[]{email});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return userId;
    }

    public UserModel getUserById(int userId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("User", new String[]{"id", "name", "email"}, "id = ?", new String[]{String.valueOf(userId)}, null, null, null);
        UserModel user = null;
        if(cursor != null && cursor.moveToFirst()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            user = new UserModel(id, name, email);
            cursor.close();
        }
        db.close();
        return user;
    }


    // Validate user credentials
    public boolean validateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT password, salt FROM User WHERE email = ?", new String[]{email});
        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(0);
            String salt = cursor.getString(1);

            String enteredPasswordHash = hashPassword(password, salt);

            cursor.close();
            db.close();

            return storedPassword.equals(enteredPasswordHash);
        }
        cursor.close();
        db.close();
        return false;
    }

    public List<CategoryModel> getAllCategoriesFull() {
        List<CategoryModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, category FROM Category", null);
        if (cursor.moveToFirst()) {
            do {
                int catId = cursor.getInt(0);
                String catName = cursor.getString(1);
                list.add(new CategoryModel(catId, catName));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public List<RecipeModel> getAllRecipes(){
        List<RecipeModel> recipeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT Recipe.id, Recipe.user_id, Recipe.title, Recipe.description, Recipe.instructions, Recipe.category_id, User.name " +
                "FROM Recipe " +
                "JOIN User ON Recipe.user_id = User.id";
        Cursor cursor = db.rawQuery(query, null);
        if(cursor != null && cursor.moveToFirst()){
            do{
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String instructions = cursor.getString(cursor.getColumnIndexOrThrow("instructions"));
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

                // Fetch image URL
                String imageUrl = getImageUrlByRecipeId(id);

                // Initialize RecipeModel with name
                RecipeModel recipe = new RecipeModel(id, userId, title, description, instructions, categoryId, imageUrl, name);
                recipeList.add(recipe);
            } while(cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return recipeList;
    }

    public List<RecipeModel> getRecipesByCategoryId(int categoryId){
        List<RecipeModel> recipeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT Recipe.id, Recipe.user_id, Recipe.title, Recipe.description, Recipe.instructions, Recipe.category_id, User.name " +
                "FROM Recipe " +
                "JOIN User ON Recipe.user_id = User.id " +
                "WHERE Recipe.category_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(categoryId)});
        if(cursor != null && cursor.moveToFirst()){
            do{
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String instructions = cursor.getString(cursor.getColumnIndexOrThrow("instructions"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

                // Fetch image URL
                String imageUrl = getImageUrlByRecipeId(id);

                // Initialize RecipeModel with username
                RecipeModel recipe = new RecipeModel(id, userId, title, description, instructions, categoryId, imageUrl, name);
                recipeList.add(recipe);
            } while(cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return recipeList;
    }



    // Inside DB.java

    // Inside DB.java
    public RecipeModel getRecipeById(int recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT Recipe.id, Recipe.user_id, Recipe.title, Recipe.description, Recipe.instructions, Recipe.category_id, User.name " +
                "FROM Recipe " +
                "JOIN User ON Recipe.user_id = User.id " +
                "WHERE Recipe.id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(recipeId)});
        RecipeModel recipe = null;
        if(cursor != null && cursor.moveToFirst()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            String instructions = cursor.getString(cursor.getColumnIndexOrThrow("instructions"));
            int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

            // Fetch image URL from Image table
            String imageUrl = getImageUrlByRecipeId(recipeId);

            // Initialize RecipeModel with all details
            recipe = new RecipeModel(id, userId, title, description, instructions, categoryId, imageUrl, name);
            cursor.close();
        }
        db.close();
        return recipe;
    }

    public List<AddRecipe.IngredientQuantity> getIngredientsByRecipeId(int recipeId) {
        List<AddRecipe.IngredientQuantity> ingredients = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT Ingredient.id, Ingredient.emri, RecipeIngredient.sasia FROM RecipeIngredient " +
                "JOIN Ingredient ON RecipeIngredient.ingredient_id = Ingredient.id " +
                "WHERE RecipeIngredient.recipe_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(recipeId)});
        if(cursor != null && cursor.moveToFirst()){
            do{
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("emri"));
                String quantity = cursor.getString(cursor.getColumnIndexOrThrow("sasia"));
                ingredients.add(new AddRecipe.IngredientQuantity(id, name, quantity));
            } while(cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return ingredients;
    }

    public String getImageUrlByRecipeId(int recipeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String imageUrl = null;
        Cursor cursor = db.query("Image", new String[]{"image_url"}, "recipe_id = ?", new String[]{String.valueOf(recipeId)}, null, null, null);
        if(cursor != null && cursor.moveToFirst()){
            imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"));
            cursor.close();
        }
        db.close();
        return imageUrl;
    }
    public RecipeModel getCompleteRecipeById(int recipeId){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT Recipe.id, Recipe.user_id, Recipe.title, Recipe.description, Recipe.instructions, Recipe.category_id, User.name " +
                "FROM Recipe " +
                "JOIN User ON Recipe.user_id = User.id " +
                "WHERE Recipe.id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(recipeId)});
        RecipeModel recipe = null;
        if(cursor != null && cursor.moveToFirst()){
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            String instructions = cursor.getString(cursor.getColumnIndexOrThrow("instructions"));
            int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

            // Fetch image URL
            String imageUrl = getImageUrlByRecipeId(recipeId);

            // Initialize RecipeModel with username
            recipe = new RecipeModel(id, userId, title, description, instructions, categoryId, imageUrl, name);
            cursor.close();
        }
        db.close();
        return recipe;
    }

    public boolean deleteRecipe(int recipeId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try{
            // Delete from Image table
            db.delete("Image", "recipe_id = ?", new String[]{String.valueOf(recipeId)});
            // Delete from RecipeIngredient table
            db.delete("RecipeIngredient", "recipe_id = ?", new String[]{String.valueOf(recipeId)});
            // Delete from Recipe table
            int rows = db.delete("Recipe", "id = ?", new String[]{String.valueOf(recipeId)});
            if(rows > 0){
                db.setTransactionSuccessful();
                return true;
            } else {
                return false;
            }
        } catch(Exception e){
            Log.e("DB", "Error deleting recipe: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }
    public List<RecipeModel> getRecipesByUserId(int userId){
        List<RecipeModel> recipeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT Recipe.id, Recipe.user_id, Recipe.title, Recipe.description, Recipe.instructions, Recipe.category_id, User.name " +
                "FROM Recipe " +
                "JOIN User ON Recipe.user_id = User.id " +
                "WHERE Recipe.user_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if(cursor != null && cursor.moveToFirst()){
            do{
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String instructions = cursor.getString(cursor.getColumnIndexOrThrow("instructions"));
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));

                // Fetch image URL
                String imageUrl = getImageUrlByRecipeId(id);

                // Initialize RecipeModel with username
                RecipeModel recipe = new RecipeModel(id, userId, title, description, instructions, categoryId, imageUrl, name);
                recipeList.add(recipe);
            } while(cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return recipeList;
    }




    // Store the 2FA code
    public boolean storeTwoFACode(String email, String code) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("two_fa_code", code);

        long result = db.replace("TwoFactorAuth", null, contentValues);
        db.close();
        return result != -1;
    }

    // Retrieve the 2FA code
    public String getTwoFACode(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT two_fa_code FROM TwoFactorAuth WHERE email = ?", new String[]{email});
        if (cursor.moveToFirst()) {
            String code = cursor.getString(0);
            cursor.close();
            db.close();
            return code;
        }
        cursor.close();
        db.close();
        return null;
    }

    public boolean removeTwoFACode(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete("TwoFactorAuth", "email = ?", new String[]{email});
        db.close();
        return rowsAffected > 0;
    }

    public boolean storeResetCode(String email, String resetCode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("reset_code", resetCode);

        long result = db.replace("PasswordReset", null, contentValues);
        db.close();
        return result != -1;
    }

    public String getResetCode(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT reset_code FROM PasswordReset WHERE email = ?", new String[]{email});
        if (cursor.moveToFirst()) {
            String code = cursor.getString(0);
            cursor.close();
            db.close();
            return code;
        }
        cursor.close();
        db.close();
        return null;
    }
    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Generate a new salt and hash the new password
        String salt = generateSalt();
        String hashedPassword = hashPassword(newPassword, salt);

        ContentValues contentValues = new ContentValues();
        contentValues.put("password", hashedPassword);
        contentValues.put("salt", salt);

        int rowsAffected = db.update("User", contentValues, "email = ?", new String[]{email});
        db.close();
        return rowsAffected > 0;
    }

    public boolean isEmailRegistered(String email) {
        SQLiteDatabase database = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM User WHERE email = ?";
        Cursor cursor = database.rawQuery(query, new String[]{email});

        boolean isRegistered = false;
        if (cursor.moveToFirst()) {
            isRegistered = cursor.getInt(0) > 0; // Check if count > 0
        }
        cursor.close();
        database.close();
        return isRegistered;
    }



}