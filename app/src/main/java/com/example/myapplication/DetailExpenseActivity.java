package com.example.myapplication;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.databinding.ActivityDetailExpenseBinding;
import com.example.myapplication.model.Expense;
import com.example.myapplication.service.ExpenseService;
import com.example.myapplication.utill.RetrofitClient;

import java.io.File;
import java.text.SimpleDateFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailExpenseActivity extends AppCompatActivity {

    private ActivityDetailExpenseBinding binding;
    private ExpenseService expenseService;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        expenseService = RetrofitClient.getClient().create(ExpenseService.class);

        String expenseId = getIntent().getStringExtra("ExpenseId");

        if (expenseId == null) {
            Toast.makeText(this, "Error: No expense ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchExpenseDetails(expenseId);
    }

    private void fetchExpenseDetails(String expenseId) {
        expenseService.getExpense(expenseId).enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Expense expense = response.body();

                    binding.currencyText.setText(expense.getCurrency());
                    binding.amountText.setText("Amount: " + expense.getAmount());
                    binding.categoryText.setText("Category: " + expense.getCategory());

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String dateText = (expense.getCreatedDate() != null)
                            ? dateFormat.format(expense.getCreatedDate())
                            : "N/A";
                    binding.tvDate.setText("Date: " + dateText);
                    binding.remarkText.setText("Remark: " + expense.getRemark());

                    String localPath = expense.getReceiptImage();
                    if (localPath != null && !localPath.isEmpty()) {
                        File imageFile = new File(localPath);
                        if (imageFile.exists()) {
                            Glide.with(DetailExpenseActivity.this)
                                    .load(Uri.fromFile(imageFile))
                                    .into(binding.imageViewReceipt);
                        } else {
                            Toast.makeText(DetailExpenseActivity.this, "Image file not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(DetailExpenseActivity.this, "Expense not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                Toast.makeText(DetailExpenseActivity.this, "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
