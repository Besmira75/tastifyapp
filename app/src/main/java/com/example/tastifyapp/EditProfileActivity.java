package com.example.tastifyapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import android.util.Base64;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etOldPassword, etNewPassword;
    private Button btnConfirmName, btnConfirmPassword;
    private SQLiteDatabase db;
    private SessionManager sessionManager; // Declare SessionManager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize UI components
        etName = findViewById(R.id.etName);
        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnConfirmName = findViewById(R.id.btnConfirmName);
        btnConfirmPassword = findViewById(R.id.btnConfirmPassword);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Get database instance
        db = openOrCreateDatabase("recipes.db", MODE_PRIVATE, null);

        // Load current user data
        loadUserProfile();

        // Handle back button click
        ImageButton btnBack = findViewById(R.id.btnBack);  // Use findViewById to reference the button
        btnBack.setOnClickListener(v -> onBackPressed());
        // Handle Confirm Name button click
        btnConfirmName.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            if (validateName(newName)) {
                showConfirmationDialog("Name", newName);
            }
        });

        // Handle Confirm Password button click
        btnConfirmPassword.setOnClickListener(v -> {
            String oldPassword = etOldPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            if (validatePassword(newPassword)) {
                if (validateOldPassword(oldPassword)) {
                    showConfirmationDialog("Password", newPassword);
                } else {
                    Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadUserProfile() {
        // Retrieve the logged-in user's ID from SessionManager
        int userId = sessionManager.getUserId();

        if (userId == -1) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT id, name, salt FROM User WHERE id = ?", new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.moveToFirst()) {
                int nameColumnIndex = cursor.getColumnIndex("name");
                int saltColumnIndex = cursor.getColumnIndex("salt");

                if (nameColumnIndex != -1 && saltColumnIndex != -1) {
                    String currentName = cursor.getString(nameColumnIndex);
                    String currentSalt = cursor.getString(saltColumnIndex);

                    etName.setText(currentName);
                    Log.d("User Profile", "Current salt: " + currentSalt);
                } else {
                    Toast.makeText(this, "Error: 'name' or 'salt' column not found in the database.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No user found or failed to load profile.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("EditProfileActivity", "Error loading user profile", e);
            Toast.makeText(this, "Failed to load user profile", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }



    private boolean validateName(String name) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateOldPassword(String oldPassword) {
        // Retrieve logged-in user's ID from SessionManager
        int userId = sessionManager.getUserId(); // Get user ID from SessionManager

        if (userId == -1) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Retrieve the stored salt and hashed password for the user
        Cursor cursor = db.rawQuery("SELECT password, salt FROM User WHERE id = ?", new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            // Get column indices
            int passwordColumnIndex = cursor.getColumnIndex("password");
            int saltColumnIndex = cursor.getColumnIndex("salt");

            // Log column indices to check if they are valid
            Log.d("EditProfileActivity", "password column index: " + passwordColumnIndex);
            Log.d("EditProfileActivity", "salt column index: " + saltColumnIndex);

            // Check if the column indices are valid
            if (passwordColumnIndex != -1 && saltColumnIndex != -1) {
                String storedHash = cursor.getString(passwordColumnIndex);
                String storedSalt = cursor.getString(saltColumnIndex);

                // Hash the entered old password with the stored salt
                String hashedOldPassword = hashPassword(oldPassword, storedSalt);

                // Compare the entered old password with the stored hashed password
                if (hashedOldPassword.equals(storedHash)) {
                    cursor.close();
                    return true;
                } else {
                    cursor.close();
                    return false;
                }
            } else {
                // If column indices are invalid, log an error and return false
                Log.e("EditProfileActivity", "Column 'password' or 'salt' not found in the database.");
                cursor.close();
                return false;
            }
        }

        // Close cursor after use
        if (cursor != null) {
            cursor.close();
        }

        return false;
    }

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

    private void showConfirmationDialog(String field, String value) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Changes")
                .setMessage("Are you sure you want to update your " + field.toLowerCase() + " to:\n\n" + value)
                .setPositiveButton("Confirm", (dialog, which) -> updateUserProfile(field, value))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateUserProfile(String field, String value) {
        // Retrieve logged-in user's ID from SessionManager
        int userId = sessionManager.getUserId(); // Get user ID from SessionManager

        if (userId == -1) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        if (field.equals("Name")) {
            values.put("name", value);
        } else if (field.equals("Password")) {
            String newSalt = generateSalt();
            String hashedNewPassword = hashPassword(value, newSalt);
            values.put("password", hashedNewPassword);
            values.put("salt", newSalt);
        }

        int rowsAffected = db.update("User", values, "id = ?", new String[]{String.valueOf(userId)});
        if (rowsAffected > 0) {
            Toast.makeText(this, field + " updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to update " + field.toLowerCase(), Toast.LENGTH_SHORT).show();
        }
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
