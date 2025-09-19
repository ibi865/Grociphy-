package com.example.homescreen.model;

public class Discounted_products {
    private int id;
    private String imageurl;

    public Discounted_products(int id, String imageurl) {
        this.id = id;
        this.imageurl = imageurl;
    }

    public int getId() {
        return id;
    }

    public String getImageurl() {
        return imageurl;
    }
}

