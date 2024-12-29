package com.example.tastifyapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AddRecipe extends AppCompatActivity {

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

    private static final int IMAGE_PICK_CODE = 100;

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
        recipeImageView = findViewById(R.id.iv_recipe_image); // Ensure this ID matches your XML
        spinnerCategory = findViewById(R.id.spinner_category);
        ingredientsContainer = findViewById(R.id.ingredients_container);
        btnAddIngredient = findViewById(R.id.btn_add_ingredient);
        btnSaveRecipe = findViewById(R.id.btn_save_recipe);
        btnSelectImage = findViewById(R.id.btn_select_image);

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
    }

    private void openImagePicker() {
        // Check for runtime permissions if targeting API 23+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, IMAGE_PICK_CODE);
                return;
            }
        }
        // Launch the image picker
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == IMAGE_PICK_CODE){
            if(grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED){
                // Permission granted, launch image picker
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null){
            Uri imageUri = data.getData();
            selectedImageUri = imageUri.toString();
            recipeImageView.setImageURI(imageUri);
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

        EditText etIngredientName = new EditText(this);
        etIngredientName.setText(ingredientName);
        etIngredientName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        ingredientLayout.addView(etIngredientName);

        EditText etIngredientQuantity = new EditText(this);
        etIngredientQuantity.setText(quantity);
        etIngredientQuantity.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
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

        if(addRecipe(userId, title, description, instructions, categoryId, ingredients)){
            Toast.makeText(this, "Recipe saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save recipe.", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean addRecipe(int userId, String title, String description, String instructions, int categoryId, List<IngredientQuantity> ingredients){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try{
            // Insert the recipe into the Recipe table
            ContentValues recipeValues = new ContentValues();
            recipeValues.put("user_id", userId);
            recipeValues.put("title", title);
            recipeValues.put("description", description);
            recipeValues.put("instructions", instructions);
            recipeValues.put("category_id", categoryId);

            long recipeId = db.insert("Recipe", null, recipeValues);
            if(recipeId == -1) throw new Exception("Failed to insert recipe");

            // Insert ingredients into the RecipeIngredient table
            for(IngredientQuantity iq : ingredients){
                // Check if ingredient already exists
                Cursor cursor = db.query("Ingredient", new String[]{"id"}, "emri = ?", new String[]{iq.getIngredientName()}, null, null, null);
                int ingredientId;
                if(cursor.moveToFirst()){
                    ingredientId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                } else {
                    // Insert new ingredient
                    ContentValues ingredientValues = new ContentValues();
                    ingredientValues.put("emri", iq.getIngredientName());
                    ingredientValues.put("category_id", categoryId); // Ensure category_id is set if required
                    long id = db.insert("Ingredient", null, ingredientValues);
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
                if(result == -1) throw new Exception("Failed to link ingredient to recipe");
            }

            // Insert the image URL into the Image table (if an image was selected)
            if(selectedImageUri != null && !selectedImageUri.isEmpty()){
                ContentValues imageValues = new ContentValues();
                imageValues.put("recipe_id", recipeId);
                imageValues.put("image_url", selectedImageUri);

                long imageId = db.insert("Image", null, imageValues);
                if(imageId == -1) throw new Exception("Failed to insert image into Image table");
            }

            db.setTransactionSuccessful();
            return true;
        } catch(Exception e){
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
