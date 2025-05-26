package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.dao.CategoryDao;
import com.example.myapplication.dao.CategoryDatabase;
import com.example.myapplication.databinding.FragmentExpensesBinding;
import com.example.myapplication.model.Category;
import com.example.myapplication.model.Expense;
import com.example.myapplication.repository.ExpenseRepository;
import com.example.myapplication.repository.IApiCallback;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ExpensesFragment extends Fragment {

    private Uri imageUri;
    private String savedImagePath;
    private FragmentExpensesBinding binding;
    private FirebaseAuth mAuth;
    private MainActivity mainActivity;
    private ExpenseRepository expenseRepository;
    private boolean isLoading = false;
    private CategoryDao categoryDao;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchCameraIntent();
                } else {
                    Toast.makeText(getContext(), "Camera permission is required to take pictures.", Toast.LENGTH_SHORT).show();
                }
            });

    private void launchCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                    if (bitmap != null) {
                        savedImagePath = saveImageLocally(bitmap);
                        binding.imagePreview.setImageBitmap(bitmap);
                        binding.imagePreviewContainer.setVisibility(View.VISIBLE);
                        binding.btnPickImage.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(requireContext(), "Failed to capture image.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), selectedUri);
                        savedImagePath = saveImageLocally(bitmap);
                        if (savedImagePath != null) {
                            imageUri = Uri.fromFile(new File(savedImagePath));
                            binding.imagePreview.setImageURI(imageUri);
                            binding.imagePreviewContainer.setVisibility(View.VISIBLE);
                            binding.btnPickImage.setVisibility(View.GONE);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExpensesBinding.inflate(inflater, container, false);

        binding.btnSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCategories();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivity = (MainActivity) requireActivity();
        mAuth = FirebaseAuth.getInstance();
        expenseRepository = new ExpenseRepository();

        CategoryDatabase db = CategoryDatabase.getInstance(getContext());
        categoryDao = db.categoryDao();

        if (categoryDao.getAllCategories().isEmpty()) {
            categoryDao.insert(new Category("Food"));
            categoryDao.insert(new Category("Transport"));
            categoryDao.insert(new Category("Shopping"));
            categoryDao.insert(new Category("Entertainment"));
        }

        loadCategories();

        binding.imageFilterViewAddCategory.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddCategoryActivity.class);
            startActivity(intent);
        });

        binding.btnCancelImage.setOnClickListener(v -> {
            binding.imagePreviewContainer.setVisibility(View.GONE);
            binding.btnPickImage.setVisibility(View.VISIBLE);
            imageUri = null;
        });

        binding.btnPickImage.setOnClickListener(v -> showImagePickerDialog());
        binding.btnAddExpense.setOnClickListener(v -> onAddExpenseClicked());
    }

    private void onAddExpenseClicked() {
        isLoading = true;
        showProgressBar();

        String amount = binding.editTextAmount.getText().toString();
        int selectedRadioButtonId = binding.radioGroup.getCheckedRadioButtonId();

        if (amount.trim().isEmpty() || selectedRadioButtonId == -1) {
            Toast.makeText(requireContext(), "Please enter an amount and select a currency.", Toast.LENGTH_SHORT).show();
            hideProgressBar();
            return;
        }

        RadioButton selectedRadioButton = binding.radioGroup.findViewById(selectedRadioButtonId);
        String currency = selectedRadioButton.getText().toString();

        String category = binding.spinnerCategory.getSelectedItem().toString();
        String remark = binding.editTextRemark.getText().toString();
        String createdBy = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "null";
        Date createdDate = new Date();
        String id = UUID.randomUUID().toString();

        Expense expense = new Expense(id, Double.parseDouble(amount), currency, category, remark, createdBy, createdDate);

        if (savedImagePath != null) {
            expense.setReceiptImageUrl(savedImagePath);
        }

        expenseRepository.createExpense(expense, new IApiCallback<Expense>() {
            @Override
            public void onSuccess(Expense result) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Expense added successfully!", Toast.LENGTH_SHORT).show();
                    if (mainActivity != null) {
                        if ((result.getCurrency().equals("USD") && result.getAmount() > 100) ||
                                (result.getCurrency().equals("KHR") && result.getAmount() > 400000)) {
                            mainActivity.showBasicNotification(result.getRemark());
                        }
                    }
                    clearInputFields();
                    if (mainActivity != null) {
                        mainActivity.binding.bottomNavigation.setSelectedItemId(R.id.nav_expense_list);
                    }
                }
                hideProgressBar();
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), errorMessage != null ? errorMessage : "Unknown error", Toast.LENGTH_SHORT).show();
                }
                hideProgressBar();
            }
        });
    }

    private void clearInputFields() {
        binding.editTextAmount.setText("");
        binding.editTextRemark.setText("");
        binding.radioGroup.clearCheck();
        binding.spinnerCategory.setSelection(0);
        binding.imagePreview.setVisibility(View.GONE);
        binding.imagePreviewContainer.setVisibility(View.GONE);
        binding.btnPickImage.setVisibility(View.VISIBLE);
        imageUri = null;
        savedImagePath = null;
    }

    private void showProgressBar() {
        if (binding != null) {
            binding.tasksProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressBar() {
        if (binding != null) {
            binding.tasksProgressBar.setVisibility(View.GONE);
        }
    }

    private void loadCategories() {
        List<Category> categories = categoryDao.getAllCategories();
        List<String> names = new ArrayList<>();
        for (Category cat : categories) {
            names.add(cat.name);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);
    }

    private void showImagePickerDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Image")
                .setItems(new String[]{"Camera", "Gallery"}, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                })
                .show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCameraIntent();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Camera Permission Needed")
                    .setMessage("This app needs camera access to take receipt photos.")
                    .setPositiveButton("Allow", (dialog, which) -> {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private String saveImageLocally(Bitmap bitmap) {
        File dir = new File(requireContext().getFilesDir(), "receipts");
        if (!dir.exists()) dir.mkdir();

        String fileName = "receipt_" + System.currentTimeMillis() + ".jpg";
        File file = new File(dir, fileName);

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
