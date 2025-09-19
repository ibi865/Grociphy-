package com.example.homescreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.homescreen.model.Cart_Model;
import com.example.homescreen.model.Product;
import com.example.homescreen.model.Wishlist;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class DetailActivity extends AppCompatActivity {

    private TextView productName, productDescription, productPrice;
    private ImageView productImage, backArrow, wishlistIcon;
    private Button addToCartButton;
    private String productId;
    private FirebaseDatabase database;
    private DatabaseReference productRef;
    private boolean isInWishlist = false;  // Track if the item is in wishlist
    private WishlistDao wishlistDao;  // Assuming you have a WishlistDao to interact with the database
    private CartDao cartDao;  // CartDao for managing the cart
    private Product currentProduct;  // To hold the product details fetched from Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Initialize views
        productName = findViewById(R.id.product_detail_name_new);
        productDescription = findViewById(R.id.product_detail_discription_new);
        productPrice = findViewById(R.id.product_detail_price_new);
        productImage = findViewById(R.id.product_detail_img_new);
        backArrow = findViewById(R.id.back_key_new);
        wishlistIcon = findViewById(R.id.wishlist_icon_new);
        addToCartButton = findViewById(R.id.add_to_cart_btn_new);

        // Get the product ID from Intent
        productId = getIntent().getStringExtra("productId");

        // Set Firebase reference
        database = FirebaseDatabase.getInstance();
        productRef = database.getReference("products").child(productId);

        // Initialize DAOs
        wishlistDao = AppDatabase.getDatabase(this).wishlistDao();
        cartDao = AppDatabase.getDatabase(this).cartDao();

        // Load product data
        fetchProductDetails();

        backArrow.setOnClickListener(v -> onBackPressed());

        // Handle Add to Cart button click
        addToCartButton.setOnClickListener(v -> addToCart());
    }

    private void fetchProductDetails() {
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentProduct = dataSnapshot.getValue(Product.class);

                    if (currentProduct != null) {
                        productName.setText(currentProduct.getName());
                        productDescription.setText(currentProduct.getDescription());
                        productPrice.setText("Rs. " + currentProduct.getPrice());

                        // Load image using Glide
                        Glide.with(DetailActivity.this)
                                .load(currentProduct.getImageUrl())
                                .placeholder(R.drawable.image_placeholder4)  // Placeholder
                                .into(productImage);

                        // Check if the product is already in the wishlist
                        checkIfInWishlist(currentProduct.getName(), currentProduct.getImageUrl());

                        // Toggle wishlist functionality
                        wishlistIcon.setOnClickListener(view -> {
                            toggleWishlist(currentProduct.getName(), currentProduct.getImageUrl(), currentProduct.getPrice());
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DetailActivity.this, "Failed to load product details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToCart() {
        if (currentProduct == null) {
            Toast.makeText(this, "Product details not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get product details for cart
        String name = currentProduct.getName();
        String description = currentProduct.getDescription();
        String priceStr = currentProduct.getPrice();

        double price = Double.parseDouble(priceStr);

        // Create a Cart_Model instance, passing null for productImageResId
        Cart_Model cartItem = new Cart_Model(name, currentProduct.getImageUrl(), null, price, price, 1); // Initial quantity is 1

        // Check if the product is already in the cart
        new Thread(() -> {
            Cart_Model existingItem = cartDao.getCartItemByName(name);
            if (existingItem != null) {
                // Update the quantity of the existing item
                existingItem.setQuantity(existingItem.getQuantity() + 1);
                existingItem.setTotalPrice(existingItem.getProductPrice() * existingItem.getQuantity());
                cartDao.update(existingItem);
                runOnUiThread(() -> Toast.makeText(DetailActivity.this, "Product quantity updated in cart", Toast.LENGTH_SHORT).show());
            } else {
                // Add the item to the cart
                cartDao.insert(cartItem);
                runOnUiThread(() -> Toast.makeText(DetailActivity.this, "Added to cart", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    private void toggleWishlist(String productName, String imageUrl, String priceStr) {
        new Thread(() -> {
            if (isInWishlist) {
                // Remove from wishlist
                Wishlist itemToRemove = wishlistDao.getWishlistItemByName(productName);
                if (itemToRemove != null) {
                    wishlistDao.delete(itemToRemove);
                    runOnUiThread(() -> {
                        isInWishlist = false;
                        updateWishlistIcon();
                        Toast.makeText(DetailActivity.this, "Removed from wishlist", Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                // Add to wishlist
                double itemPrice;
                try {
                    itemPrice = Double.parseDouble(priceStr.replaceAll("[^\\d.]", ""));
                } catch (NumberFormatException e) {
                    runOnUiThread(() -> Toast.makeText(DetailActivity.this, "Invalid price format", Toast.LENGTH_SHORT).show());
                    return;
                }

                Wishlist newItem = new Wishlist(productName, imageUrl, null, itemPrice);
                wishlistDao.insert(newItem);
                runOnUiThread(() -> {
                    isInWishlist = true;
                    updateWishlistIcon();
                    Toast.makeText(DetailActivity.this, "Added to wishlist", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateWishlistIcon() {
        if (isInWishlist) {
            wishlistIcon.setImageResource(R.drawable.heart_red); // Heart icon for added item
        } else {
            wishlistIcon.setImageResource(R.drawable.heart2); // Default heart icon
        }
    }

    private void checkIfInWishlist(String productName, String imageUrl) {
        new Thread(() -> {
            Wishlist wishlistItem = wishlistDao.getWishlistItemByName(productName);
            if (wishlistItem != null) {
                isInWishlist = true;
            }
            runOnUiThread(() -> updateWishlistIcon());
        }).start();
    }
}
