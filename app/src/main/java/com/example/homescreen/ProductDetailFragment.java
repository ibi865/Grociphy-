package com.example.homescreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.homescreen.AppDatabase;
import com.example.homescreen.CartDao;
import com.example.homescreen.WishlistDao;
import com.example.homescreen.model.Cart_Model;
import com.example.homescreen.model.Wishlist;

public class ProductDetailFragment extends Fragment {

    private TextView productName, description, price;
    private ImageView productImage, backArrow, wishlistIcon;
    private Button addToCartBtn;
    private CartDao cartDao;
    private WishlistDao wishlistDao;
    private boolean isInWishlist = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_detail, container, false);

        // Initialize databases
        cartDao = AppDatabase.getDatabase(requireContext()).cartDao();
        wishlistDao = AppDatabase.getDatabase(requireContext()).wishlistDao();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        productName = view.findViewById(R.id.product_detail_name);
        description = view.findViewById(R.id.product_detail_discription);
        price = view.findViewById(R.id.product_detail_price);
        productImage = view.findViewById(R.id.product_detail_img);
        backArrow = view.findViewById(R.id.back_key);
        addToCartBtn = view.findViewById(R.id.add_to_cart_btn);
        wishlistIcon = view.findViewById(R.id.wishlist_icon);

        // Apply Data from Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            String name = bundle.getString("name", "No Name");
            String desc = bundle.getString("description", "No Description Available");
            String priceStr = bundle.getString("price", "N/A");
            int imageResId = bundle.getInt("imageUrl", 0);

            productName.setText(name);
            description.setText(desc);
            price.setText(priceStr);

            if (imageResId != 0) {
                productImage.setImageResource(imageResId);
            }

            // Check if item is already in wishlist
            new Thread(() -> {
                Wishlist existingItem = wishlistDao.getWishlistItemByName(name);
                requireActivity().runOnUiThread(() -> {
                    isInWishlist = existingItem != null;
                    updateWishlistIcon();
                });
            }).start();

            // Handle Wishlist Icon Click
            wishlistIcon.setOnClickListener(v -> toggleWishlist(name, imageResId, priceStr));

            // Handle Add to Cart Button Click
            addToCartBtn.setOnClickListener(v -> {
                String productNameStr = productName.getText().toString();
                String priceText = price.getText().toString().replaceAll("[^\\d.]", "");
                double itemPrice;
                try {
                    itemPrice = Double.parseDouble(priceText);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid price format", Toast.LENGTH_SHORT).show();
                    return;
                }
                addToCartBtn.setEnabled(false);
                new Thread(() -> {
                    Cart_Model existingItem = cartDao.getCartItemByName(name);

                    if (existingItem != null) {
                        int newQuantity = existingItem.getQuantity() + 1;
                        existingItem.setQuantity(newQuantity);
                        existingItem.setTotalPrice(existingItem.getProductPrice() * newQuantity);
                        cartDao.update(existingItem);

                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Item quantity updated in cart", Toast.LENGTH_SHORT).show()
                        );
                    } else {
                        Cart_Model newItem = new Cart_Model(name,null, imageResId, itemPrice, itemPrice, 1);
                        cartDao.insert(newItem);

                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Item added to cart", Toast.LENGTH_SHORT).show();
                            addToCartBtn.setEnabled(true); // Re-enable
                        });
                    }
                }).start();
            });
        }

        // Handle Back Arrow Click
        backArrow.setOnClickListener(v -> {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                View fragmentContainer = activity.findViewById(R.id.product_fragment_container);
                View mainLayout = activity.findViewById(R.id.main);
                View bottomSection = activity.findViewById(R.id.bottom_section);

                if (fragmentContainer != null) {
                    fragmentContainer.setVisibility(View.GONE);
                    mainLayout.setVisibility(View.VISIBLE);
                    bottomSection.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void toggleWishlist(String productName, int imageResId, String priceStr) {
        new Thread(() -> {
            if (isInWishlist) {
                // Remove from wishlist
                Wishlist itemToRemove = wishlistDao.getWishlistItemByName(productName);
                if (itemToRemove != null) {
                    wishlistDao.delete(itemToRemove);
                    requireActivity().runOnUiThread(() -> {
                        isInWishlist = false;
                        updateWishlistIcon();
                        Toast.makeText(getContext(), "Removed from wishlist", Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                // Add to wishlist
                double itemPrice;
                try {
                    itemPrice = Double.parseDouble(priceStr.replaceAll("[^\\d.]", ""));
                } catch (NumberFormatException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Invalid price format", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                Wishlist newItem = new Wishlist(productName, null, imageResId, itemPrice);
                wishlistDao.insert(newItem);
                requireActivity().runOnUiThread(() -> {
                    isInWishlist = true;
                    updateWishlistIcon();
                    Toast.makeText(getContext(), "Added to wishlist", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateWishlistIcon() {
        if (isInWishlist) {
            wishlistIcon.setImageResource(R.drawable.heart_red); // Make sure you have this drawable
        } else {
            wishlistIcon.setImageResource(R.drawable.heart2); // Your default black heart
        }
    }
}