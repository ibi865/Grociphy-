package com.example.homescreen.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cart_items")
public class Cart_Model {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String productName;
    private String productImageUrl; // Firebase image URL
    private Integer productImageResId; // Drawable resource ID (nullable)
    private double productPrice;
    private double totalPrice;
    private int quantity;


    public Cart_Model() {
        // Firebase needs this constructor
    }
    // Single constructor for both image sources
    public Cart_Model(String productName, String productImageUrl, Integer productImageResId, double productPrice, double totalPrice, int quantity) {
        this.productName = productName;
        this.productImageUrl = productImageUrl;
        this.productImageResId = productImageResId;
        this.productPrice = productPrice;
        this.totalPrice = totalPrice;
        this.quantity = quantity;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public Integer getProductImageResId() {
        return productImageResId;
    }

    public void setProductImageResId(Integer productImageResId) {
        this.productImageResId = productImageResId;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
