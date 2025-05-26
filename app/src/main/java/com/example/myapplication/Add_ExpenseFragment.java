package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.ExpenseAdapter;
import com.example.myapplication.databinding.FragmentAddExpenseBinding;
import com.example.myapplication.model.Expense;
import com.example.myapplication.repository.ExpenseRepository;
import com.example.myapplication.repository.IApiCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class Add_ExpenseFragment extends Fragment {

    private FragmentAddExpenseBinding binding;
    private ExpenseAdapter expenseAdapter;
    private ExpenseRepository repository;
    private FirebaseAuth mAuth;

    private int currentPage = 1;
    private boolean isLoading = false;
    private static final int PRE_LOAD_ITEMS = 1;

    public Add_ExpenseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddExpenseBinding.inflate(inflater, container, false);

        mAuth = FirebaseAuth.getInstance();
        repository = new ExpenseRepository();
        expenseAdapter = new ExpenseAdapter();

        setupRecyclerView();
        setupSignOutButton();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.rcLexpenseList.setLayoutManager(layoutManager);
        binding.rcLexpenseList.setAdapter(expenseAdapter);

        // Swipe to delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Expense expense = expenseAdapter.getExpenseAt(position);

                if (expense != null) {
                    repository.deleteExpense(expense.getId(), new IApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            expenseAdapter.removeExpense(position);
                            Toast.makeText(getContext(), "Expense deleted", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Toast.makeText(getContext(), "Failed to delete: " + errorMessage, Toast.LENGTH_SHORT).show();
                            expenseAdapter.notifyItemChanged(position); // Undo swipe
                        }
                    });
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.rcLexpenseList);

        // Pagination / Lazy loading
        binding.rcLexpenseList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (manager == null) return;

                int totalItemCount = manager.getItemCount();
                int lastVisibleItem = manager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItem + PRE_LOAD_ITEMS)) {
                    loadExpenses(false);
                }
            }
        });
    }

    private void setupSignOutButton() {
        binding.btnSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        currentPage = 1;
        loadExpenses(true);
    }

    private void loadExpenses(boolean reset) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isLoading) return; // Prevent multiple parallel calls
        isLoading = true;
        showProgressBar();

        String currentUserId = user.getUid();
        repository.getExpenses(currentPage, currentUserId, new IApiCallback<List<Expense>>() {
            @Override
            public void onSuccess(List<Expense> expenses) {
                if (reset) {
                    expenseAdapter.setExpenses(expenses);
                } else {
                    expenseAdapter.addExpenses(expenses);
                }

                if (!expenses.isEmpty()) {
                    currentPage++;
                }

                isLoading = false;
                hideProgressBar();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Error loading expenses: " + errorMessage, Toast.LENGTH_SHORT).show();
                isLoading = false;
                hideProgressBar();
            }
        });
    }

    private void showProgressBar() {
        if (binding != null) binding.tasksProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (binding != null) binding.tasksProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
