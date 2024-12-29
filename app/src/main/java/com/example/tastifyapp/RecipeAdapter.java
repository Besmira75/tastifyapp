package com.example.tastifyapp;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<RecipeModel> recipeList;

    public RecipeAdapter(List<RecipeModel> recipeList) {
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        // Get the RecipeModel at this position
        RecipeModel recipe = recipeList.get(position);

        // Set the title and description
        holder.tvRecipeTitle.setText(recipe.getTitle());
        holder.tvRecipeSubtitle.setText(recipe.getDescription());

        // Log the image URI
        Log.d("RecipeAdapter", "Loading image URI: " + recipe.getImageUrl());

        // Load the image using Glide
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Uri imageUri = Uri.parse(recipe.getImageUrl());
            Glide.with(holder.itemView.getContext())
                    .load(imageUri)
                    .placeholder(R.drawable.ic_placeholder) // Ensure you have this drawable
                    .error(R.drawable.ic_error)             // Ensure you have this drawable
                    .into(holder.imgRecipe);
        } else {
            // Set a default image or placeholder if no image is available
            holder.imgRecipe.setImageResource(R.drawable.ic_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    // Method to update the data and refresh the RecyclerView
    public void updateData(List<RecipeModel> newList) {
        this.recipeList = newList;
        notifyDataSetChanged();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {

        ImageView imgRecipe;
        TextView tvRecipeTitle;
        TextView tvRecipeSubtitle;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRecipe = itemView.findViewById(R.id.imgRecipe);
            tvRecipeTitle = itemView.findViewById(R.id.tvRecipeTitle);
            tvRecipeSubtitle = itemView.findViewById(R.id.tvRecipeSubtitle);
        }
    }
}
