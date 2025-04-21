package com.example.cnetcoffee.Model;

import javafx.scene.image.Image;

public class ProductRevenue {
    private String nameProduct;
    private Image imageProduct;
    private double price;
    private int quantityOrdered;
    private double totalProductRevenue;

    public ProductRevenue(String nameProduct, Image imageProduct, double price, int quantityOrdered, double totalProductRevenue) {
        this.nameProduct = nameProduct;
        this.imageProduct = imageProduct;
        this.price = price;
        this.quantityOrdered = quantityOrdered;
        this.totalProductRevenue = totalProductRevenue;
    }

    public String getNameProduct() {
        return nameProduct;
    }

    public void setNameProduct(String nameProduct) {
        this.nameProduct = nameProduct;
    }

    public Image getImageProduct() {
        return imageProduct;
    }

    public void setImageProduct(Image imageProduct) {
        this.imageProduct = imageProduct;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(int quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public double getTotalProductRevenue() {
        return totalProductRevenue;
    }

    public void setTotalProductRevenue(double totalProductRevenue) {
        this.totalProductRevenue = totalProductRevenue;
    }
}
