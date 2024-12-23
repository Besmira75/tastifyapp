package com.example.tastifyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;


public class SignIn extends AppCompatActivity {
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private DB db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.sign_in);

        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        signInButton = findViewById(R.id.button);
        db = new DB(this);


        // Handle login button click
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInputLayout.getEditText().getText().toString().trim();
                String password = passwordInputLayout.getEditText().getText().toString().trim();

                // Validate inputs
                if (!validateFields()) return;

                // Validate credentials
                boolean isValid = db.validateUser(email, password);
                if (isValid) {
                    Toast.makeText(SignIn.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    // Navigate to the main activity
                    Intent intent = new Intent(SignIn.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Close the current activity
                } else {
                    Toast.makeText(SignIn.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateFields() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        boolean isValid = true;

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Enter a valid email");
            isValid = false;
        } else {
            emailInputLayout.setError(null);
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError("Password is required");
            isValid = false;
        } else {
            passwordInputLayout.setError(null);
        }

        // Proceed if inputs are valid
        if (isValid) {
            boolean userExists = db.validateUser(email, password);
            if (userExists) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

                // Navigate to main activity
                Intent intent = new Intent(SignIn.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close the SignIn activity
            } else {
                Toast.makeText(this, "Invalid email or password!", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }
}
