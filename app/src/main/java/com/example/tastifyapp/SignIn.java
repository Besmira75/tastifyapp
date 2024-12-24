package com.example.tastifyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import java.security.SecureRandom;

public class SignIn extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private DB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        signInButton = findViewById(R.id.button);
        db = new DB(this);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(SignIn.this, "Email and Password are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(SignIn.this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (db.validateUser(email, password)) {
                    sendTwoFACode(email);

                    Intent intent = new Intent(SignIn.this, TwoFactorActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                } else {
                    Toast.makeText(SignIn.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Link to Forgot Password Activity
        TextView forgotPassword = findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn.this, PasswordResetActivity.class);
                startActivity(intent);
            }
        });

        // Link to Sign Up Activity
        TextView signUp = findViewById(R.id.signUp);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
            }
        });
    }

    private String generateSixDigitCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void sendTwoFACode(String email) {
        String code = generateSixDigitCode();
        db.storeTwoFACode(email, code);

        // Email sending logic
        new Thread(() -> {
            try {
                // Replace with your email credentials
                final String fromEmail = "astritkrasniqi079@gmail.com";
                final String password = "htme gnwk usqq ewpd";

                // Configure properties
                java.util.Properties props = new java.util.Properties();
                props.put("mail.smtp.host", "smtp.gmail.com"); // Change if not using Gmail
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");

                // Create session
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
                message.setSubject("Your 2FA Code");
                message.setText("Your 2FA code is: " + code);

                // Send email
                javax.mail.Transport.send(message);
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(SignIn.this, "Failed to send email", Toast.LENGTH_SHORT).show());
            }
        }).start();

        Toast.makeText(this, "2FA code sent to your email", Toast.LENGTH_SHORT).show();
    }

}