package com.example.tastifyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TwoFactorActivity extends AppCompatActivity {
    private EditText codeEditText;
    private Button verifyButton;
    private DB db;
    private SessionManager sessionManager;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_factor);

        // Initialize DB and SessionManager
        db = new DB(this);
        sessionManager = new SessionManager(this);

        // Initialize views
        codeEditText = findViewById(R.id.codeEditText);
        verifyButton = findViewById(R.id.verifyButton);

        // Get email from intent
        Intent intent = getIntent();
        email = intent.getStringExtra("email");

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Invalid session. Please sign in again.", Toast.LENGTH_SHORT).show();
            navigateToSignIn();
            return;
        }

        verifyButton.setOnClickListener(v -> verifyTwoFACode());
    }

    private void verifyTwoFACode() {
        String enteredCode = codeEditText.getText().toString().trim();

        if (TextUtils.isEmpty(enteredCode)) {
            Toast.makeText(this, "Please enter the 2FA code", Toast.LENGTH_SHORT).show();
            return;
        }

        String storedCode = db.getTwoFACode(email);

        if (storedCode == null) {
            Toast.makeText(this, "No 2FA code found. Please sign in again.", Toast.LENGTH_SHORT).show();
            navigateToSignIn();
            return;
        }

        if (enteredCode.equals(storedCode)) {
            // 2FA verification successful

            // Get user ID from email
            int userId = db.getUserId(email);
            if (userId != -1) {
                // Save user session
                sessionManager.saveUserSession(userId);

                // Remove the used 2FA code for security
                boolean removed = db.removeTwoFACode(email);
                if (!removed) {
                    Toast.makeText(this, "Failed to remove 2FA code. Please contact support.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Navigate to MainActivity
                Intent mainIntent = new Intent(TwoFactorActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish(); // Prevent returning to TwoFactorActivity
            } else {
                Toast.makeText(this, "User not found. Please sign in again.", Toast.LENGTH_SHORT).show();
                navigateToSignIn();
            }
        } else {
            Toast.makeText(this, "Invalid 2FA code. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToSignIn() {
        Intent intent = new Intent(TwoFactorActivity.this, SignIn.class);
        startActivity(intent);
        finish(); // Prevent returning to TwoFactorActivity
    }
}
