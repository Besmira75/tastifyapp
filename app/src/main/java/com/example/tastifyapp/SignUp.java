// SignUp.java - Activity for User Registration
package com.example.tastifyapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tastifyapp.data.DB;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {

    private EditText nameField, emailField, passwordField, confirmPasswordField;
    private com.example.tastifyapp.data.DB DB;
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

        signUpButton.setOnClickListener(v -> {
            if (validateFields()) {
                String name = nameField.getText().toString().trim();
                String email = emailField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();

                // Hash and salt the password before storing it
                String saltedPassword = hashPassword(password);

                // Proceed with sign-up logic
                if (DB.checkUserEmail(email)) {
                    Toast.makeText(this, "User already exists.", Toast.LENGTH_SHORT).show();
                } else if (DB.insertUser(name, email, saltedPassword)) {
                    Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to register user.", Toast.LENGTH_SHORT).show();
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

    private String hashPassword(String password) {
        try {
            // Generate a random salt
            SecureRandom secureRandom = new SecureRandom();
            byte[] salt = new byte[16];
            secureRandom.nextBytes(salt);

            // Hash the password with SHA-256 and salt
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] hashedPassword = digest.digest(password.getBytes());

            // Convert hashed password to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedPassword) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
