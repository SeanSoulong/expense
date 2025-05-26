package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.myapplication.model.Expense;
import com.example.myapplication.repository.ExpenseRepository;
import com.example.myapplication.repository.IApiCallback;
import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {

    private FirebaseAuth mAuth;
    private ExpenseRepository expenseRepository;
    private TextView lastExpenseText;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        expenseRepository = new ExpenseRepository();

        lastExpenseText = view.findViewById(R.id.textView2);

        fetchLastExpense();

        return view;
    }

    private void fetchLastExpense() {
        String userId = mAuth.getCurrentUser().getUid();

        expenseRepository.getLastExpense(userId, new IApiCallback<Expense>() {
            @Override
            public void onSuccess(Expense expense) {
                String text = "My last expense was " + expense.getAmount() + " " + expense.getCurrency(); // Adjust according to your model
                lastExpenseText.setText(text);
            }

            @Override
            public void onError(String errorMessage) {
                lastExpenseText.setText(getActivity().getString(R.string.no_expenses_found));
            }
        });
    }
}