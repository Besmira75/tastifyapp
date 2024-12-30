// com/example/tastifyapp/HomeFragment.java
package com.example.tastifyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeFragment extends Fragment implements RecipeAdapter.OnRecipeListener {

    private RecyclerView recyclerCategories;
    private RecyclerView recyclerRecipes;
    private CategoryAdapter categoryAdapter;
    private RecipeAdapter recipeAdapter;
    private DB db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize DB
        db = new DB(requireContext());

        // Find views
        Button btnAddRecipe = rootView.findViewById(R.id.btnAddRecipe);
        recyclerCategories = rootView.findViewById(R.id.recycler_categories);
        recyclerRecipes = rootView.findViewById(R.id.recycler_recipes);

        // Set layout managers
        recyclerCategories.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerRecipes.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load all categories (full: id + name) from DB
        List<CategoryModel> categoryList = db.getAllCategoriesFull();
        // Create and set CategoryAdapter
        categoryAdapter = new CategoryAdapter(categoryList);
        recyclerCategories.setAdapter(categoryAdapter);

        // Load all recipes initially
        List<RecipeModel> recipeList = db.getAllRecipes();
        // Create and set RecipeAdapter with OnRecipeListener and without Edit/Delete buttons
        recipeAdapter = new RecipeAdapter(recipeList, this, false);
        recyclerRecipes.setAdapter(recipeAdapter);

        // Set click listener for adding a new recipe
        btnAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddRecipe.class);
            intent.putExtra(AddRecipe.MODE, AddRecipe.MODE_ADD);
            startActivity(intent);
        });

        // Set category click listener to filter recipes
        categoryAdapter.setOnCategoryClickListener(categoryId -> {
            // Retrieve filtered recipes from DB
            List<RecipeModel> filteredRecipes = db.getRecipesByCategoryId(categoryId);
            // Update the recipe list displayed
            updateRecipeList(filteredRecipes);
        });

        return rootView;
    }

    // Helper method to update the RecipeAdapter with a new list of recipes
    private void updateRecipeList(List<RecipeModel> newList) {
        recipeAdapter.updateData(newList);
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
        // No action needed as Edit/Delete buttons are hidden in HomeFragment
    }

    /**
     * Handles the Delete button click event.
     *
     * @param position The position of the clicked item in the RecyclerView.
     */
    @Override
    public void onDeleteClick(int position) {
        // No action needed as Edit/Delete buttons are hidden in HomeFragment
    }
}
