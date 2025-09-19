package com.example.homescreen;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    EditText email, password;
    Button login_btn;
    TextView signup_text;
    ImageView googleLoginBtn, phoneLoginBtn;
    LottieAnimationView loadingAnimation;
    private LottieAnimationView welcomeAnimation;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))  // make sure client_id is set in strings.xml
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // UI references
        email = findViewById(R.id.loginemail);
        password = findViewById(R.id.loginpass);
        login_btn = findViewById(R.id.loginbtn);
        signup_text = findViewById(R.id.l_signup);
        loadingAnimation = findViewById(R.id.loading_animation);
        googleLoginBtn = findViewById(R.id.imaggoogle);
        phoneLoginBtn = findViewById(R.id.imagphone);
        welcomeAnimation = findViewById(R.id.welcome_animation);

        login_btn.setOnClickListener(v -> performLogin());
        signup_text.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
        });

        googleLoginBtn.setOnClickListener(v -> signInWithGoogle());
        phoneLoginBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Phone login not implemented", Toast.LENGTH_SHORT).show();
        });
    }

    private void performLogin() {
        String emailTxt = email.getText().toString().trim();
        String passwordTxt = password.getText().toString().trim();

        if (TextUtils.isEmpty(emailTxt)) {
            email.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(passwordTxt)) {
            password.setError("Password is required");
            return;
        }

        welcomeAnimation.cancelAnimation();
        welcomeAnimation.setVisibility(View.GONE);

        login_btn.setVisibility(View.GONE);
        loadingAnimation.setVisibility(View.VISIBLE);
        loadingAnimation.playAnimation();

        mAuth.signInWithEmailAndPassword(emailTxt, passwordTxt)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        handleLoginSuccess();
                    } else {
                        handleLoginFailure(task.getException());
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        welcomeAnimation.cancelAnimation();
        welcomeAnimation.setVisibility(View.GONE);

        login_btn.setVisibility(View.GONE);
        loadingAnimation.setVisibility(View.VISIBLE);
        loadingAnimation.playAnimation();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserDataToDatabase(user);
                        handleLoginSuccess();
                    } else {
                        handleLoginFailure(task.getException());
                    }
                });
    }

    private void saveUserDataToDatabase(FirebaseUser user) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("id", user.getUid());
        userData.put("name", user.getDisplayName());
        userData.put("email", user.getEmail());
        if (user.getPhotoUrl() != null) {
            userData.put("profile", user.getPhotoUrl().toString());
        }

        database.getReference().child("users").child(user.getUid()).setValue(userData)
                .addOnFailureListener(e -> Log.w(TAG, "Failed to save user data", e));
    }

    private void handleLoginSuccess() {
        boolean fromCart = getIntent().getBooleanExtra("from_cart", false);
        if (fromCart) {
            setResult(RESULT_OK);
            finish();
        } else {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void handleLoginFailure(Exception exception) {
        welcomeAnimation.setVisibility(View.VISIBLE);
        welcomeAnimation.playAnimation();

        login_btn.setVisibility(View.VISIBLE);
        loadingAnimation.setVisibility(View.GONE);
        loadingAnimation.cancelAnimation();

        Log.w(TAG, "Login failed", exception);
        String errorMessage = exception != null ? exception.getMessage() : "Unknown error occurred";
        Toast.makeText(LoginActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        welcomeAnimation.setVisibility(View.VISIBLE);
        welcomeAnimation.playAnimation();
        if (isTaskRoot()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
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
}
