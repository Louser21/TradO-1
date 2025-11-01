package com.example.trado;

public class Ad {
    private String title;
    private String desc;
    private String price;
    private String imageUrl;
    private String ownerId;

    private String id;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }


    public Ad() {}

    public Ad(String title, String desc, String price, String imageUrl, String ownerId) {
        this.title = title;
        this.desc = desc;
        this.price = price;
        this.imageUrl = imageUrl;
        this.ownerId = ownerId;
    }

    public String getTitle() { return title; }
    public String getDesc() { return desc; }
    public String getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getOwnerId() { return ownerId; }

    public void setTitle(String title) { this.title = title; }
    public void setDesc(String desc) { this.desc = desc; }
    public void setPrice(String price) { this.price = price; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
}
