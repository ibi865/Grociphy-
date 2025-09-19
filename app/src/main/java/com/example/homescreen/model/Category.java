package com.example.homescreen.model;

public class Category {

    private int id;
    private String imageurl;

    public Category(int id, String imageurl) {
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


