package com.example.myapplication;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;


import com.example.myapplication.dao.CategoryDao;
import com.example.myapplication.dao.CategoryDatabase;
import com.example.myapplication.databinding.ActivityAddCategoryBinding;
import com.example.myapplication.model.Category;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddCategoryActivity extends BaseActivity {
    ActivityAddCategoryBinding binding;
    CategoryDao categoryDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        binding = ActivityAddCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        categoryDao = CategoryDatabase.getInstance(this).categoryDao();

        binding.btnAddCategory.setOnClickListener(view -> {
            String name = binding.etAddCategory.getText().toString().trim();
            if (!name.isEmpty()) {
                executorService.execute(() -> {
                    categoryDao.insert(new Category(name));
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    });
                });
            } else {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show();
            }
        });
    }
}