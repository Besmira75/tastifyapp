// com/example/tastifyapp/RecipeDetailsActivity.java
package com.example.tastifyapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

public class RecipeDetailsActivity extends AppCompatActivity {

    private ImageView ivRecipeImageDetails;
    private TextView tvRecipeTitleDetails, tvRecipeCategoryDetails, tvRecipeDescriptionDetails, tvRecipeInstructionsDetails, tvRecipeCreatorDetails;
    private LinearLayout ingredientsContainerDetails;
    private Button btnEditRecipeDetails;

    private DB dbHelper;
    private SessionManager sessionManager;
    private int recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        // Initialize DB helper and SessionManager
        dbHelper = new DB(this);
        sessionManager = new SessionManager(this);

        // Initialize views
        ivRecipeImageDetails = findViewById(R.id.iv_recipe_image_details);
        tvRecipeTitleDetails = findViewById(R.id.tv_recipe_title_details);
        tvRecipeCategoryDetails = findViewById(R.id.tv_recipe_category_details);
        tvRecipeDescriptionDetails = findViewById(R.id.tv_recipe_description_details);
        tvRecipeInstructionsDetails = findViewById(R.id.tv_recipe_instructions_details);
        tvRecipeCreatorDetails = findViewById(R.id.tv_recipe_creator_details); // Updated TextView for name
        ingredientsContainerDetails = findViewById(R.id.ingredients_container_details);
        btnEditRecipeDetails = findViewById(R.id.btn_edit_recipe_details);

        // Retrieve recipe ID from Intent
        Intent intent = getIntent();
        if(intent != null && intent.hasExtra(AddRecipe.RECIPE_ID)){
            recipeId = intent.getIntExtra(AddRecipe.RECIPE_ID, -1);
            if(recipeId != -1){
                loadRecipeDetails(recipeId);
            } else {
                Toast.makeText(this, "Invalid Recipe ID.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "No Recipe ID provided.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Handle Edit Button Click
        btnEditRecipeDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editIntent = new Intent(RecipeDetailsActivity.this, AddRecipe.class);
                editIntent.putExtra(AddRecipe.MODE, AddRecipe.MODE_EDIT);
                editIntent.putExtra(AddRecipe.RECIPE_ID, recipeId);
                startActivity(editIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the recipe details in case of edits
        loadRecipeDetails(recipeId);
    }

    /**
     * Loads the recipe details from the database and populates the UI.
     *
     * @param recipeId The ID of the recipe to load.
     */
    private void loadRecipeDetails(int recipeId){
        RecipeModel recipe = dbHelper.getRecipeById(recipeId);
        if(recipe != null){
            // Set title, category, description, instructions
            tvRecipeTitleDetails.setText(recipe.getTitle());

            // Fetch category name
            String categoryName = getCategoryNameById(recipe.getCategoryId());
            if(categoryName != null){
                tvRecipeCategoryDetails.setText("Category: " + categoryName);
            } else {
                tvRecipeCategoryDetails.setText("Category: Unknown");
            }

            // Set creator's name
            tvRecipeCreatorDetails.setText("by " + recipe.getName());

            tvRecipeDescriptionDetails.setText(recipe.getDescription());
            tvRecipeInstructionsDetails.setText(recipe.getInstructions());

            // Load image with Glide
            if(recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()){
                String imageUrl = recipe.getImageUrl();
                Log.d("RecipeDetails", "Loading image from URL: " + imageUrl);
                Glide.with(this)
                        .load(Uri.parse(imageUrl))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_error)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model,
                                                        Target<Drawable> target, boolean isFirstResource) {
                                Log.e("RecipeDetails", "Glide failed to load image: " + e.getMessage());
                                // Optionally, show a Toast or Snackbar to notify the user
                                Toast.makeText(RecipeDetailsActivity.this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                                return false; // Allow Glide to handle the error drawable
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                           com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                Log.d("RecipeDetails", "Glide successfully loaded image.");
                                return false;
                            }
                        })
                        .into(ivRecipeImageDetails);
            } else {
                Log.d("RecipeDetails", "No image URL found for Recipe ID: " + recipeId + ". Loading placeholder.");
                Glide.with(this)
                        .load(R.drawable.ic_placeholder)
                        .into(ivRecipeImageDetails);
            }

            // Load ingredients
            List<AddRecipe.IngredientQuantity> ingredients = dbHelper.getIngredientsByRecipeId(recipeId);
            if(ingredients != null && !ingredients.isEmpty()){
                ingredientsContainerDetails.removeAllViews(); // Clear previous entries
                for(AddRecipe.IngredientQuantity iq : ingredients){
                    addIngredientToView(iq.getIngredientName(), iq.getQuantity());
                }
            } else {
                // Display a message if no ingredients are listed
                TextView tvNoIngredients = new TextView(this);
                tvNoIngredients.setText("No ingredients listed.");
                tvNoIngredients.setTextSize(16f);
                tvNoIngredients.setTextColor(ContextCompat.getColor(this, R.color.colorSecondaryText));
                ingredientsContainerDetails.removeAllViews();
                ingredientsContainerDetails.addView(tvNoIngredients);
            }

            // Show or hide the Edit button based on user ownership
            if(isCurrentUserOwner(recipe.getUserId())){
                btnEditRecipeDetails.setVisibility(View.VISIBLE);
            } else {
                btnEditRecipeDetails.setVisibility(View.GONE);
            }

        } else {
            Log.e("RecipeDetails", "Recipe not found for ID: " + recipeId);
            Toast.makeText(this, "Recipe not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Retrieves the category name based on category ID.
     *
     * @param categoryId The ID of the category.
     * @return The name of the category or null if not found.
     */
    private String getCategoryNameById(int categoryId){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("Category", new String[]{"category"}, "id = ?", new String[]{String.valueOf(categoryId)}, null, null, null);
        String categoryName = null;
        if(cursor != null && cursor.moveToFirst()){
            categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            cursor.close();
        }
        db.close();
        return categoryName;
    }

    /**
     * Dynamically adds an ingredient to the ingredients container.
     *
     * @param name     The name of the ingredient.
     * @param quantity The quantity of the ingredient.
     */
    private void addIngredientToView(String name, String quantity){
        // Create a horizontal LinearLayout to hold name and quantity
        LinearLayout ingredientLayout = new LinearLayout(this);
        ingredientLayout.setOrientation(LinearLayout.HORIZONTAL);
        ingredientLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        ingredientLayout.setPadding(0, 8, 0, 8);

        // Ingredient Name
        TextView tvName = new TextView(this);
        tvName.setText("- " + name);
        tvName.setTextSize(16f);
        tvName.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryText));
        tvName.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        ));
        ingredientLayout.addView(tvName);

        // Quantity
        TextView tvQuantity = new TextView(this);
        tvQuantity.setText(quantity);
        tvQuantity.setTextSize(16f);
        tvQuantity.setTextColor(ContextCompat.getColor(this, R.color.colorSecondaryText));
        tvQuantity.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        ingredientLayout.addView(tvQuantity);

        // Add to container
        ingredientsContainerDetails.addView(ingredientLayout);
    }

    /**
     * Checks if the current logged-in user is the owner of the recipe.
     *
     * @param recipeUserId The user ID associated with the recipe.
     * @return True if the current user is the owner, false otherwise.
     */
    private boolean isCurrentUserOwner(int recipeUserId){
        if(sessionManager.isLoggedIn()){
            int currentUserId = sessionManager.getUserId();
            return currentUserId == recipeUserId;
        }
        return false;
    }
}
