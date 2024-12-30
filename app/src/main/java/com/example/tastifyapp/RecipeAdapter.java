// com/example/tastifyapp/RecipeAdapter.java
package com.example.tastifyapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import androidx.core.content.ContextCompat;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<RecipeModel> recipeList;
    private OnRecipeListener mOnRecipeListener;
    private boolean showEditDelete;  // Flag to control button visibility

    public RecipeAdapter(List<RecipeModel> recipeList, OnRecipeListener onRecipeListener, boolean showEditDelete) {
        this.recipeList = recipeList;
        this.mOnRecipeListener = onRecipeListener;
        this.showEditDelete = showEditDelete;  // Set based on the fragment using the adapter
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_recipe, parent, false);
        return new RecipeViewHolder(view, mOnRecipeListener, showEditDelete);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeModel recipe = recipeList.get(position);
        holder.tvRecipeTitle.setText(recipe.getTitle());
        holder.tvRecipeSubtitle.setText(recipe.getDescription());
        holder.tvRecipeCreator.setText("by " + recipe.getName());

        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(recipe.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .into(holder.imgRecipe);
        } else {
            holder.imgRecipe.setImageResource(R.drawable.ic_placeholder);
        }

        // Set visibility of edit and delete buttons
        holder.btnEditRecipe.setVisibility(showEditDelete ? View.VISIBLE : View.GONE);
        holder.btnDeleteRecipe.setVisibility(showEditDelete ? View.VISIBLE : View.GONE);
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
    public List<RecipeModel> getRecipeList() {
        return recipeList;
    }
    public static class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imgRecipe;
        TextView tvRecipeTitle, tvRecipeSubtitle, tvRecipeCreator;
        Button btnEditRecipe, btnDeleteRecipe;
        OnRecipeListener onRecipeListener;

        RecipeViewHolder(@NonNull View itemView, OnRecipeListener onRecipeListener, boolean showEditDelete) {
            super(itemView);
            imgRecipe = itemView.findViewById(R.id.imgRecipe);
            tvRecipeTitle = itemView.findViewById(R.id.tvRecipeTitle);
            tvRecipeSubtitle = itemView.findViewById(R.id.tvRecipeSubtitle);
            tvRecipeCreator = itemView.findViewById(R.id.tvRecipeCreator); // Updated TextView for name
            btnEditRecipe = itemView.findViewById(R.id.btnEditRecipe);
            btnDeleteRecipe = itemView.findViewById(R.id.btnDeleteRecipe);
            this.onRecipeListener = onRecipeListener;

            if (showEditDelete) {
                btnEditRecipe.setOnClickListener(this);
                btnDeleteRecipe.setOnClickListener(this);
            }

            // Set click listener for the entire item view
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onRecipeListener != null){
                        onRecipeListener.onItemClick(getAdapterPosition());
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == btnEditRecipe.getId()) {
                onRecipeListener.onEditClick(getAdapterPosition());
            } else if (v.getId() == btnDeleteRecipe.getId()) {
                onRecipeListener.onDeleteClick(getAdapterPosition());
            }
        }
    }

    public interface OnRecipeListener {
        void onItemClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }
}
