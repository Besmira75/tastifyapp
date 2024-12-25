package com.example.tastifyapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;



public class AddRecipe extends AppCompatActivity {

    private DB dbHelper;
    private Spinner spinnerCategory;
    private LinearLayout ingredientsContainer;
    private Button btnAddIngredient, btnSaveRecipe;

    private List<IngredientQuantity> ingredients = new ArrayList<>();
    private List<String> categoryList = new ArrayList<>();
    private List<Integer> categoryIdList = new ArrayList<>();

    // // // // // // for image // // // // // // //
    private ImageView recipeImageView;
    private Button btnSelectImage;
    private String selectedImageUri;

    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        dbHelper = new DB(this);

        recipeImageView = findViewById(R.id.iv_recipe_image); // Correct the ID to match the XML
// Replace with your ImageView's ID.

        recipeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });


        spinnerCategory = findViewById(R.id.spinner_category);
        ingredientsContainer = findViewById(R.id.ingredients_container);
        btnAddIngredient = findViewById(R.id.btn_add_ingredient);
        btnSaveRecipe = findViewById(R.id.btn_save_recipe);

        // Load categories from the database
        loadCategories();

        // Add Ingredient Button Clicked
        btnAddIngredient.setOnClickListener(v -> showAddIngredientDialog());

        // Save Recipe Button Clicked
        btnSaveRecipe.setOnClickListener(v -> saveRecipe());


    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, 100); // 100 is the request code.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            Uri imageUri = data.getData(); // Get the URI of the selected image
            selectedImageUri = imageUri.toString(); // Store the URI (for later use, like saving to DB or displaying the image)

            // Optionally, set the image to the ImageView
            recipeImageView.setImageURI(imageUri);

            // Save image path with the rest of the recipe data
            if (imageUri != null) {
                String imagePath = imageUri.toString(); // Convert URI to string
            }
        }
    }

    private void loadCategories() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM Category", null);

            categoryList.clear();
            categoryIdList.clear();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                    int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    categoryList.add(categoryName);
                    categoryIdList.add(categoryId);
                } while (cursor.moveToNext());
            }

            if (categoryList.isEmpty()) {
                // If no categories are found in the database, notify the user
                categoryList.add("No categories available");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load categories: " + e.getMessage(), Toast.LENGTH_LONG).show();
            categoryList.add("Error loading categories");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        // Update the spinner adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Log categories for debugging
        for (int i = 0; i < categoryList.size(); i++) {
            System.out.println("Category: " + categoryList.get(i) + ", ID: " + categoryIdList.get(i));
        }
    }


    private void showAddIngredientDialog() {
        final EditText etIngredientName = new EditText(this);
        final EditText etIngredientQuantity = new EditText(this);

        etIngredientName.setHint("Ingredient Name");
        etIngredientQuantity.setHint("Quantity");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Ingredient");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(etIngredientName);
        layout.addView(etIngredientQuantity);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String ingredientName = etIngredientName.getText().toString().trim();
            String quantity = etIngredientQuantity.getText().toString().trim();

            if (!ingredientName.isEmpty() && !quantity.isEmpty()) {
                ingredients.add(new IngredientQuantity(ingredientName, quantity));
                addIngredientToView(ingredientName, quantity);
            } else {
                Toast.makeText(this, "Please enter both ingredient and quantity.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }



    private void addIngredientToView(String ingredientName, String quantity) {
        LinearLayout ingredientLayout = new LinearLayout(this);
        ingredientLayout.setOrientation(LinearLayout.HORIZONTAL);
        ingredientLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        EditText etIngredientName = new EditText(this);
        etIngredientName.setText(ingredientName);
        etIngredientName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        ingredientLayout.addView(etIngredientName);

        EditText etIngredientQuantity = new EditText(this);
        etIngredientQuantity.setText(quantity); // Directly set the string
        etIngredientQuantity.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        ingredientLayout.addView(etIngredientQuantity);

        ingredientsContainer.addView(ingredientLayout);
    }


    private void saveRecipe() {
        String title = ((EditText) findViewById(R.id.et_recipe_title)).getText().toString().trim();
        String description = ((EditText) findViewById(R.id.et_recipe_description)).getText().toString().trim();
        String instructions = ((EditText) findViewById(R.id.et_recipe_instructions)).getText().toString().trim();

        if (categoryIdList.isEmpty() || spinnerCategory.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "Invalid category selection.", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryId = categoryIdList.get(spinnerCategory.getSelectedItemPosition());

        if (title.isEmpty() || description.isEmpty() || instructions.isEmpty()) {
            Toast.makeText(this, "Please fill in all recipe details.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (addRecipe(1, title, description, instructions, categoryId, ingredients)) {                              // ME BO DINAMIKISHT USER ID NE BAZE TE CILIT USER ESHTE LOG-IN !!!!!
            Toast.makeText(this, "Recipe saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save recipe.", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean addRecipe(int userId, String title, String description, String instructions, int categoryId, List<IngredientQuantity> ingredients) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            // Insert the recipe into the Recipe table
            ContentValues recipeValues = new ContentValues();
            recipeValues.put("user_id", userId);
            recipeValues.put("title", title);
            recipeValues.put("description", description);
            recipeValues.put("instructions", instructions);

            // Get the selected category ID from the spinner
            int selectedCategoryIndex = spinnerCategory.getSelectedItemPosition();
            if (selectedCategoryIndex < 0 || selectedCategoryIndex >= categoryIdList.size()) {
                throw new Exception("Invalid category selected");
            }
            categoryId = categoryIdList.get(selectedCategoryIndex);
            recipeValues.put("category_id", categoryId);

            long recipeId = db.insert("Recipe", null, recipeValues);
            if (recipeId == -1) throw new Exception("Failed to insert recipe");

            // Insert ingredients into the RecipeIngredient table
            // Insert all ingredients directly
            for (IngredientQuantity iq : ingredients) {
                ContentValues ingredientValues = new ContentValues();
                ingredientValues.put("emri", iq.getIngredientName());
                long ingredientId = db.insert("Ingredient", null, ingredientValues);
                if (ingredientId == -1) throw new Exception("Failed to insert ingredient");

                ContentValues recipeIngredientValues = new ContentValues();
                recipeIngredientValues.put("recipe_id", recipeId);
                recipeIngredientValues.put("ingredient_id", ingredientId);
                recipeIngredientValues.put("sasia", iq.getQuantity()); // Quantity as String
                long recipeIngredientResult = db.insert("RecipeIngredient", null, recipeIngredientValues);
                if (recipeIngredientResult == -1) throw new Exception("Failed to link ingredient to recipe");
            }

            // Insert the image URL into the Image table (if an image was selected)
            if (selectedImageUri != null && !selectedImageUri.isEmpty()) {
                ContentValues imageValues = new ContentValues();
                imageValues.put("recipe_id", recipeId);
                imageValues.put("image_url", selectedImageUri);

                long imageId = db.insert("Image", null, imageValues);
                if (imageId == -1) throw new Exception("Failed to insert image into Image table");
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }



    public static class IngredientQuantity {
        private final String ingredientName;
        private final String quantity;

        public IngredientQuantity(String ingredientName, String quantity) {
            this.ingredientName = ingredientName;
            this.quantity = quantity;
        }

        public String getIngredientName() {
            return ingredientName;
        }

        public String getQuantity() {
            return quantity;
        }
    }
}
