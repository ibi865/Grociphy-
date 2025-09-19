package com.example.homescreen.model;

public class TopBrand {
    String name;
    String description;
    String loc;
    Integer imageUrl;

    public TopBrand(String name, String description, String loc, Integer imageUrl) {
        this.name = name;
        this.description = description;
        this.loc = loc;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLoc() {
        return loc;
    }

    public Integer getImageUrl() {
        return imageUrl;
    }
}