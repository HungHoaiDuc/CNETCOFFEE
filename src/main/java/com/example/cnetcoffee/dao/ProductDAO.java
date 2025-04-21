package com.example.cnetcoffee.dao;

import java.sql.*;
import com.example.cnetcoffee.Model.Product;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProductDAO {
    private Connection conn;

    public ProductDAO() {
        this.conn = DBConnect.getConnection();
    }

    public ObservableList<Product> getAllProducts() {
        ObservableList<Product> products = FXCollections.observableArrayList();
        String query = "SELECT * FROM foods ";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                byte[] imageBytes = rs.getBytes("image");
                products.add(new Product(
                        rs.getInt("food_id"),
                        rs.getString("food_name"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getString("availability"),
                        imageBytes
                ));
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi lấy dữ liệu từ database: " + e.getMessage());
        }
        return products;
    }


    public boolean addProduct(Product product) {
        String query = "INSERT INTO foods (food_name, price, category, availability, image) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, product.getProductName());
            stmt.setDouble(2, product.getPrice());
            stmt.setString(3, product.getCategory());
            stmt.setString(4, product.getAvailability());

            // Kiểm tra nếu ảnh không null và lưu vào cơ sở dữ liệu
            if (product.getImageBytes() != null) {
                stmt.setBytes(5, product.getImageBytes());
            } else {
                stmt.setNull(5, Types.BLOB);  // Nếu không có ảnh, lưu NULL
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean updateProduct(Product product) {
        String query = "UPDATE foods SET food_name=?, price=?, category=?, availability=?, image=? WHERE food_id=?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, product.getProductName());
            stmt.setDouble(2, product.getPrice());
            stmt.setString(3, product.getCategory());
            stmt.setString(4, product.getAvailability());
            // Set ảnh dưới dạng byte[]
            if (product.getImageBytes() != null) {
                stmt.setBytes(5, product.getImageBytes());
            } else {
                stmt.setNull(5, Types.BLOB);
            }
            stmt.setInt(6, product.getProductId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean stopBusinessProduct(int productId) {
        String updateSQL = "UPDATE foods SET availability = 'Stop Doing Business' WHERE food_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setInt(1, productId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }




}