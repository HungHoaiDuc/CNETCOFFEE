package com.example.cnetcoffee.Model;

import javafx.beans.property.*;

public class MenuOrderItem {
    private final IntegerProperty quantity = new SimpleIntegerProperty(0);
    private final DoubleProperty price = new SimpleDoubleProperty(0.0);
    private final StringProperty productName = new SimpleStringProperty("");
    private final int foodId;

    public MenuOrderItem(int foodId, String productName, int quantity, double price) {
        this.foodId = foodId;
        this.productName.set(productName);
        this.quantity.set(quantity);
        this.price.set(price);
    }

    public int getQuantity() {
        return quantity.get();
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public double getPrice() {
        return price.get();
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public String getProductName() {
        return productName.get();
    }

    public StringProperty productNameProperty() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName.set(productName);
    }

    public int getFoodId() {
        return foodId;
    }



    public double getSubtotal() {
        return price.get() * quantity.get();
    }
}
