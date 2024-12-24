package com.example.tastifyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PasswordResetActivity extends AppCompatActivity {
    private EditText emailEditText;
    private Button sendEmailButton;
    private DB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        emailEditText = findViewById(R.id.emailEditText);
        sendEmailButton = findViewById(R.id.sendEmailButton);
        db = new DB(this);

        sendEmailButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter your email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.isEmailRegistered(email)) {
                sendResetCode(email); // Method for sending reset code
            } else {
                Toast.makeText(this, "Email is not registered", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendResetCode(String email) {
        String resetCode = generateResetCode();
        db.storeResetCode(email, resetCode);

        // Email sending logic
        new Thread(() -> {
            try {
                final String fromEmail = "astritkrasniqi079@gmail.com"; // Your email
                final String password = "htme gnwk usqq ewpd"; // Your app password

                // Email properties
                java.util.Properties props = new java.util.Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");

                javax.mail.Session session = javax.mail.Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            @Override
                            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                                return new javax.mail.PasswordAuthentication(fromEmail, password);
                            }
                        });

                // Create email message
                javax.mail.Message message = new javax.mail.internet.MimeMessage(session);
                message.setFrom(new javax.mail.internet.InternetAddress(fromEmail));
                message.setRecipients(javax.mail.Message.RecipientType.TO,
                        javax.mail.internet.InternetAddress.parse(email));
                message.setSubject("Password Reset Code");
                message.setText("Your password reset code is: " + resetCode);

                // Send email
                javax.mail.Transport.send(message);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Reset code sent to your email", Toast.LENGTH_SHORT).show();
                    // Navigate to ResetPasswordActivity
                    Intent intent = new Intent(PasswordResetActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Failed to send email", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String generateResetCode() {
        // Generate a 6-digit random code
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}
