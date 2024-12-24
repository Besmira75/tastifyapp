package com.example.tastifyapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class SignUp extends AppCompatActivity {

    private EditText nameField, emailField, passwordField, confirmPasswordField;
    private DB DB;
    private Button signUpButton;

    // Notification Channel ID
    private static final String CHANNEL_ID = "signup_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize fields
        nameField = findViewById(R.id.editTextName);
        emailField = findViewById(R.id.editTextEmail);
        passwordField = findViewById(R.id.editTextPassword);
        confirmPasswordField = findViewById(R.id.editTextConfirmPassword);
        signUpButton = findViewById(R.id.buttonRegister);

        DB = new DB(this);

        // Create notification channel (required for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SignUp Channel";
            String description = "Channel for registration notifications";
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            android.app.NotificationChannel channel = new android.app.NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            android.app.NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Request notification permission (for Android 13 and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // Handle sign-up button click
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameField.getText().toString().trim();
                String email = emailField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();

                // Validate inputs
                if (!validateFields()) return;

                // Insert the user into the database
                boolean success = DB.insertUser(name, email, password);
                if (success) {
                    Toast.makeText(SignUp.this, "Sign-Up Successful", Toast.LENGTH_SHORT).show();

                    // Trigger the welcome notification
                    showWelcomeNotification(name);

                    // Navigate to the login page
                    Intent intent = new Intent(SignUp.this, SignIn.class);
                    startActivity(intent);
                    finish(); // Close the current activity
                } else {
                    Toast.makeText(SignUp.this, "Error in Sign-Up", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateFields() {
        // Retrieve values
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        // Validate Name (only alphabets, not empty)
        if (TextUtils.isEmpty(name) || !name.matches("[a-zA-Z]+")) {
            nameField.setError("Name must contain only letters and not be empty.");
            return false;
        }

        // Validate Email (non-empty and valid format)
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Enter a valid email address.");
            return false;
        }

        // Validate Password (at least 1 lowercase, 1 uppercase, 1 digit, 1 special character, 6-20 characters)
        if (TextUtils.isEmpty(password) || !password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{6,20}$")) {
            passwordField.setError("Password must be 6-20 characters long and contain 1 lowercase, 1 uppercase, 1 digit, and 1 special character.");
            return false;
        }

        // Ensure passwords match
        if (!password.equals(confirmPassword)) {
            confirmPasswordField.setError("Passwords do not match.");
            return false;
        }

        return true;
    }

    // Method to show the welcome notification
    private void showWelcomeNotification(String userName) {
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Welcome to Tastify, " + userName + "!")
                .setContentText("We're glad to have you in our group.")
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // Use a default icon if you don't have one
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, notificationBuilder.build()); // 1 is the notification ID
    }
}
