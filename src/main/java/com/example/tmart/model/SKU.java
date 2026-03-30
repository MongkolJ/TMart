package com.example.tmart.model;

import jakarta.persistence.*;

import java.util.Base64;

@Entity
@Table(name = "sku")
public class SKU {
    @Id
    private String id;

    @Column(name = "title")
    private String title;

    @Column(name = "price")
    private double price;

    @Column(name = "description")
    private String description;

    @Column(name = "category_id")
    private String categoryId;

    @Lob
    @Column(name = "image", columnDefinition = "LONGBLOB")
    private byte[] image;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getImageAsBase64() {
        if (image == null) return "";
        String encoded = Base64.getEncoder().encodeToString(image);
        return "data:image/jpeg;base64," + encoded;
    }
}
