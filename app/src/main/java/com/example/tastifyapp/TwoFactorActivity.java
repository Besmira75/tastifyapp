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
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_factor);

        codeEditText = findViewById(R.id.codeEditText);
        verifyButton = findViewById(R.id.verifyButton);
        db = new DB(this);

        userEmail = getIntent().getStringExtra("email");

        verifyButton.setOnClickListener(v -> {
            String enteredCode = codeEditText.getText().toString().trim();

            if (TextUtils.isEmpty(enteredCode)) {
                Toast.makeText(this, "Enter the 2FA code", Toast.LENGTH_SHORT).show();
                return;
            }

            String storedCode = db.getTwoFACode(userEmail);
            if (enteredCode.equals(storedCode)) {
                Intent intent = new Intent(TwoFactorActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid 2FA code", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
