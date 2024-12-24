package com.example.tastifyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText resetCodeEditText, newPasswordEditText, confirmNewPasswordEditText;
    private Button resetPasswordButton;
    private DB db;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        resetCodeEditText = findViewById(R.id.resetCodeEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPasswordEditText); // New field
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        db = new DB(this);

        userEmail = getIntent().getStringExtra("email");

        resetPasswordButton.setOnClickListener(v -> {
            String resetCode = resetCodeEditText.getText().toString().trim();
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

            // Check if fields are empty
            if (TextUtils.isEmpty(resetCode) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
                Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if new password matches confirmation password
            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate reset code
            String storedCode = db.getResetCode(userEmail);
            if (resetCode.equals(storedCode)) {
                db.updatePassword(userEmail, newPassword); // Update the password in the database
                Toast.makeText(this, "Password reset successfully", Toast.LENGTH_SHORT).show();

                // Navigate to SignIn activity
                Intent intent = new Intent(ResetPasswordActivity.this, SignIn.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid reset code", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
