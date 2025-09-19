package com.example.homescreen.model;

import java.util.ArrayList;
import java.util.List;

public class OrderModel {

    private String userId;
    private double totalPrice;
    private List<Cart_Model> products;
    private String status;
    private long timestamp;


    public OrderModel() {
        products = new ArrayList<>(); // Double initialization for Firebase
    }
    // Constructor
    public OrderModel(String userId, double totalPrice, List<Cart_Model> products, String status, long timestamp) {
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.products = products;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<Cart_Model> getProducts() {
        return products;
    }

    public void setProducts(List<Cart_Model> products) {
        this.products = products;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    // In OrderModel.java
    public String getProductListString() {
        if (products == null || products.isEmpty()) {
            return "No products";
        }

        StringBuilder sb = new StringBuilder();
        for (Cart_Model product : products) {
            if (product != null) {
                sb.append(product.getProductName())
                        .append(" (Qty: ")
                        .append(product.getQuantity())
                        .append("), ");
            }
        }

        // Remove the trailing comma and space if there are any products
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }

        return sb.toString();
    }

}
