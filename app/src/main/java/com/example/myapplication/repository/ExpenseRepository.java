package com.example.myapplication.repository;

import com.example.myapplication.model.Expense;
import com.example.myapplication.service.ExpenseService;
import com.example.myapplication.utill.RetrofitClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpenseRepository {
    private ExpenseService expenseService;
    private static final int PAGE_SIZE = 5;

    public ExpenseRepository() {
        expenseService = RetrofitClient.getClient().create(ExpenseService.class);
    }

    public void getAllExpenses(String createdBy, final IApiCallback<List<Expense>> callback) {
        List<Expense> allExpenses = new ArrayList<>();
        int currentPage = 1;
        loadExpensesRecursively(createdBy, currentPage, allExpenses, callback);
    }

    public void getExpense(String expenseId, final IApiCallback<Expense> callback) {
        expenseService.getExpense(expenseId).enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getExpenses(int page, String createdBy, final IApiCallback<List<Expense>> callback) {
        Call<List<Expense>> call = expenseService.getExpenses(page, PAGE_SIZE, createdBy);

        call.enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<Expense>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteExpense(String expenseId, final IApiCallback<Void> callback) {
        expenseService.deleteExpense(expenseId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null); // Fix: Void means you should return null
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }


    public void createExpense(Expense expense, final IApiCallback<Expense> callback) {
        expenseService.createExpense(expense).enqueue(new Callback<Expense>() {
            @Override
            public void onResponse(Call<Expense> call, Response<Expense> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Expense> call, Throwable t) {
                String error = t.getMessage() != null ? t.getMessage() : "Unknown error occurred";
                callback.onError("Error: " + error);
            }
        });
    }

    public void getLastExpense(String createdBy, final IApiCallback<Expense> callback) {
        // Fetch first page with size 1 to get the most recent expense
        expenseService.getExpenses(1, 1, createdBy).enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0)); // First (latest) expense
                } else {
                    callback.onError("No expenses found");
                }
            }

            @Override
            public void onFailure(Call<List<Expense>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }


    private String getErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                return "Error: " + response.code() + " - " + response.errorBody().string();
            }
        } catch (IOException e) {
            return "Error: " + response.code() + " (failed to read error body)";
        }
        return "Unknown error: " + response.code();
    }

    private void loadExpensesRecursively(final String createdBy, final int currentPage, final List<Expense> allExpenses, final IApiCallback<List<Expense>> callback) {
        expenseService.getExpenses(currentPage, PAGE_SIZE, createdBy).enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Expense> expenses = response.body();
                    allExpenses.addAll(expenses);

                    if (expenses.size() < PAGE_SIZE) {
                        callback.onSuccess(allExpenses);
                    } else {
                        loadExpensesRecursively(createdBy, currentPage + 1, allExpenses, callback);
                    }
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<Expense>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
