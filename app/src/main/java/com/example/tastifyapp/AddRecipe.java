package com.example.tastifyapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

public class AddRecipe extends AppCompatActivity {

    public static final String MODE = "MODE";
    public static final String MODE_ADD = "ADD";
    public static final String MODE_EDIT = "EDIT";
    public static final String RECIPE_ID = "RECIPE_ID";

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private static final int PERMISSION_REQUEST_CODE = 101;

    private DB dbHelper;
    private SessionManager sessionManager;
    private Spinner spinnerCategory;
    private LinearLayout ingredientsContainer;
    private Button btnAddIngredient, btnSaveRecipe, btnSelectImage;

    private List<IngredientQuantity> ingredients = new ArrayList<>();
    private List<String> categoryList = new ArrayList<>();
    private List<Integer> categoryIdList = new ArrayList<>();

    // For image
    private ImageView recipeImageView;
    private String selectedImageUri;

    private int editRecipeId = -1; // To store recipe ID when editing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        dbHelper = new DB(this);
        sessionManager = new SessionManager(this);

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please sign in to add a recipe.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize views
        recipeImageView = findViewById(R.id.iv_recipe_image);
        spinnerCategory = findViewById(R.id.spinner_category);
        ingredientsContainer = findViewById(R.id.ingredients_container);
        btnAddIngredient = findViewById(R.id.btn_add_ingredient);
        btnSaveRecipe = findViewById(R.id.btn_save_recipe);
        btnSelectImage = findViewById(R.id.btn_select_image);

        // Initialize the image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            // Log the received flags
                            int receivedFlags = result.getData().getFlags();
                            Log.d("AddRecipe", "Received Flags: " + receivedFlags);

                            // Extract only READ and WRITE URI permissions
                            final int takeFlags = receivedFlags & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            Log.d("AddRecipe", "Extracted takeFlags: " + takeFlags);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                if (takeFlags != 0) { // Ensure at least one flag is present
                                    try {
                                        getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                                        Log.d("AddRecipe", "Persistable URI permission granted.");
                                    } catch (SecurityException e) {
                                        Log.e("AddRecipe", "Failed to take persistable URI permission: " + e.getMessage());
                                        Toast.makeText(this, "Failed to access the selected image.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                } else {
                                    // Handle the case where no required flags are present
                                    Log.e("AddRecipe", "No URI permissions granted.");
                                    Toast.makeText(this, "No permissions granted for the selected image.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }

                            // Store the image URI as a string without double encoding
                            selectedImageUri = imageUri.toString();
                            Log.d("AddRecipe", "Selected Image URI: " + selectedImageUri);

                            // Use Glide to load the image
                            Glide.with(this)
                                    .load(imageUri)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .placeholder(R.drawable.ic_placeholder)
                                    .error(R.drawable.ic_error)
                                    .into(recipeImageView);

                            Log.d("AddRecipe", "Set ImageView with Glide for URI: " + selectedImageUri);
                        } else {
                            Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Set up image picker via ImageView click
        recipeImageView.setOnClickListener(v -> openImagePicker());

        // Alternatively, set up image picker via button click
        btnSelectImage.setOnClickListener(v -> openImagePicker());

        // Load categories from the database
        loadCategories();

        // Add Ingredient Button Clicked
        btnAddIngredient.setOnClickListener(v -> showAddIngredientDialog());

        // Save Recipe Button Clicked
        btnSaveRecipe.setOnClickListener(v -> saveRecipe());

        // Determine mode (Add/Edit)
        Intent intent = getIntent();
        String mode = intent.getStringExtra(MODE);
        if (MODE_EDIT.equals(mode)) {
            // Editing an existing recipe
            editRecipeId = intent.getIntExtra(RECIPE_ID, -1);
            if (editRecipeId != -1) {
                populateFieldsForEdit(editRecipeId);
            } else {
                Toast.makeText(this, "Invalid recipe ID.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            // Adding a new recipe
            // Optionally, set UI elements for Add mode
            TextView title = findViewById(R.id.title_text_view); // Ensure this ID exists
            title.setText("Add New Recipe");
        }
    }

    private void populateFieldsForEdit(int recipeId) {
        RecipeModel recipe = dbHelper.getCompleteRecipeById(recipeId); // Use complete method
        if (recipe != null) {
            // Populate title, description, instructions
            EditText etTitle = findViewById(R.id.et_recipe_title);
            EditText etDescription = findViewById(R.id.et_recipe_description);
            EditText etInstructions = findViewById(R.id.et_recipe_instructions);

            etTitle.setText(recipe.getTitle());
            etDescription.setText(recipe.getDescription());
            etInstructions.setText(recipe.getInstructions());

            // Set category
            int categoryPosition = categoryIdList.indexOf(recipe.getCategoryId());
            if (categoryPosition != -1) {
                spinnerCategory.setSelection(categoryPosition);
            }

            // Load ingredients
            List<IngredientQuantity> ingredients = dbHelper.getIngredientsByRecipeId(recipeId);
            for (IngredientQuantity iq : ingredients) {
                addIngredientToView(iq.getIngredientName(), iq.getQuantity());
                this.ingredients.add(iq);
            }

            // Load image
            if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
                selectedImageUri = recipe.getImageUrl();
                Log.d("AddRecipe", "Retrieved Image URI for Edit: " + selectedImageUri);

                // Use Glide to load the image with enhanced logging
                Glide.with(this)
                        .load(Uri.parse(selectedImageUri))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_error)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                        Target<Drawable> target, boolean isFirstResource) {
                                Log.e("AddRecipe", "Glide failed to load image: " + e.getMessage());
                                return false; // Allow Glide to handle the error drawable
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model,
                                                           Target<Drawable> target, DataSource dataSource,
                                                           boolean isFirstResource) {
                                Log.d("AddRecipe", "Glide successfully loaded image.");
                                return false;
                            }
                        })
                        .into(recipeImageView);
            } else {
                Log.d("AddRecipe", "No Image URI available for Recipe ID: " + recipeId);
                Glide.with(this)
                        .load(R.drawable.ic_placeholder)
                        .into(recipeImageView);
            }

            // Update UI elements for Edit mode
            TextView title = findViewById(R.id.title_text_view);
            title.setText("Edit Recipe");
        } else {
            Log.e("AddRecipe", "Failed to load recipe details for ID: " + recipeId);
            Toast.makeText(this, "Recipe details not found.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void openImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
                return;
            }
        } else { // Below API 33
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        // Permission already granted, launch image picker
        launchImagePicker();
    }

    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Grant temporary read permission to the URI
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Permission granted, launch image picker
                launchImagePicker();
            } else {
                // Permission denied, show a message to the user
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])){
                    new AlertDialog.Builder(this)
                            .setTitle("Permission Needed")
                            .setMessage("This permission is needed to select and display images for your recipes.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                ActivityCompat.requestPermissions(this, new String[]{permissions[0]}, PERMISSION_REQUEST_CODE);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.dismiss();
                                Toast.makeText(this, "Permission denied. Cannot select images.", Toast.LENGTH_SHORT).show();
                            })
                            .create()
                            .show();
                } else {
                    Toast.makeText(this, "Permission denied permanently. Please enable it from settings.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void loadCategories() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT id, category FROM Category", null);

            categoryList.clear();
            categoryIdList.clear();

            if(cursor != null && cursor.moveToFirst()){
                do{
                    int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                    categoryList.add(categoryName);
                    categoryIdList.add(categoryId);
                }while(cursor.moveToNext());
            }

            if(categoryList.isEmpty()){
                categoryList.add("No categories available");
            }
        } catch(Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Failed to load categories.", Toast.LENGTH_LONG).show();
            categoryList.add("Error loading categories");
        } finally {
            if(cursor != null) cursor.close();
            db.close();
        }

        // Set up spinner adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void showAddIngredientDialog(){
        final EditText etIngredientName = new EditText(this);
        final EditText etIngredientQuantity = new EditText(this);

        etIngredientName.setHint("Ingredient Name");
        etIngredientQuantity.setHint("Quantity");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Ingredient");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16); // Add padding for better UI
        layout.addView(etIngredientName);
        layout.addView(etIngredientQuantity);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String ingredientName = etIngredientName.getText().toString().trim();
            String quantity = etIngredientQuantity.getText().toString().trim();

            if(!ingredientName.isEmpty() && !quantity.isEmpty()){
                ingredients.add(new IngredientQuantity(ingredientName, quantity));
                addIngredientToView(ingredientName, quantity);
            } else {
                Toast.makeText(this, "Please enter both ingredient and quantity.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addIngredientToView(String ingredientName, String quantity){
        LinearLayout ingredientLayout = new LinearLayout(this);
        ingredientLayout.setOrientation(LinearLayout.HORIZONTAL);
        ingredientLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        ingredientLayout.setPadding(0, 8, 0, 8); // Add padding for better UI

        EditText etIngredientName = new EditText(this);
        etIngredientName.setText(ingredientName);
        etIngredientName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        etIngredientName.setEnabled(false); // Make it read-only
        ingredientLayout.addView(etIngredientName);

        EditText etIngredientQuantity = new EditText(this);
        etIngredientQuantity.setText(quantity);
        etIngredientQuantity.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        etIngredientQuantity.setEnabled(false); // Make it read-only
        ingredientLayout.addView(etIngredientQuantity);

        ingredientsContainer.addView(ingredientLayout);
    }

    private void saveRecipe(){
        String title = ((EditText) findViewById(R.id.et_recipe_title)).getText().toString().trim();
        String description = ((EditText) findViewById(R.id.et_recipe_description)).getText().toString().trim();
        String instructions = ((EditText) findViewById(R.id.et_recipe_instructions)).getText().toString().trim();

        if(categoryIdList.isEmpty() || spinnerCategory.getSelectedItemPosition() < 0){
            Toast.makeText(this, "Invalid category selection.", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoryId = categoryIdList.get(spinnerCategory.getSelectedItemPosition());

        if(title.isEmpty() || description.isEmpty() || instructions.isEmpty()){
            Toast.makeText(this, "Please fill in all recipe details.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current user's ID from SessionManager
        if(!sessionManager.isLoggedIn()){
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
            finish();
            return;
        }

        int userId = sessionManager.getUserId();

        Intent intent = getIntent();
        String mode = intent.getStringExtra(MODE);

        boolean success;
        if (MODE_EDIT.equals(mode)) {
            if (editRecipeId != -1) {
                success = updateRecipe(editRecipeId, userId, title, description, instructions, categoryId, ingredients);
            } else {
                Toast.makeText(this, "Invalid recipe ID.", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            success = addRecipe(userId, title, description, instructions, categoryId, ingredients);
        }

        if(success){
            Toast.makeText(this, MODE_EDIT.equals(mode) ? "Recipe updated successfully!" : "Recipe saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, MODE_EDIT.equals(mode) ? "Failed to update recipe." : "Failed to save recipe.", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean addRecipe(int userId, String title, String description, String instructions, int categoryId, List<IngredientQuantity> ingredients){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try{
            Log.d("AddRecipe", "Starting to add a new recipe.");

            // Insert the recipe into the Recipe table
            ContentValues recipeValues = new ContentValues();
            recipeValues.put("user_id", userId);
            recipeValues.put("title", title);
            recipeValues.put("description", description);
            recipeValues.put("instructions", instructions);
            recipeValues.put("category_id", categoryId);

            long recipeId = db.insert("Recipe", null, recipeValues);
            Log.d("AddRecipe", "Inserted Recipe with ID: " + recipeId);
            if(recipeId == -1) throw new Exception("Failed to insert recipe");

            // Insert ingredients into the RecipeIngredient table
            for(IngredientQuantity iq : ingredients){
                Log.d("AddRecipe", "Processing ingredient: " + iq.getIngredientName());

                // Check if ingredient already exists
                Cursor cursor = db.query("Ingredient", new String[]{"id"}, "emri = ?", new String[]{iq.getIngredientName()}, null, null, null);
                int ingredientId;
                if(cursor.moveToFirst()){
                    ingredientId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    Log.d("AddRecipe", "Found existing Ingredient ID: " + ingredientId);
                } else {
                    // Insert new ingredient
                    ContentValues ingredientValues = new ContentValues();
                    ingredientValues.put("emri", iq.getIngredientName());

                    long id = db.insert("Ingredient", null, ingredientValues);
                    Log.d("AddRecipe", "Inserted Ingredient with ID: " + id);
                    if(id == -1) throw new Exception("Failed to insert ingredient");
                    ingredientId = (int) id;
                }
                cursor.close();

                // Link ingredient to recipe
                ContentValues recipeIngredientValues = new ContentValues();
                recipeIngredientValues.put("recipe_id", recipeId);
                recipeIngredientValues.put("ingredient_id", ingredientId);
                recipeIngredientValues.put("sasia", iq.getQuantity()); // Quantity as String

                long result = db.insert("RecipeIngredient", null, recipeIngredientValues);
                Log.d("AddRecipe", "Linked Ingredient ID " + ingredientId + " to Recipe ID " + recipeId + " with result: " + result);
                if(result == -1) throw new Exception("Failed to link ingredient to recipe");
            }

            // Insert the image URL into the Image table (if an image was selected)
            if(selectedImageUri != null && !selectedImageUri.isEmpty()){
                Log.d("AddRecipe", "Inserting Image URL: " + selectedImageUri);

                ContentValues imageValues = new ContentValues();
                imageValues.put("recipe_id", recipeId);
                imageValues.put("image_url", selectedImageUri);

                long imageId = db.insert("Image", null, imageValues);
                Log.d("AddRecipe", "Inserted Image with ID: " + imageId);
                if(imageId == -1) throw new Exception("Failed to insert image into Image table");
            } else {
                Log.d("AddRecipe", "No image selected. Skipping image insertion.");
            }

            db.setTransactionSuccessful();
            Log.d("AddRecipe", "Recipe added successfully.");
            return true;
        } catch(Exception e){
            Log.e("AddRecipe", "Error adding recipe: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public boolean updateRecipe(int recipeId, int userId, String title, String description, String instructions, int categoryId, List<IngredientQuantity> ingredients){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try{
            Log.d("AddRecipe", "Starting to update recipe ID: " + recipeId);

            // Update the recipe in the Recipe table
            ContentValues recipeValues = new ContentValues();
            recipeValues.put("user_id", userId);
            recipeValues.put("title", title);
            recipeValues.put("description", description);
            recipeValues.put("instructions", instructions);
            recipeValues.put("category_id", categoryId);

            int rowsAffected = db.update("Recipe", recipeValues, "id = ?", new String[]{String.valueOf(recipeId)});
            Log.d("AddRecipe", "Rows affected in Recipe table: " + rowsAffected);
            if(rowsAffected <= 0) throw new Exception("Failed to update recipe");

            // Update ingredients
            // For simplicity, delete existing and re-insert
            db.delete("RecipeIngredient", "recipe_id = ?", new String[]{String.valueOf(recipeId)});
            Log.d("AddRecipe", "Deleted existing ingredients for Recipe ID: " + recipeId);

            for(IngredientQuantity iq : ingredients){
                Log.d("AddRecipe", "Processing ingredient: " + iq.getIngredientName());

                // Check if ingredient already exists
                Cursor cursor = db.query("Ingredient", new String[]{"id"}, "emri = ?", new String[]{iq.getIngredientName()}, null, null, null);
                int ingredientId;
                if(cursor.moveToFirst()){
                    ingredientId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    Log.d("AddRecipe", "Found existing Ingredient ID: " + ingredientId);
                } else {
                    // Insert new ingredient
                    ContentValues ingredientValues = new ContentValues();
                    ingredientValues.put("emri", iq.getIngredientName());

                    long id = db.insert("Ingredient", null, ingredientValues);
                    Log.d("AddRecipe", "Inserted Ingredient with ID: " + id);
                    if(id == -1) throw new Exception("Failed to insert ingredient");
                    ingredientId = (int) id;
                }
                cursor.close();

                // Link ingredient to recipe
                ContentValues recipeIngredientValues = new ContentValues();
                recipeIngredientValues.put("recipe_id", recipeId);
                recipeIngredientValues.put("ingredient_id", ingredientId);
                recipeIngredientValues.put("sasia", iq.getQuantity());

                long result = db.insert("RecipeIngredient", null, recipeIngredientValues);
                Log.d("AddRecipe", "Linked Ingredient ID " + ingredientId + " to Recipe ID " + recipeId + " with result: " + result);
                if(result == -1) throw new Exception("Failed to link ingredient to recipe");
            }

            // Update the image URL in the Image table
            if(selectedImageUri != null && !selectedImageUri.isEmpty()){
                Log.d("AddRecipe", "Updating Image URL: " + selectedImageUri);

                ContentValues imageValues = new ContentValues();
                imageValues.put("image_url", selectedImageUri);

                int imageRows = db.update("Image", imageValues, "recipe_id = ?", new String[]{String.valueOf(recipeId)});
                Log.d("AddRecipe", "Rows affected in Image table: " + imageRows);
                if(imageRows == 0){
                    // No existing image, insert new
                    imageValues.put("recipe_id", recipeId);
                    long imageId = db.insert("Image", null, imageValues);
                    Log.d("AddRecipe", "Inserted Image with ID: " + imageId);
                    if(imageId == -1) throw new Exception("Failed to insert image into Image table");
                }
            } else {
                // If no image is selected, optionally remove existing image
                db.delete("Image", "recipe_id = ?", new String[]{String.valueOf(recipeId)});
                Log.d("AddRecipe", "Deleted existing image for Recipe ID: " + recipeId);
            }

            db.setTransactionSuccessful();
            Log.d("AddRecipe", "Recipe updated successfully.");
            return true;
        } catch(Exception e){
            Log.e("AddRecipe", "Error updating recipe: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // IngredientQuantity Class
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
