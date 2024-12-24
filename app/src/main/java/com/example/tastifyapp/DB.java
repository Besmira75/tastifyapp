package com.example.tastifyapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import androidx.annotation.Nullable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class DB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "recipes.db";
    private static final int DATABASE_VERSION = 5; // Incremented version for 2FA table

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
                "password TEXT NOT NULL, " +
                "salt TEXT NOT NULL);");

        // 2FA Table
        db.execSQL("CREATE TABLE IF NOT EXISTS TwoFactorAuth (" +
                "email TEXT PRIMARY KEY, " +
                "two_fa_code TEXT NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            db.execSQL("CREATE TABLE IF NOT EXISTS TwoFactorAuth (" +
                    "email TEXT PRIMARY KEY, " +
                    "two_fa_code TEXT NOT NULL);");
        }
    }

    // Generate a random salt
    private String generateSalt() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] saltBytes = new byte[16];
        secureRandom.nextBytes(saltBytes);
        return Base64.encodeToString(saltBytes, Base64.NO_WRAP);
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
}