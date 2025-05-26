package com.example.myapplication.service;

import com.example.myapplication.model.Expense;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ExpenseService {
    @GET("expenses?_expand=category&_sort=createdDate&_order=desc")
    Call<List<Expense>> getExpenses(
            @Query("_page") int page,
            @Query("_limit") int limit,
            @Query("createdBy") String createdBy
    );

    @GET("expenses/{id}")
    Call<Expense> getExpense(@Path("id") String id);

    @POST("expenses")  // FIXED ENDPOINT
    Call<Expense> createExpense(@Body Expense expense);

    @DELETE("expenses/{id}")  // FIXED ENDPOINT
    Call<Void> deleteExpense(@Path("id") String expenseId);
}
