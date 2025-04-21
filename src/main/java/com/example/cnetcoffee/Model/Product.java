package com.example.cnetcoffee.Model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;



public class Product {
    private int productId;
    private String productName;
    private double price;
    private String category;
    private String availability;
    private byte[] imageBytes;
    // Constructor
    public Product(int productId, String productName, double price, String category, String availability, byte[] imageBytes) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.category = category;
        this.availability = availability;
        this.imageBytes = imageBytes;
    }

    // Getters & Setters
    public int getProductId() {
        return productId;
    }
    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getAvailability() {
        return availability;
    }
    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    public ImageView getImageView() {
        if (imageBytes != null && imageBytes.length > 0) {
            Image image = new Image(new ByteArrayInputStream(imageBytes));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            return imageView;
        }
        return new ImageView(); // Nếu không có ảnh
    }
}
