package com.example.homescreen.Admin_Panel;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.homescreen.R;
import com.example.homescreen.adapter_packages.View_All_Adapter;
import com.example.homescreen.model.Product;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.*;

import java.io.IOException;
import java.util.*;

public class View_Products extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View_All_Adapter adapter;
    private List<Product> productList;
    private Uri selectedImageUri = null;
    private String oldImageUrl = null;
    private String selectedProductId = null;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView imagePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_products);

        recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();

        ImageView btnback=findViewById(R.id.btnBack);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




        adapter = new View_All_Adapter(this, productList, new View_All_Adapter.OnProductClickListener() {
            @Override
            public void onEditClick(Product product) {
                showEditDialog(product);
            }

            @Override
            public void onDeleteClick(Product product) {
                deleteProduct(product);
            }
        });

        recyclerView.setAdapter(adapter);



        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (imagePreview != null) {
                            imagePreview.setImageURI(selectedImageUri);
                        }
                        Toast.makeText(this, "New image selected", Toast.LENGTH_SHORT).show();
                    }
                });

        fetchProductsFromFirebase();
    }

    private void fetchProductsFromFirebase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("products");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Product product = snap.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(View_Products.this, "Failed to load products.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_product, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        TextInputEditText name = dialogView.findViewById(R.id.productNameEditText);
        TextInputEditText price = dialogView.findViewById(R.id.productPriceEditText);
        TextInputEditText description = dialogView.findViewById(R.id.productDescriptionEditText);
        MaterialButton chooseImageBtn = dialogView.findViewById(R.id.btnChooseImage);
        MaterialButton saveBtn = dialogView.findViewById(R.id.btnAddItem);
        imagePreview = dialogView.findViewById(R.id.imagePreview);

        name.setText(product.getName());
        price.setText(product.getPrice());
        description.setText(product.getDescription());
        oldImageUrl = product.getImageUrl();
        selectedProductId = product.getProductId();
        selectedImageUri = null;

        if (oldImageUrl != null) {
            Glide.with(this).load(oldImageUrl).into(imagePreview);
        }

        chooseImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        AlertDialog dialog = builder.create();
        // Back arrow listener
        ImageView btnBack = dialogView.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> dialog.dismiss());

        saveBtn.setOnClickListener(v -> {
            String newName = name.getText().toString().trim();
            String newPrice = price.getText().toString().trim();
            String newDesc = description.getText().toString().trim();

            if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newPrice) || TextUtils.isEmpty(newDesc)) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            product.setName(newName);
            product.setPrice(newPrice);
            product.setDescription(newDesc);

            if (selectedImageUri != null) {
                MediaManager.get().upload(selectedImageUri)
                        .callback(new UploadCallback() {
                            @Override public void onStart(String requestId) {}
                            @Override public void onProgress(String requestId, long bytes, long totalBytes) {}

                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                String newImageUrl = resultData.get("secure_url").toString();
                                deleteOldImageFromCloudinary(oldImageUrl);
                                product.setImageUrl(newImageUrl);
                                updateProductInFirebase(product, dialog);
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                Toast.makeText(View_Products.this, "Image upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            }

                            @Override public void onReschedule(String requestId, ErrorInfo error) {}
                        }).dispatch();
            } else {
                updateProductInFirebase(product, dialog);
            }
        });

        dialog.show();
    }

    private void updateProductInFirebase(Product product, AlertDialog dialog) {
        Toast.makeText(View_Products.this, "Updating product...", Toast.LENGTH_SHORT).show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("products");
        ref.child(product.getProductId()).setValue(product)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(View_Products.this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    selectedImageUri = null;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(View_Products.this, "Failed to update product", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteProduct(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteOldImageFromCloudinary(product.getImageUrl()); // Delete image from Cloudinary first

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("products");
                    ref.child(product.getProductId()).removeValue()
                            .addOnSuccessListener(unused -> Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }



    private void deleteOldImageFromCloudinary(String oldImageUrl) {
        if (oldImageUrl == null || !oldImageUrl.contains("res.cloudinary.com")) return;

        // Extract publicId from the image URL
        String[] parts = oldImageUrl.split("/");
        String publicIdWithExtension = parts[parts.length - 1];
        String publicId = publicIdWithExtension.split("\\.")[0];

        // Run the Cloudinary deletion in a background thread
        new DeleteImageTask().execute(publicId);
    }

    private class DeleteImageTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String publicId = params[0];
            try {
                // Deleting image from Cloudinary in background
                MediaManager.get().getCloudinary().uploader().destroy(publicId, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Handle post-deletion actions (if any), like showing a Toast
            Toast.makeText(View_Products.this, "Old image deleted from Cloudinary", Toast.LENGTH_SHORT).show();
        }
    }


}
