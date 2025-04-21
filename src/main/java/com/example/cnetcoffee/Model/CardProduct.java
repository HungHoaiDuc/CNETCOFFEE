package com.example.cnetcoffee.Model;

public class CardProduct {
    private int id;
    private String name;
    private double price;
    private byte[] image;

    public CardProduct(int id, String name, double price, byte[] image) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
    }

    public int getId() {
        return id; }
    public void setId(int id) {
        this.id = id; }
    public String getName() {
        return name; }
    public void setName(String name) {
        this.name = name; }
    public double getPrice() {
        return price; }
    public void setPrice(double price) {
        this.price = price; }
    public byte[] getImage() {
        return image; }
    public void setImage(byte[] image) {
        this.image = image; }

}
