package com.example.tastifyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyRecipesFragment extends Fragment {

    private RecyclerView recyclerRecipes;
    private RecipeAdapter recipeAdapter;
    private DB db;
    private SessionManager sessionManager;
    private Button btnAddRecipe;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_my_recipes, container, false);

        // Initialize DB and SessionManager
        db = new DB(requireContext());
        sessionManager = new SessionManager(requireContext());

        // Find views
        btnAddRecipe = rootView.findViewById(R.id.btnAddRecipe);
        recyclerRecipes = rootView.findViewById(R.id.recycler_recipes_my);

        // Set layout manager
        recyclerRecipes.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter with empty list
        recipeAdapter = new RecipeAdapter(new ArrayList<>());
        recyclerRecipes.setAdapter(recipeAdapter);

        // Load user's recipes
        loadUserRecipes();

        // Add Recipe Button Click Listener
        btnAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddRecipe.class);
            startActivity(intent);
        });

        return rootView;
    }

    private void loadUserRecipes(){
        if(sessionManager.isLoggedIn()){
            int userId = sessionManager.getUserId();
            List<RecipeModel> recipes = db.getRecipesByUserId(userId);
            if (recipes != null && !recipes.isEmpty()) {
                recipeAdapter.updateData(recipes);
            } else {
                Toast.makeText(getContext(), "No recipes found.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(getContext(), "Please sign in to view your recipes.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), SignIn.class);
            startActivity(intent);
            requireActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the recipe list when the fragment is resumed
        loadUserRecipes();
    }
}
