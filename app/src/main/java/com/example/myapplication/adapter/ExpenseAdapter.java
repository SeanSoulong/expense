package com.example.myapplication.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.DetailExpenseActivity;
import com.example.myapplication.databinding.ListExpenseBinding;
import com.example.myapplication.model.Expense;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public ExpenseAdapter() {
        this.expenses = new ArrayList<>();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListExpenseBinding binding = ListExpenseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ExpenseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);

        holder.binding.tvAmount.setText(String.valueOf(expense.getAmount()));
        holder.binding.tvCurrency.setText(expense.getCurrency());

        if (expense.getCreatedDate() != null) {
            holder.binding.tvDate.setText(dateFormat.format(expense.getCreatedDate()));
        } else {
            holder.binding.tvDate.setText("N/A");
        }

        holder.binding.chCategory.setText(expense.getCategory());
        holder.binding.tvRemark.setText(expense.getRemark());

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(holder.itemView.getContext(), DetailExpenseActivity.class);
            intent.putExtra("ExpenseId", expense.getId());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void setExpenses(List<Expense> newExpenses) {
        expenses.clear();
        expenses.addAll(newExpenses);
        notifyDataSetChanged();
    }

    public void addExpenses(List<Expense> newExpenses) {
        int startPosition = expenses.size();
        expenses.addAll(newExpenses);
        notifyItemRangeInserted(startPosition, newExpenses.size());
    }

    public void removeExpense(int position) {
        if (position >= 0 && position < expenses.size()) {
            expenses.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Expense getExpenseAt(int position) {
        if (position >= 0 && position < expenses.size()) {
            return expenses.get(position);
        }
        return null;
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        protected ListExpenseBinding binding;

        public ExpenseViewHolder(@NonNull ListExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
