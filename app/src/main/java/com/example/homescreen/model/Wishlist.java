package com.example.homescreen.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wishlist_items")
public class Wishlist {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String productName;
    private String productImageUrl; // Firebase image URL
    private Integer productImageResId; // Drawable resource ID (nullable)
    private double productPrice;

    public Wishlist(String productName, String productImageUrl, Integer productImageResId, double productPrice) {
        this.productName = productName;
        this.productImageUrl = productImageUrl;
        this.productImageResId = productImageResId;
        this.productPrice = productPrice;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductImageUrl() { return productImageUrl; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }

    public Integer getProductImageResId() { return productImageResId; }
    public void setProductImageResId(Integer productImageResId) { this.productImageResId = productImageResId; }

    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }
}
