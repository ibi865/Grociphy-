package com.example.homescreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import com.airbnb.lottie.LottieAnimationView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



import com.example.homescreen.Admin_Panel.AdminLoginActivity;

public class SplashScreen extends AppCompatActivity {

    private LottieAnimationView animationView;
    private static final int SPLASH_TIME = 3000;
    private final Handler handler = new Handler();

    // Secret gesture tracking
    private int tapCount = 0;
    private long lastTapTime = 0;
    private static final int REQUIRED_TAPS = 3;
    private static final int TAP_RESET_DELAY = 1000;
    private boolean isAdminTriggered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        animationView = findViewById(R.id.animationView);
        animationView.setVisibility(View.VISIBLE);

        View splashImage = findViewById(R.id.splashpic);

        // System insets handling
        ViewCompat.setOnApplyWindowInsetsListener(splashImage, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupSecretGestures(splashImage);
        startSplashTimer();
    }

    private void setupSecretGestures(View splashImage) {
        // Triple Tap detection
        splashImage.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastTapTime > TAP_RESET_DELAY) {
                tapCount = 1;
            } else {
                tapCount++;
            }

            lastTapTime = currentTime;

            if (tapCount >= REQUIRED_TAPS) {
                handleAdminAccess();
            }
        });

        // Long Press detection
        splashImage.setOnLongClickListener(v -> {
            handleAdminAccess();
            return true;
        });
    }

    private void startSplashTimer() {
        handler.postDelayed(() -> {
            if (!isAdminTriggered) {
                navigateToMain();
            }
        }, SPLASH_TIME);
    }

    private void handleAdminAccess() {
        isAdminTriggered = true;
        animationView.setVisibility(View.GONE);
        handler.removeCallbacksAndMessages(null); // Cancel any pending navigation
        navigateToAdminLogin();
    }

    private void navigateToMain() {
        startActivity(new Intent(SplashScreen.this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void navigateToAdminLogin() {
        startActivity(new Intent(SplashScreen.this, AdminLoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null); // Prevent memory leaks
    }


}