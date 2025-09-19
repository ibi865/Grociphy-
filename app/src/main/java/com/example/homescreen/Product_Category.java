package com.example.homescreen;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.homescreen.adapter_packages.ProductAdapter;
import com.example.homescreen.model.Product;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class Product_Category extends AppCompatActivity {

    private RecyclerView productRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private ImageView btnBack;
    private EditText searchEditText;
    private ImageView cart;
    private LottieAnimationView noResultsAnimation;
    private TextView noResultsText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_category);

        // Initialize RecyclerView
        btnBack = findViewById(R.id.btnBack);
        cart=findViewById(R.id.cart);
        noResultsAnimation = findViewById(R.id.no_results_animation);
        noResultsText = findViewById(R.id.no_results_text);
        searchEditText = findViewById(R.id.searchEditText);
        productRecyclerView = findViewById(R.id.categoriesRecyclerView);
        productRecyclerView.setHasFixedSize(true);


        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Product_Category.this, Cart_Activity.class);
                startActivity(intent);
                finish(); // Optional: finish current activity
            }
        });

        // Set GridLayoutManager for 2 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        productRecyclerView.setLayoutManager(gridLayoutManager);

        // Add equal spacing between items
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        productRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // not needed
            }
        });


        // Initialize product list
        productList = new ArrayList<>();

        // Set up Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference productsRef = database.getReference("products");

        // Fetch products from Firebase
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }

                productAdapter = new ProductAdapter(Product_Category.this, productList);
                productRecyclerView.setAdapter(productAdapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(Product_Category.this, "Failed to load products.", Toast.LENGTH_SHORT).show();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to MainActivity
                Intent intent = new Intent(Product_Category.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Optional: finish current activity
            }
        });

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

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();

        for (Product product : productList) {
            if (product.getName() != null && product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }

        if (productAdapter != null) {
            productAdapter.setFilteredList(filteredList);
            if (filteredList.isEmpty() && !query.isEmpty()) {
                noResultsAnimation.setVisibility(View.VISIBLE);
                noResultsText.setVisibility(View.VISIBLE);
                noResultsAnimation.playAnimation();
            } else {
                noResultsAnimation.setVisibility(View.GONE);
                noResultsText.setVisibility(View.GONE);
                noResultsAnimation.cancelAnimation();
            }
        }
    }



    // Inner class for item spacing
    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacing;
        private final boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {
                // Distribute spacing more evenly
                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }


}
