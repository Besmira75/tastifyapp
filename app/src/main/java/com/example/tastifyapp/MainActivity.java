package com.example.tastifyapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private Button logoutButton;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        sessionManager = new SessionManager(this);

        // If user is not logged in, redirect to SignIn
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, SignIn.class);
            startActivity(intent);
            finish(); // Prevent returning to MainActivity
            return;
        }

        // 1. Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 2. DrawerLayout + Toggle
        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 3. NavigationView
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 4. Log Out Button inside the sidebar
        logoutButton = findViewById(R.id.button_logout);
        logoutButton.setOnClickListener(view -> {
            // Clear the user session
            sessionManager.clearSession();
            Toast.makeText(MainActivity.this, "Logged out!", Toast.LENGTH_SHORT).show();
            // Redirect to SignIn
            Intent intent = new Intent(MainActivity.this, SignIn.class);
            startActivity(intent);
            finish(); // Prevent returning to MainActivity
        });

        // 5. Default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new HomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new HomeFragment())
                    .commit();
        } else if (id == R.id.nav_my_recipes) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new MyRecipesFragment())
                    .commit();
        } else if (id == R.id.nav_profile) {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
        }

        // Close the drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
