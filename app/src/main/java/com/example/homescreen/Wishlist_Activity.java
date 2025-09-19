package com.example.homescreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homescreen.adapter_packages.WishlistAdapter;
import com.example.homescreen.model.Wishlist;

import java.util.List;

public class Wishlist_Activity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WishlistAdapter wishlistAdapter;
    private ImageButton backButton;
    private TextView itemCountTextView;
    private Button removeAllButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wishlist);

        backButton = findViewById(R.id.btn_back);
        itemCountTextView = findViewById(R.id.item_count_text);
        removeAllButton = findViewById(R.id.btn_remove_all);
        recyclerView = findViewById(R.id.wishlist_recyclerview);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        wishlistAdapter = new WishlistAdapter(this, AppDatabase.getDatabase(this).wishlistDao());
        recyclerView.setAdapter(wishlistAdapter);

        // Back button functionality
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(Wishlist_Activity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Remove all button functionality
        removeAllButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Wishlist")
                    .setMessage("Are you sure you want to remove all items from your wishlist?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        new Thread(() -> {
                            AppDatabase.getDatabase(this).wishlistDao().clearWishlist();
                        }).start();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // LiveData observation for wishlist updates
        AppDatabase.getDatabase(this).wishlistDao().getAllItems().observe(this, new Observer<List<Wishlist>>() {
            @Override
            public void onChanged(List<Wishlist> wishlist) {
                int itemCount = wishlist != null ? wishlist.size() : 0;
                itemCountTextView.setText("Items: " + itemCount);
                wishlistAdapter.setWishlistItems(wishlist);
            }
        });
    }
}
