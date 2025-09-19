package com.example.homescreen.Admin_Panel;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.homescreen.R;
import com.example.homescreen.SplashScreen;
import com.example.homescreen.model.OrderModel;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.os.Environment;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Admin_Panel extends AppCompatActivity {


    private DrawerLayout drawerLayout;
    private TextView tvPendingOrders, tvCompletedOrders, tvTotalEarnings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        // Initialize views
        TextView tvTime = findViewById(R.id.tvTime);
        TextView tvDate = findViewById(R.id.tvDate);
        ImageView ivMenu = findViewById(R.id.ivMenu);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ImageView ivLogout = findViewById(R.id.ivLogout);
        ImageView ivNotification = findViewById(R.id.ivNotification);
        TextView totalUsersTextView = findViewById(R.id.total_users);
        countTotalUsers(totalUsersTextView);

        //Initialize stats card views
        tvPendingOrders = findViewById(R.id.tv_pending_orders_count);
        tvCompletedOrders = findViewById(R.id.tv_completed_orders_count);
        tvTotalEarnings = findViewById(R.id.tv_total_earnings);

        // Set current time and date
        setCurrentTimeAndDate(tvTime, tvDate);

        // calling stats method
        fetchOrderStats();

        // Menu icon click listener
        ivMenu.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent;

            if (id == R.id.nav_home) {
                // Handle home navigation
                drawerLayout.closeDrawer(GravityCompat.START);
            } else if (id == R.id.nav_settings) {
                intent = new Intent(Admin_Panel.this, SplashScreen.class);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
            } else if (id == R.id.nav_profile) {
                // Profile navigation intent
                intent = new Intent(Admin_Panel.this, Admin_Profile.class);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
            } else if (id == R.id.nav_report) {
                // Handle PDF report generation
                generatePdfReport();
                drawerLayout.closeDrawer(GravityCompat.START);
            } else if (id == R.id.nav_logout) {
                showLogoutConfirmation();
            }
            return true;
        });

        // Logout icon click listener
        ivLogout.setOnClickListener(v -> showLogoutConfirmation());

        // Notification icon click listener
        ivNotification.setOnClickListener(v -> openNotifications());

        // Card click listeners
        findViewById(R.id.cardAddProduct).setOnClickListener(v -> openAddProduct());
        findViewById(R.id.cardViewProducts).setOnClickListener(v -> openViewProducts());
        findViewById(R.id.cardViewOrders).setOnClickListener(v -> openViewOrders());
        findViewById(R.id.cardCustomerSupport).setOnClickListener(v -> openCustomerSupport());
    }

    private void setCurrentTimeAndDate(TextView timeView, TextView dateView) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        timeView.setText(timeFormat.format(new Date()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMMM", Locale.getDefault());
        dateView.setText(dateFormat.format(new Date()));
    }

    private void showLogoutConfirmation() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "You are not logged in.", Toast.LENGTH_SHORT).show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, AdminLoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    private void openNotifications() {
        startActivity(new Intent(this, SplashScreen.class));
    }

    private void openAddProduct() {
        startActivity(new Intent(this, Add_Product.class));
    }

    private void openViewProducts() {
        startActivity(new Intent(this, View_Products.class));
    }

    private void openViewOrders() {
        startActivity(new Intent(this, View_All_Orders.class));
    }

    private void openCustomerSupport() {
        startActivity(new Intent(this, Admin_Profile.class));
    }

    public void fetchOrderStats() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int pendingCount = 0;
                int completedCount = 0;
                double totalEarnings = 0.0;

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    OrderModel order = orderSnapshot.getValue(OrderModel.class);
                    if (order != null) {
                        String status = order.getStatus();
                        Double totalPrice = order.getTotalPrice();

                        if ("COMPLETED".equalsIgnoreCase(status)) {
                            completedCount++;
                            totalEarnings += totalPrice != null ? totalPrice : 0.0;
                        } else if ("PENDING".equalsIgnoreCase(status)) {
                            pendingCount++;
                        }
                    }
                }

                // Update UI
                tvPendingOrders.setText(String.valueOf(pendingCount));
                tvCompletedOrders.setText(String.valueOf(completedCount));
                tvTotalEarnings.setText(String.format("Rs. %.2f", totalEarnings));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Admin_Panel.this, "Failed to load stats", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void countTotalUsers(TextView totalUsersTextView) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalUsers = (int) snapshot.getChildrenCount();
                totalUsersTextView.setText(String.valueOf(totalUsers));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                totalUsersTextView.setText("Error");
                Toast.makeText(Admin_Panel.this, "Failed to load user count", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void generatePdfReport() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<OrderModel> orders = new ArrayList<>();
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    OrderModel order = orderSnap.getValue(OrderModel.class);
                    if (order != null) {
                        orders.add(order);
                    }
                }

                if (!orders.isEmpty()) {
                    createPdfFromOrders(orders);
                } else {
                    Toast.makeText(Admin_Panel.this, "No orders found to generate report.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Admin_Panel.this, "Failed to fetch orders.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void createPdfFromOrders(List<OrderModel> orders) {
        try {
            PdfDocument pdfDocument = new PdfDocument();
            Paint paint = new Paint();
            Paint titlePaint = new Paint();
            Paint headerPaint = new Paint();
            Paint summaryPaint = new Paint();

            // Setup paints
            titlePaint.setTextSize(20f);
            titlePaint.setFakeBoldText(true);
            titlePaint.setTextAlign(Paint.Align.CENTER);

            headerPaint.setTextSize(12f);
            headerPaint.setFakeBoldText(true);

            paint.setTextSize(10f);
            summaryPaint.setTextSize(12f);
            summaryPaint.setFakeBoldText(true);

            int pageWidth = 595, pageHeight = 842; // A4 size in points (210x297mm)
            int margin = 40;
            int y = 70; // Initial y position
            int lineSpacing = 15;
            int col1Width = 80;  // Order ID column
            int col2Width = 280; // Items column
            int col3Width = 80;  // Total column
            int col4Width = 80;  // Status column

            // Create first page
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            // Report title
            canvas.drawText("Order Report", pageWidth / 2, y, titlePaint);
            y += 25;

            // Report date
            paint.setTextSize(11f);
            canvas.drawText("Generated on: " + new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()),
                    margin, y, paint);
            y += 30;

            // Draw table header
            paint.setFakeBoldText(true);
            canvas.drawText("Order ID", margin, y, paint);
            canvas.drawText("Items", margin + col1Width, y, paint);
            canvas.drawText("Total", margin + col1Width + col2Width, y, paint);
            canvas.drawText("Status", margin + col1Width + col2Width + col3Width, y, paint);
            paint.setFakeBoldText(false);

            // Draw header underline
            y += 5;
            canvas.drawLine(margin, y, pageWidth - margin, y, paint);
            y += 15;

            // Statistics
            int completed = 0, pending = 0;
            double totalIncome = 0;

            for (OrderModel order : orders) {
                if (y > pageHeight - 100) { // Leave space for footer
                    pdfDocument.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.getPages().size() + 1).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                }

                // Update statistics
                if ("COMPLETED".equalsIgnoreCase(order.getStatus())) {
                    completed++;
                    totalIncome += order.getTotalPrice();
                } else if ("PENDING".equalsIgnoreCase(order.getStatus())) {
                    pending++;
                }

                // Draw order ID (shortened)
                String orderId = order.getUserId().length() > 8 ? order.getUserId().substring(0, 8) : order.getUserId();
                canvas.drawText(orderId, margin, y, paint);

                // Draw items with proper line wrapping
                String itemsText = order.getProductListString();
                String[] itemLines = splitTextIntoLines(itemsText, col2Width, paint);

                // Draw first line of items
                canvas.drawText(itemLines[0], margin + col1Width, y, paint);

                // Draw total and status on first line
                canvas.drawText("Rs. " + String.format("%.2f", order.getTotalPrice()),
                        margin + col1Width + col2Width, y, paint);
                canvas.drawText(order.getStatus().toLowerCase(),
                        margin + col1Width + col2Width + col3Width, y, paint);

                // Draw additional item lines if needed
                if (itemLines.length > 1) {
                    for (int i = 1; i < itemLines.length; i++) {
                        y += lineSpacing;
                        if (y > pageHeight - 100) {
                            pdfDocument.finishPage(page);
                            pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.getPages().size() + 1).create();
                            page = pdfDocument.startPage(pageInfo);
                            canvas = page.getCanvas();
                            y = 50;
                            // Redraw the order ID on new page for continuity
                            canvas.drawText(orderId, margin, y, paint);
                        }
                        canvas.drawText(itemLines[i], margin + col1Width, y, paint);
                    }
                }

                y += lineSpacing;

                // Draw light separator line
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(0.5f);
                canvas.drawLine(margin, y, pageWidth - margin, y, paint);
                paint.setStyle(Paint.Style.FILL);

                y += 10;
            }

            // Summary Section
            y += 20;
            canvas.drawLine(margin, y, pageWidth - margin, y, paint);
            y += 20;

            canvas.drawText("SUMMARY", margin, y, summaryPaint);
            y += 20;
            canvas.drawText("Total Orders: " + orders.size(), margin, y, paint);
            y += 15;
            canvas.drawText("Completed: " + completed, margin, y, paint);
            y += 15;
            canvas.drawText("Pending: " + pending, margin, y, paint);
            y += 15;
            canvas.drawText("Total Income: Rs. " + String.format("%.2f", totalIncome), margin, y, paint);

            pdfDocument.finishPage(page);

            // Save to Downloads
            File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "Order_Report_" + System.currentTimeMillis() + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                pdfDocument.writeTo(fos);
                pdfDocument.close();

                new AlertDialog.Builder(Admin_Panel.this)
                        .setTitle("Report Generated")
                        .setMessage("PDF report saved to Downloads folder!")
                        .setPositiveButton("Open", (dialog, which) -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(FileProvider.getUriForFile(Admin_Panel.this,
                                            getApplicationContext().getPackageName() + ".provider", pdfFile),
                                    "application/pdf");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        })
                        .setNegativeButton("OK", null)
                        .show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to split long text into multiple lines
    private String[] splitTextIntoLines(String text, int maxWidth, Paint paint) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (paint.measureText(currentLine + " " + word) < maxWidth) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines.toArray(new String[0]);
    }




}