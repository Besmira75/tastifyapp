package com.example.tastifyapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail;
    private Button btnEditProfile;
    private SQLiteDatabase db;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_profile_activity, container, false);

        // Initialize UI components
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        // Initialize SessionManager
        sessionManager = new SessionManager(getActivity());

        // Get database instance
        db = getActivity().openOrCreateDatabase("recipes.db", Context.MODE_PRIVATE, null);
        // Set up the Toolbar and enable the back button


        // Load user data
        loadUserProfile();

        // Handle Edit Profile button click
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        return view;

    }

    private void loadUserProfile() {
        // Retrieve the logged-in user's ID from SessionManager
        int userId = sessionManager.getUserId();

        if (userId == -1) {
            // Handle case where no user is logged in
            tvName.setText("User not logged in");
            tvEmail.setText("N/A");
            return;
        }

        // Query the database to get the name and email of the user
        Cursor cursor = db.rawQuery("SELECT name, email FROM User WHERE id = ?", new String[]{String.valueOf(userId)});

        // Check if the cursor is not null and contains data
        if (cursor != null && cursor.moveToFirst()) {
            // Check if the required columns exist
            int nameColumnIndex = cursor.getColumnIndex("name");
            int emailColumnIndex = cursor.getColumnIndex("email");

            if (nameColumnIndex != -1 && emailColumnIndex != -1) {
                // Retrieve name and email
                String name = cursor.getString(nameColumnIndex);
                String email = cursor.getString(emailColumnIndex);

                // Update UI with user data
                tvName.setText(name);
                tvEmail.setText(email);
            } else {
                // Handle case where the columns do not exist
                tvName.setText("Name not found");
                tvEmail.setText("Email not found");
            }

            // Close the cursor
            cursor.close();
        } else {
            // Handle case where no data was found
            tvName.setText("Name not found");
            tvEmail.setText("Email not found");
        }
    }

}
