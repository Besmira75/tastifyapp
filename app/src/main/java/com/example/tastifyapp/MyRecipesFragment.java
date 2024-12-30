// com/example/tastifyapp/MyRecipesFragment.java
package com.example.tastifyapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyRecipesFragment extends Fragment implements RecipeAdapter.OnRecipeListener {

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

        // Load user's recipes
        loadUserRecipes();

        // Add Recipe Button Click Listener
        btnAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddRecipe.class);
            intent.putExtra(AddRecipe.MODE, AddRecipe.MODE_ADD);
            startActivity(intent);
        });

        return rootView;
    }

    /**
     * Loads the recipes belonging to the logged-in user.
     * If the user is not logged in, redirects to the SignIn activity.
     */
    private void loadUserRecipes(){
        if(sessionManager.isLoggedIn()){
            int userId = sessionManager.getUserId();
            List<RecipeModel> recipes = db.getRecipesByUserId(userId);
            if (recipes != null && !recipes.isEmpty()) {
                // Initialize the adapter with Edit/Delete buttons
                recipeAdapter = new RecipeAdapter(recipes, this, true);
                recyclerRecipes.setAdapter(recipeAdapter);
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

    /**
     * Handles the item click event to open RecipeDetailsActivity.
     *
     * @param position The position of the clicked item in the RecyclerView.
     */
    @Override
    public void onItemClick(int position) {
        RecipeModel clickedRecipe = recipeAdapter.getRecipeList().get(position);
        Intent intent = new Intent(getActivity(), RecipeDetailsActivity.class);
        intent.putExtra(AddRecipe.RECIPE_ID, clickedRecipe.getId());
        startActivity(intent);
    }

    /**
     * Handles the Edit button click event.
     *
     * @param position The position of the clicked item in the RecyclerView.
     */
    @Override
    public void onEditClick(int position) {
        RecipeModel recipe = recipeAdapter.getRecipeList().get(position);
        Intent intent = new Intent(getActivity(), AddRecipe.class);
        intent.putExtra(AddRecipe.MODE, AddRecipe.MODE_EDIT);
        intent.putExtra(AddRecipe.RECIPE_ID, recipe.getId());
        startActivity(intent);
    }

    /**
     * Handles the Delete button click event.
     *
     * @param position The position of the clicked item in the RecyclerView.
     */
    @Override
    public void onDeleteClick(int position) {
        RecipeModel recipe = recipeAdapter.getRecipeList().get(position);

        // Build the confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Recipe");
        builder.setMessage("Are you sure you want to delete this recipe?");
        builder.setCancelable(true);

        // Set up the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                boolean deleted = db.deleteRecipe(recipe.getId());
                if(deleted){
                    Toast.makeText(getContext(), "Recipe deleted successfully.", Toast.LENGTH_SHORT).show();
                    // Remove from adapter and notify changes
                    recipeAdapter.getRecipeList().remove(position);
                    recipeAdapter.notifyItemRemoved(position);
                } else {
                    Toast.makeText(getContext(), "Failed to delete recipe.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss(); // Dismiss the dialog
            }
        });

        // Create and show the dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
