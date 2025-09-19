package com.example.homescreen.Admin_Panel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homescreen.R;
import com.example.homescreen.adapter_packages.OrderAdapter;
import com.example.homescreen.model.OrderModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



import java.util.ArrayList;
import java.util.List;

public class View_All_Orders extends AppCompatActivity {

    private RecyclerView orderRecyclerView;
    private ProgressBar progressBar;
    private OrderAdapter orderAdapter;
    private List<OrderModel> allOrders = new ArrayList<>();
    private List<OrderModel> filteredOrders = new ArrayList<>();
    private ImageView backarrow;
    private Spinner orderFilterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_all_orders);

        // Initialize views
        orderRecyclerView = findViewById(R.id.ordersRecyclerView);
        progressBar = findViewById(R.id.orders_progressBar);
        backarrow = findViewById(R.id.backArrow);
        orderFilterSpinner = findViewById(R.id.orderFilterSpinner);


        // Setup Spinner options with custom adapter
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_dropdown_item,  // Custom item layout
                new String[]{"ALL", "PENDING", "COMPLETED"});
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);  // Custom dropdown layout
        orderFilterSpinner.setAdapter(spinnerAdapter);

        // Setup RecyclerView
        orderAdapter = new OrderAdapter(this, filteredOrders, this::applyFilterAndUpdate);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderRecyclerView.setAdapter(orderAdapter);

        // Spinner item change listener
        orderFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilterAndUpdate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Load data from Firebase
        loadOrdersFromFirebase();

        // Back arrow navigation
        backarrow.setOnClickListener(v -> {
            startActivity(new Intent(View_All_Orders.this, Admin_Panel.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }

    private void loadOrdersFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allOrders.clear();
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    OrderModel order = orderSnap.getValue(OrderModel.class);
                    if (order != null) {
                        allOrders.add(order);
                    }
                }
                applyFilterAndUpdate(); // Filter and refresh view
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(View_All_Orders.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilterAndUpdate() {
        String selectedStatus = orderFilterSpinner.getSelectedItem().toString();
        filteredOrders.clear();

        if ("ALL".equalsIgnoreCase(selectedStatus)) {
            filteredOrders.addAll(allOrders);
        } else {
            for (OrderModel order : allOrders) {
                if (order.getStatus() != null &&
                        order.getStatus().equalsIgnoreCase(selectedStatus)) {
                    filteredOrders.add(order);
                }
            }
        }

        orderAdapter.notifyDataSetChanged(); // Safe to call now
    }
    public void refreshStats() {
        if (getParent() instanceof Admin_Panel) {
            ((Admin_Panel) getParent()).fetchOrderStats();
        }
    }

}
