package com.example.homescreen.adapter_packages;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.homescreen.R;
import com.example.homescreen.WishlistDao;
import com.example.homescreen.model.Wishlist;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    private List<Wishlist> wishlistItems;
    private final Context context;
    private final WishlistDao wishlistDao;

    public WishlistAdapter(Context context, WishlistDao wishlistDao) {
        this.context = context;
        this.wishlistDao = wishlistDao;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        Wishlist item = wishlistItems.get(position);

        holder.tvProductName.setText(item.getProductName());
        holder.tvProductPrice.setText(String.format("Rs. %.2f", item.getProductPrice()));

        // Use Glide to load image URL or fallback to drawable resource ID
        if (item.getProductImageUrl() != null && !item.getProductImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getProductImageUrl()) // Firebase image URL
                    .placeholder(R.drawable.image_placeholder2)
                    .into(holder.imgProduct);
        } else if (item.getProductImageResId() != null) {
            holder.imgProduct.setImageResource(item.getProductImageResId()); // Drawable resource ID
        }

        holder.btnDelete.setOnClickListener(v -> deleteItem(item));
    }

    private void deleteItem(Wishlist item) {
        new Thread(() -> wishlistDao.delete(item)).start();
    }

    @Override
    public int getItemCount() {
        return wishlistItems != null ? wishlistItems.size() : 0;
    }

    public void setWishlistItems(List<Wishlist> wishlistItems) {
        this.wishlistItems = wishlistItems;
        notifyDataSetChanged();
    }

    static class WishlistViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvProductPrice;
        ImageButton btnDelete;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
