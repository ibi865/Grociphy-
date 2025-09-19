package com.example.homescreen.adapter_packages;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.homescreen.CartDao;
import com.example.homescreen.R;
import com.example.homescreen.model.Cart_Model;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Cart_Model> cartItems;
    private final Context context;
    private final CartDao cartDao;

    public CartAdapter(Context context, CartDao cartDao) {
        this.context = context;
        this.cartDao = cartDao;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Cart_Model item = cartItems.get(position);

        holder.tvProductName.setText(item.getProductName());
        holder.tvProductPrice.setText(String.format("Rs. %.2f", item.getProductPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvTotalPrice.setText(String.format("Rs. %.2f", item.getTotalPrice()));

        // Load image based on productImageUrl or fallback to drawable resource
        if (item.getProductImageUrl() != null && !item.getProductImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getProductImageUrl()) // Firebase image URL
                    .placeholder(R.drawable.image_placeholder2)
                    .into(holder.imgProduct);
        } else if (item.getProductImageResId() != 0) {
            holder.imgProduct.setImageResource(item.getProductImageResId()); // Drawable resource ID
        }

        holder.btnIncrease.setOnClickListener(v -> updateQuantity(item, holder, item.getQuantity() + 1));
        holder.btnDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                updateQuantity(item, holder, item.getQuantity() - 1);
            }
        });
        holder.btnDelete.setOnClickListener(v -> deleteItem(item));
    }

    private void updateQuantity(Cart_Model item, CartViewHolder holder, int newQuantity) {
        new Thread(() -> {
            item.setQuantity(newQuantity);
            item.setTotalPrice(item.getProductPrice() * newQuantity);
            cartDao.update(item);
        }).start();
    }

    private void deleteItem(Cart_Model item) {
        new Thread(() -> cartDao.delete(item)).start();
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public void setCartItems(List<Cart_Model> cartItems) {
        this.cartItems = cartItems;
        notifyDataSetChanged();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvProductPrice, tvQuantity, tvTotalPrice;
        ImageButton btnIncrease, btnDecrease, btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
