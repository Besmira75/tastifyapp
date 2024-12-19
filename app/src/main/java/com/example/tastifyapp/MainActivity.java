package com.example.tastifyapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Start the SignIn activity
        Intent intent = new Intent(MainActivity.this, SignIn.class);
        startActivity(intent);

        // Finish MainActivity to prevent going back to it
        finish();
    }

}
