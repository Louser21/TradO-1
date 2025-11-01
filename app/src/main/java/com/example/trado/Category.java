package com.example.trado;

public class Category {
    private String name;
    private int imageResId;

    public Category(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public int getImageResId() { return imageResId; }
}

