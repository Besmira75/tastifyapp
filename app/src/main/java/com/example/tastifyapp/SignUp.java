// SignUp.java - Activity for User Registration
package com.example.tastifyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {

    private EditText nameField, emailField, passwordField, confirmPasswordField;
    private DB DB;
    private Button signUpButton;

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
        if (TextUtils.isEmpty(password) || !Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{6,20}$").matcher(password).matches()) {
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


}
