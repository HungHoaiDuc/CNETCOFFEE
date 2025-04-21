package com.example.cnetcoffee.dao;

import com.example.cnetcoffee.Model.CardProduct;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class CardProductDAO {
    private Connection conn;

    public CardProductDAO(Connection conn) {
        this.conn = conn;
    }


    public ObservableList<CardProduct> getAllProducts() throws SQLException {
        ObservableList<CardProduct> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM foods WHERE availability != 'Stop Doing Business' ORDER BY food_id ASC";

        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("food_id");
            String name = rs.getString("food_name");
            double price = rs.getDouble("price");
            byte[] image = rs.getBytes("image");

            System.out.println("🟢 " + name + " - Price: " + price + " - Img size: " + (image != null ? image.length : "null"));

            CardProduct product = new CardProduct(id, name, price, image);
            list.add(product);
        }

        return list;
    }

}
