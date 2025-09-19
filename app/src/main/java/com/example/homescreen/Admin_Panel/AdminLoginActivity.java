package com.example.homescreen.Admin_Panel;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.homescreen.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminLoginActivity extends AppCompatActivity {

    private TextInputEditText etAdminEmail, etAdminPassword;
    private com.airbnb.lottie.LottieAnimationView loadingAnimation, welcomeAnimation;
    private DatabaseReference adminRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        adminRef = FirebaseDatabase.getInstance().getReference("admin");

        // Initialize views
        etAdminEmail = findViewById(R.id.admin_email);
        etAdminPassword = findViewById(R.id.admin_password);
        loadingAnimation = findViewById(R.id.admin_loading);
        welcomeAnimation = findViewById(R.id.welcome_animation);

        findViewById(R.id.admin_login_btn).setOnClickListener(v -> validateAdmin());
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


    private void validateAdmin() {
        String email = etAdminEmail.getText().toString().trim();
        String password = etAdminPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Stop welcome animation and show loading
        welcomeAnimation.cancelAnimation();
        welcomeAnimation.setVisibility(View.GONE);
        showLoading(true);

        // Check against stored admin credentials
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String storedEmail = snapshot.child("email").getValue(String.class);
                String storedPassword = snapshot.child("password").getValue(String.class);

                if (storedEmail == null || storedPassword == null) {
                    showLoading(false);
                    Toast.makeText(AdminLoginActivity.this,
                            "Admin credentials not configured", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (email.equals(storedEmail) && password.equals(storedPassword)) {
                    authenticateWithFirebase(email, password);
                } else {
                    showLoading(false);
                    Toast.makeText(AdminLoginActivity.this,
                            "Invalid admin credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(AdminLoginActivity.this,
                        "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void authenticateWithFirebase(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        redirectToAdminPanel();
                    } else {
                        welcomeAnimation.setVisibility(View.VISIBLE);
                        welcomeAnimation.playAnimation();
                        showLoading(false);
                        Toast.makeText(AdminLoginActivity.this,
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void redirectToAdminPanel() {
        startActivity(new Intent(this, Admin_Panel.class));
        finish();
    }

    private void showLoading(boolean show) {
        loadingAnimation.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            loadingAnimation.playAnimation();
        } else {
            loadingAnimation.cancelAnimation();
        }
    }

    @Override
    protected void onStart() {
        welcomeAnimation.setVisibility(View.VISIBLE);
        welcomeAnimation.playAnimation();
        super.onStart();
        // Force logout when accessing admin login
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }
}