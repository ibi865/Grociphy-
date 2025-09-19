package com.example.homescreen.Admin_Panel;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.homescreen.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Add_Product extends AppCompatActivity {

    private EditText productNameEditText, productPriceEditText, productDescriptionEditText;
    private MaterialButton btnAddItem, btnChooseImage;
    private Uri selectedImageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private LottieAnimationView successAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // Initialize views
        productNameEditText = findViewById(R.id.productNameEditText);
        productPriceEditText = findViewById(R.id.productPriceEditText);
        productDescriptionEditText = findViewById(R.id.productDescriptionEditText);
        btnAddItem = findViewById(R.id.btnAddItem);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        ImageView btnBack = findViewById(R.id.btnBack);
        successAnimation = findViewById(R.id.success_animation);

        // Back button click
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(Add_Product.this, Admin_Panel.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show();
                    }
                });

        // Choose image button
        btnChooseImage.setOnClickListener(v -> openImageChooser());

        // Add product button
        btnAddItem.setOnClickListener(v -> addProduct());
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Product Image"));
    }

    private void addProduct() {
        String name = productNameEditText.getText().toString().trim();
        String price = productPriceEditText.getText().toString().trim();
        String description = productDescriptionEditText.getText().toString().trim();

        if (name.isEmpty() || price.isEmpty() || description.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Uploading product...", Toast.LENGTH_SHORT).show();

        // Upload to Cloudinary
        MediaManager.get().upload(selectedImageUri)
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}

                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();
                        saveProductToFirebase(name, price, description, imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(Add_Product.this, "Image upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void saveProductToFirebase(String name, String price, String description, String imageUrl) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("products");
        String productId = databaseRef.push().getKey();

        Map<String, Object> productData = new HashMap<>();
        productData.put("productId", productId);
        productData.put("name", name);
        productData.put("price", price);
        productData.put("description", description);
        productData.put("imageUrl", imageUrl);

        if (productId != null) {
            databaseRef.child(productId).setValue(productData)
                    .addOnSuccessListener(unused -> {
                        showSuccessAnimation();
                        clearForm();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to upload product", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showSuccessAnimation() {
        // Hide the add button temporarily
        btnAddItem.setVisibility(View.GONE);

        // Show and play the animation
        successAnimation.setVisibility(View.VISIBLE);
        successAnimation.playAnimation();

        // Add listener to handle animation completion
        successAnimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Toast.makeText(Add_Product.this, "Product uploaded successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Hide animation and show button again after animation completes
                successAnimation.setVisibility(View.GONE);
                btnAddItem.setVisibility(View.VISIBLE);
            }

            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });
    }


    private void clearForm() {
        productNameEditText.setText("");
        productPriceEditText.setText("");
        productDescriptionEditText.setText("");
        selectedImageUri = null;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view instanceof EditText) {
            Rect outRect = new Rect();
            view.getGlobalVisibleRect(outRect);
            if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                view.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
