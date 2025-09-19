package com.example.homescreen.model;

public class Recently_viewed {
    String name;
    String description;
    String price;
    String quantity;
    String unit;
    Integer imageUrl;

    Integer BigimgUrl;

    public Integer getBigimgUrl() {
        return BigimgUrl;
    }

    public Recently_viewed(String name, String description, String price, String quantity, String unit, Integer imageUrl, Integer BigimgUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.unit = unit;
        this.imageUrl = imageUrl;
        this.BigimgUrl=BigimgUrl;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public Integer getImageUrl() {
        return imageUrl;
    }
}
