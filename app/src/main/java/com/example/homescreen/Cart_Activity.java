package com.example.homescreen;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.homescreen.adapter_packages.CartAdapter;
import com.example.homescreen.model.Cart_Model;
import com.example.homescreen.model.OrderModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class Cart_Activity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private static final int LOGIN_REQUEST_CODE = 1001;
    private CartAdapter adapter;
    private CartDao cartDao;
    private TextView tvSubtotal, tvDelivery, tvTax, tvTotal;
    private ImageView btn_back;

    private LottieAnimationView orderSuccessAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize views
        recyclerView = findViewById(R.id.cart_recyclerview);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDelivery = findViewById(R.id.tv_delivery);
        tvTax = findViewById(R.id.tv_tax);
        tvTotal = findViewById(R.id.tv_total);
        Button btnOrderNow = findViewById(R.id.btn_order_now);
        orderSuccessAnimation = findViewById(R.id.order_success_animation);
        btn_back=findViewById(R.id.btn_back);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize database
        cartDao = AppDatabase.getDatabase(this).cartDao();

        // Initialize adapter
        adapter = new CartAdapter(this, cartDao);
        recyclerView.setAdapter(adapter);

        // Observe cart items
        cartDao.getAllItems().observe(this, cartItems -> {
            adapter.setCartItems(cartItems);
            calculateOrderSummary(cartItems);
        });

        // Order Now button
        btnOrderNow.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser == null) {
                // User is not signed in
                Toast.makeText(Cart_Activity.this, "Please sign in to place your order.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Cart_Activity.this, LoginActivity.class);
                intent.putExtra("from_cart", true);
                startActivityForResult(intent, LOGIN_REQUEST_CODE);
            } else {
                // User is signed in, proceed with placing order
                placeOrder();  // 👈 replace confirmOrder() with this
            }
        });



        // Back button - Updated implementation
        btn_back.setOnClickListener(v -> {
            Intent intent = new Intent(Cart_Activity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });


    }

    private void calculateOrderSummary(List<Cart_Model> cartItems) {
        double subtotal = 0;
        for (Cart_Model item : cartItems) {
            subtotal += item.getTotalPrice();
        }

        // Check if the cart is empty
        double delivery = (cartItems.isEmpty()) ? 0.0 : 10.0; // Set delivery to 0 if cart is empty
        double tax = subtotal * 0.02; // 2% tax

        tvSubtotal.setText(String.format("Rs. %.2f", subtotal));
        tvDelivery.setText(String.format("Rs. %.2f", delivery));
        tvTax.setText(String.format("Rs. %.2f", tax));
        tvTotal.setText(String.format("Rs. %.2f", subtotal + delivery + tax));
    }



    private void placeOrder() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // User is not signed in
            Toast.makeText(this, "Please sign in to place your order.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Cart_Activity.this, LoginActivity.class);
            intent.putExtra("from_cart", true); // Tell LoginActivity user is coming from Cart
            startActivityForResult(intent, LOGIN_REQUEST_CODE);
        } else {
            // User is signed in, proceed with placing order
            confirmOrder();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            // User has logged in successfully
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
            placeOrder(); // 👈 Call here after successful login
        }
    }




    private void confirmOrder() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            runOnUiThread(() -> Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show());
            return;
        }

        new Thread(() -> {
            List<Cart_Model> cartItems = cartDao.getCartItemsOnce(); // Custom method to fetch list synchronously
            if (cartItems == null || cartItems.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show());
                return;
            }

            double totalPrice = 0;
            for (Cart_Model item : cartItems) {
                totalPrice += item.getTotalPrice();
            }

            String userId = currentUser.getUid();
            long timestamp = System.currentTimeMillis();
            String status = "Pending";

            OrderModel order = new OrderModel(userId, totalPrice, cartItems, status, timestamp);

            // Upload to Firebase
            FirebaseDatabase.getInstance().getReference("orders")
                    .push()
                    .setValue(order)
                    .addOnSuccessListener(aVoid -> {
                        runOnUiThread(() -> {
                            orderSuccessAnimation.setVisibility(View.VISIBLE);
                            orderSuccessAnimation.playAnimation();

                            // After animation completes, clear cart and finish
                            orderSuccessAnimation.addAnimatorListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {}

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    new Thread(() -> {
                                        cartDao.clearCart();
                                        runOnUiThread(() -> {
                                            finish();
                                        });
                                    }).start();
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {}

                                @Override
                                public void onAnimationRepeat(Animator animation) {}
                            });
                        });
                    })
                    .addOnFailureListener(e -> runOnUiThread(() ->
                            Toast.makeText(this, "Failed to place order: " + e.getMessage(), Toast.LENGTH_SHORT).show()));
        }).start();
    }



}