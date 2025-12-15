package com.example.mobileapp.model;

import java.io.Serializable;

public class Product implements Serializable {
    public String id;
    public String category; // "top", "bottom", "shoes"
    public Integer imageRes; // resource id (nullable)
    public String imagePath; // asset or file path (nullable)
    public String title;
    public String shopLink;
    public String description;

    // Constructor for drawable resource
    public Product(String id, String category, int imageRes, String title, String shopLink, String description) {
        this.id = id;
        this.category = category;
        this.imageRes = imageRes;
        this.imagePath = null;
        this.title = title;
        this.shopLink = shopLink;
        this.description = description;
    }

    // Constructor for asset/file path
    public Product(String id, String category, String imagePath, String title, String shopLink, String description) {
        this.id = id;
        this.category = category;
        this.imageRes = null;
        this.imagePath = imagePath;
        this.title = title;
        this.shopLink = shopLink;
        this.description = description;
    }
}
