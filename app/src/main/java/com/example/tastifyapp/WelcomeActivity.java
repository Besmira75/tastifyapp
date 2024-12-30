package com.example.tastifyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            // User is already logged in, redirect them to MainActivity
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Make sure this activity is removed from the activity stack
            return; // Ensure the rest of the onCreate does not execute
        }

        // Initialize the buttons and text views
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvSignIn = findViewById(R.id.tvSignIn);

        // Set the click listener for the register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the SignUpActivity
                Intent intent = new Intent(WelcomeActivity.this, SignUp.class);
                startActivity(intent);
            }
        });

        // Set the click listener for the sign-in text view
        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the SignInActivity
                Intent intent = new Intent(WelcomeActivity.this, SignIn.class);
                startActivity(intent);
            }
        });
    }
}
