package com.example.trado;

public class Ad {
    private String id;
    private String title;
    private String desc;
    private String price;
    private String imageUrl;
    private String ownerId;
    private String category; // added field

    public Ad() {}

    public Ad(String title, String desc, String price, String imageUrl, String ownerId, String category) {
        this.title = title;
        this.desc = desc;
        this.price = price;
        this.imageUrl = imageUrl;
        this.ownerId = ownerId;
        this.category = category;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
