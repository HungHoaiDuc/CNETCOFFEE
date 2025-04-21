package com.example.cnetcoffee.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PriceDAO {
    public static int getPricePerHour(String type) {
        String sql = "SELECT price_per_hour FROM prices WHERE type = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("price_per_hour");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Giá mặc định nếu không tìm thấy
        return "VIP".equalsIgnoreCase(type) ? 7000 : 5000;
    }

    public static void updatePrice(String type, int pricePerHour) {
        String sql = "UPDATE prices SET price_per_hour = ? WHERE type = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pricePerHour);
            stmt.setString(2, type);
            stmt.executeUpdate();
            System.out.println("✅ Đã cập nhật giá tiền cho loại máy: " + type);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
