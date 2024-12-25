package com.example.tastifyapp;

import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton menuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        menuButton = findViewById(R.id.btnMenu);

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(findViewById(R.id.sidebar_layout))) {
                    drawerLayout.closeDrawer(findViewById(R.id.sidebar_layout));
                } else {
                    drawerLayout.openDrawer(findViewById(R.id.sidebar_layout));
                }
            }
        });
        if (savedInstanceState == null) {
            displayFragment(new HomeFragment());
        }

        setupDrawerContent();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(findViewById(R.id.sidebar_layout))) {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar_layout));
        } else {
            super.onBackPressed();
        }
    }
    private void setupDrawerContent() {
        findViewById(R.id.linkHome).setOnClickListener(view -> displayFragment(new HomeFragment()));
        findViewById(R.id.linkMyRecipes).setOnClickListener(view -> displayFragment(new MyRecipesFragment()));
        findViewById(R.id.linkProfile).setOnClickListener(view -> displayFragment(new ProfileFragment()));
    }

    private void displayFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

}
