package com.example.tastifyapp.pages;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tastifyapp.R;
import com.example.tastifyapp.SignUp;

public class WelcomePage extends AppCompatActivity {
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void register(View view) {
        startActivity(new Intent(WelcomePage.this, SignUp.class));
    }


//    public void signin(View view) {
//        startActivity(new Intent(WelcomePage.this , SignIn.class));
//    }
}
