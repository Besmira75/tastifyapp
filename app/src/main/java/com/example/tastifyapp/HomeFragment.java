package com.example.tastifyapp;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tastifyapp.adapters.RecipeAdapter;
import com.example.tastifyapp.adapters.CategoryAdapter;
import com.example.tastifyapp.R;
import com.example.tastifyapp.data.DB;

public class HomeFragment extends Fragment {

    private RecyclerView rvCategories;
    private RecyclerView rvRecipes;
    private CategoryAdapter categoryAdapter;
    private RecipeAdapter recipeAdapter;
    private DB database;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        rvCategories = rootView.findViewById(R.id.rvCategories);
        rvRecipes = rootView.findViewById(R.id.rvRecipes);

        setupRecyclerViews();

        return rootView;
    }

    private void setupRecyclerViews() {
        database = DB.getInstance(getContext());

        // Set up horizontal RecyclerView for categories
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvCategories.setLayoutManager(horizontalLayoutManager);
        categoryAdapter = new CategoryAdapter(getContext(), database.getCategories());
        rvCategories.setAdapter(categoryAdapter);

        // Set up vertical RecyclerView for recipes
        LinearLayoutManager verticalLayoutManager = new LinearLayoutManager(getContext());
        rvRecipes.setLayoutManager(verticalLayoutManager);
        recipeAdapter = new RecipeAdapter(getContext(), database.getRecipes());
        rvRecipes.setAdapter(recipeAdapter);
    }
}
