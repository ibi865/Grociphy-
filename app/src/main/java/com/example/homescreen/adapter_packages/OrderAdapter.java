package com.example.homescreen.adapter_packages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homescreen.Admin_Panel.View_All_Orders;
import com.example.homescreen.R;
import com.example.homescreen.model.Cart_Model;
import com.example.homescreen.model.OrderModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderStatusChangedListener {
        void onOrderStatusChanged();
    }


    private final Context context;
    private final List<OrderModel> orderList;

    private final OnOrderStatusChangedListener statusChangedListener;

    public OrderAdapter(Context context, List<OrderModel> orderList, OnOrderStatusChangedListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.statusChangedListener = listener;
    }


    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, @SuppressLint("RecyclerView") int position) {
        OrderModel order = orderList.get(position);

        // Set User ID and Total Price
        String userId = order.getUserId();
        String formattedUserId = userId.substring(0, 4) + "..." + userId.substring(userId.length() - 4);
        holder.tvUserId.setText("User ID: " + formattedUserId);
        holder.tvTotalPrice.setText(String.format("Rs. %.2f", order.getTotalPrice()));


        StringBuilder productNames = new StringBuilder();
        List<Cart_Model> products = order.getProducts();
        if (products != null && !products.isEmpty()) {
            for (int i = 0; i < products.size(); i++) {
                Cart_Model product = products.get(i);
                if (product != null && product.getProductName() != null) {
                    productNames.append(product.getProductName());
                    if (i < products.size() - 1) {
                        productNames.append(", ");
                    }
                }
            }
        } else {
            productNames.append("No products");
        }
        holder.tvProductList.setText(productNames.toString());

        // Set Timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy - h:mm a", Locale.getDefault());
        String formattedDate = sdf.format(new Date(order.getTimestamp()));
        holder.tvTimestamp.setText(formattedDate);

        // Set Status
        holder.tvStatus.setText(order.getStatus());

        // Show/Hide Confirm Button based on status
        if ("COMPLETED".equalsIgnoreCase(order.getStatus())) {
            holder.btnConfirmOrder.setVisibility(View.GONE);
        } else {
            holder.btnConfirmOrder.setVisibility(View.VISIBLE);
            holder.btnConfirmOrder.setText("Confirm Order");
            holder.btnConfirmOrder.setEnabled(true);
        }

        // Confirm button logic
        // In your OrderAdapter's onBindViewHolder:
        holder.btnConfirmOrder.setOnClickListener(v -> {
            // 1. First get a reference to the orders node
            DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

            // 2. Find the exact order to update by comparing all fields
            ordersRef.orderByChild("timestamp").equalTo(order.getTimestamp()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        OrderModel dbOrder = orderSnapshot.getValue(OrderModel.class);
                        if (dbOrder != null && dbOrder.getUserId().equals(order.getUserId())) {
                            // 3. Update only the status of the matched order
                            orderSnapshot.getRef().child("status").setValue("COMPLETED")
                                    .addOnSuccessListener(aVoid -> {
                                        order.setStatus("COMPLETED");
                                        notifyItemChanged(position);
                                        if (statusChangedListener != null) {
                                            statusChangedListener.onOrderStatusChanged();
                                        }
                                        if (context instanceof View_All_Orders) {
                                            ((View_All_Orders) context).refreshStats();
                                        }
                                    });
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Error finding order", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }


    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserId, tvTotalPrice, tvProductList, tvStatus, tvTimestamp;
        Button btnConfirmOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserId = itemView.findViewById(R.id.tv_userId);
            tvTotalPrice = itemView.findViewById(R.id.tv_totalPrice);
            tvProductList = itemView.findViewById(R.id.tv_productList);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            btnConfirmOrder = itemView.findViewById(R.id.btn_confirmOrder);
        }
    }
}
