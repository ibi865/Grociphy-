package com.example.homescreen.Admin_Panel;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.homescreen.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Admin_Profile extends AppCompatActivity {

    private Button btnChangePassword;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private AlertDialog passwordChangeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("admin");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Remove default title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Set up back arrow
        ImageView backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
            }
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        dialogBuilder.setView(dialogView);

        final EditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        final EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        final EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);
        Button btnUpdate = dialogView.findViewById(R.id.btnSubmitPassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelPassword);

        // Create dialog without default buttons
        passwordChangeDialog = dialogBuilder.create();
        passwordChangeDialog.show();

        // Handle Update button click
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = etOldPassword.getText().toString().trim();
                String newPassword = etNewPassword.getText().toString().trim();
                String confirmPassword = etConfirmPassword.getText().toString().trim();

                if (TextUtils.isEmpty(oldPassword)) {
                    etOldPassword.setError("Enter current password");
                    return;
                }

                if (TextUtils.isEmpty(newPassword)) {
                    etNewPassword.setError("Enter new password");
                    return;
                }

                if (newPassword.length() < 6) {
                    etNewPassword.setError("Password too short (min 6 characters)");
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    etConfirmPassword.setError("Passwords don't match");
                    return;
                }

                // Show toast when update process starts
                Toast.makeText(Admin_Profile.this, "Updating password...", Toast.LENGTH_SHORT).show();
                changePassword(oldPassword, newPassword);
            }
        });

        // Handle Cancel button click
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Admin_Profile.this, "Password unchanged", Toast.LENGTH_SHORT).show();
                passwordChangeDialog.dismiss();
            }
        });
    }

    private void changePassword(final String oldPassword, final String newPassword) {
        final FirebaseUser user = mAuth.getCurrentUser();
        final String email = user.getEmail();

        // First re-authenticate the user
        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Authentication successful, now update password
                    user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Password updated in Authentication, now update in Realtime Database
                                updatePasswordInDatabase(newPassword);
                            } else {
                                Toast.makeText(Admin_Profile.this, "Failed to update password: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(Admin_Profile.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updatePasswordInDatabase(String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();

            // Directly reference the admin node since it's at root level
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Check if email matches (case-sensitive comparison)
                        if (dataSnapshot.child("email").getValue(String.class).equals(userEmail)) {
                            // Update the password directly under admin node
                            mDatabase.child("password").setValue(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(Admin_Profile.this,
                                                        "Password updated successfully",
                                                        Toast.LENGTH_SHORT).show();
                                                passwordChangeDialog.dismiss();
                                            } else {
                                                Toast.makeText(Admin_Profile.this,
                                                        "Database update failed: " + task.getException().getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(Admin_Profile.this,
                                    "Email doesn't match admin records",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Admin_Profile.this,
                                "Admin record not found",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(Admin_Profile.this,
                            "Database error: " + databaseError.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}