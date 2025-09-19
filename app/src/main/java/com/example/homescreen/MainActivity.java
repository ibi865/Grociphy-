package com.example.homescreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.homescreen.adapter_packages.Recently_viewed_adapter;
import com.example.homescreen.adapter_packages.SliderAdapter;
import com.example.homescreen.adapter_packages.TopBrandAdapter;
import com.example.homescreen.adapter_packages.category_adapter;
import com.example.homescreen.adapter_packages.discounted_adapter;
import com.example.homescreen.model.Category;
import com.example.homescreen.model.Discounted_products;
import com.example.homescreen.model.Recently_viewed;
import com.example.homescreen.model.TopBrand;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView menuIcon, all_category_img,cartIcon,wishlistIcon;
    private RecyclerView discount_recyclerview, catetory_recyclerview, recently_viwed_recyclerview,topBrandsRecyclerView;
    private discounted_adapter d_adapter;
    private ArrayList<Discounted_products> d_products;
    private category_adapter c_adapter;
    private ArrayList<Category> c_products;
    private ArrayList<Recently_viewed> r_products;
    private Recently_viewed_adapter r_adapter;

    private NavigationView navigationView;

    // Top Brands Adapter
    private TopBrandAdapter topBrandAdapter;
    private ArrayList<TopBrand> topBrandList;

    // Slider variables
    private ViewPager2 viewPager2;
    private Handler sliderHandler = new Handler();
    private static final long SLIDER_DELAY_MS = 3000; // 3 seconds
    private Runnable sliderRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu);
        discount_recyclerview = findViewById(R.id.discounted_recyclerview);
        catetory_recyclerview = findViewById(R.id.category_recyclerview);
        recently_viwed_recyclerview = findViewById(R.id.recenetly_viewed);
        topBrandsRecyclerView = findViewById(R.id.top_brands_recyclerview);
        all_category_img = findViewById(R.id.all_category_img);
        cartIcon = findViewById(R.id.cart);
        wishlistIcon=findViewById(R.id.setting);
        navigationView = findViewById(R.id.navigation_view);
        MenuItem authItem = navigationView.getMenu().findItem(R.id.nav_logout); // Make sure your menu item ID matches

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            authItem.setTitle("Login");
        } else {
            authItem.setTitle("Logout");
        }

        viewPager2 = findViewById(R.id.imageSlider);

        // Fetch and show user name
        fetchAndDisplayUserName();


        // Set up Image Slider
        setupImageSlider();

        // Set RecyclerView Layout Manager
        discount_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        catetory_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recently_viwed_recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        topBrandsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Initialize Product List
        d_products = new ArrayList<>();
        d_products.add(new Discounted_products(R.drawable.discountberry, "Discount Berry"));
        d_products.add(new Discounted_products(R.drawable.discountbrocoli, "Discount Apple"));
        d_products.add(new Discounted_products(R.drawable.discountmeat, "Discount Banana"));
        d_products.add(new Discounted_products(R.drawable.discountberry, "Discount Berry"));
        d_products.add(new Discounted_products(R.drawable.discountbrocoli, "Discount Apple"));
        d_products.add(new Discounted_products(R.drawable.discountmeat, "Discount Banana"));

        c_products = new ArrayList<>();
        c_products.add(new Category(R.drawable.ic_home_fish, "Home Fish"));
        c_products.add(new Category(R.drawable.ic_home_fruits, "Home Fruits"));
        c_products.add(new Category(R.drawable.ic_home_meats, "Home Meat"));
        c_products.add(new Category(R.drawable.ic_home_veggies, "Home Veggies"));
        c_products.add(new Category(R.drawable.ic_home_fish, "Home Fish"));
        c_products.add(new Category(R.drawable.ic_home_fruits, "Home Fruits"));
        c_products.add(new Category(R.drawable.ic_home_meats, "Home Meat"));
        c_products.add(new Category(R.drawable.ic_home_veggies, "Home Veggies"));

        r_products = new ArrayList<>();
        r_products.add(new Recently_viewed("Watermelon",
                "Watermelon has high water content and also provide fiber",
                "Rs 80", "1", "Kg", R.drawable.card4, R.drawable.b4));
        r_products.add(new Recently_viewed("Papaya",
                "Papaya is rich in vitamins and contains enzymes that aid digestion.",
                "Rs 250", "1", "Kg", R.drawable.card3, R.drawable.b3));
        r_products.add(new Recently_viewed("Strawberry",
                "Strawberries are rich in antioxidants and vitamin C, making them a delicious and nutritious fruit.",
                "Rs 90", "1", "Kg", R.drawable.card2, R.drawable.b1));
        r_products.add(new Recently_viewed("Kiwi",
                "Kiwi is packed with vitamin C and antioxidants, helping to support immune health and improve skin radiance.",
                "Rs 110", "1", "PC", R.drawable.card1, R.drawable.b2));

        topBrandList = new ArrayList<>();
        topBrandList.add(new TopBrand("FreshMart", "Your go-to place for organic produce and daily essentials.", "Lahore - Shalimar Link Road", R.drawable.brand1_image));
        topBrandList.add(new TopBrand("GroceryHub", "Serving quality grocery products with quick delivery.", "Karachi - Gulshan-e-Iqbal", R.drawable.brand2_image));
        topBrandList.add(new TopBrand("DailyNeeds", "Affordable groceries and home items all under one roof.", "Islamabad - Blue Area", R.drawable.brand3_image));
        topBrandList.add(new TopBrand("QuickPick", "Fast, fresh, and reliable grocery service at your doorstep.", "Faisalabad - Jaranwala Road", R.drawable.brand4_image));


        // Initialize Adapter with Correct Data
        d_adapter = new discounted_adapter(this, d_products);
        discount_recyclerview.setAdapter(d_adapter);

        c_adapter = new category_adapter(this, c_products);
        catetory_recyclerview.setAdapter(c_adapter);

        r_adapter = new Recently_viewed_adapter(this, r_products);
        recently_viwed_recyclerview.setAdapter(r_adapter);

        // Initialize TopBrandAdapter and set it to RecyclerView
        topBrandAdapter = new TopBrandAdapter(this, topBrandList);
        topBrandsRecyclerView.setAdapter(topBrandAdapter);

        // Menu Click Listener
        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
                drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
            } else {
                drawerLayout.openDrawer(androidx.core.view.GravityCompat.START);
            }
        });

        all_category_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, All_category.class);
                startActivity(intent);
            }
        });

        cartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create intent to open Cart Activity
                Intent cartIntent = new Intent(MainActivity.this, Cart_Activity.class);
                startActivity(cartIntent);

                // Optional: Add a smooth transition animation
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        wishlistIcon.setOnClickListener(v -> {
            Intent wishlistIntent = new Intent(MainActivity.this, Wishlist_Activity.class);
            startActivity(wishlistIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // Optional: Smooth transition
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Already in home, just close drawer
            } else if (id == R.id.nav_categories) {
                startActivity(new Intent(this, All_category.class));
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, Cart_Activity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else if (id == R.id.nav_orders) {
                // Open Wishlist Activity
                startActivity(new Intent(this, Wishlist_Activity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else if (id == R.id.nav_support) {
                // Handle support
                startActivity(new Intent(this, Customer_support.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else if (id == R.id.nav_about) {
                // Handle about us
                startActivity(new Intent(this, SplashScreen.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }else if (id == R.id.nav_logout) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser == null) {
                    // User is already signed out
                    Toast.makeText(this, "You are not logged in. Redirecting to login page.", Toast.LENGTH_SHORT).show();
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Logout")
                            .setMessage("Are you sure you want to logout?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Proper Google Sign-Out
                                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken(getString(R.string.client_id)) // Add this in strings.xml
                                        .requestEmail()
                                        .build();

                                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

                                googleSignInClient.signOut().addOnCompleteListener(task -> {
                                    FirebaseAuth.getInstance().signOut();
                                    Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();

                                });
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .show();
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void fetchAndDisplayUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(uid);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.hasChild("name")) {
                        String name = snapshot.child("name").getValue(String.class);
                        View headerView = navigationView.getHeaderView(0);
                        TextView usernameText = headerView.findViewById(R.id.user_name);
                        if (usernameText != null) {
                            usernameText.setText("Hi " + name);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("MainActivity", "Failed to load user name", error.toException());
                }
            });
        }
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


    private void setupImageSlider() {
        List<Integer> sliderImages = new ArrayList<>();
        sliderImages.add(R.drawable.slide101);
        sliderImages.add(R.drawable.slide6);
        sliderImages.add(R.drawable.slide12);

        // Set up adapter
        viewPager2.setAdapter(new SliderAdapter(sliderImages));

        // Auto-scroll for slider
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = viewPager2.getCurrentItem();
                int nextItem = (currentItem + 1) % sliderImages.size();
                viewPager2.setCurrentItem(nextItem, true);
                sliderHandler.postDelayed(this, SLIDER_DELAY_MS);
            }
        };

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Reset the handler when user manually changes page
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY_MS);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start auto-scrolling when activity resumes
        if (viewPager2.getAdapter() != null) {
            sliderHandler.postDelayed(sliderRunnable, SLIDER_DELAY_MS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto-scrolling when activity is paused
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up to prevent memory leaks
        if (sliderHandler != null) {
            sliderHandler.removeCallbacksAndMessages(null);
        }
    }
}