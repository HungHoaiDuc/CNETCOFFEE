package com.example.cnetcoffee.dao;

import com.example.cnetcoffee.Model.DailyRevenue;
import com.example.cnetcoffee.Model.ProductRevenue;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.image.Image;

public class ProductRevenueDAO {

    public List<ProductRevenue> getProductRevenue(
            LocalDate startDate, LocalDate endDate, String searchText) {
        List<ProductRevenue> productRevenueList = new ArrayList<>();
        String sql =
                "SELECT f.food_name, f.image, f.price, SUM(od.quantity) AS quantity_ordered, SUM(od.subtotal) AS total_revenue "
                        + "FROM foods f "
                        + "JOIN order_details od ON f.food_id = od.food_id "
                        + "JOIN orders o ON od.order_id = o.order_id "
                        + "WHERE o.order_time BETWEEN ? AND ? "
                        + "AND f.food_name LIKE ? "
                        + "GROUP BY f.food_id, f.food_name, f.image, f.price "
                        + "ORDER BY total_revenue DESC";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            pstmt.setTimestamp(2, Timestamp.valueOf(endDate.atStartOfDay().plusDays(1)));
            pstmt.setString(3, "%" + searchText + "%");

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String foodName = rs.getString("food_name");
                InputStream imageStream = rs.getBinaryStream("image");
                Image image = null;
                if (imageStream != null) {
                    image = new Image(imageStream);
                    try {
                        imageStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                double price = rs.getDouble("price");
                int quantityOrdered = rs.getInt("quantity_ordered");
                double totalRevenue = rs.getDouble("total_revenue");

                ProductRevenue productRevenue =
                        new ProductRevenue(foodName, image, price, quantityOrdered, totalRevenue);
                productRevenueList.add(productRevenue);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productRevenueList;
    }

    public double getTotalServiceRevenue(
            LocalDate startDate, LocalDate endDate, String searchText) {
        double totalRevenue = 0.0;
        String sql =
                "SELECT SUM(od.subtotal) AS total_revenue "
                        + "FROM orders o "
                        + "JOIN order_details od ON o.order_id = od.order_id "
                        + "JOIN foods f ON od.food_id = f.food_id "
                        + "WHERE o.order_time BETWEEN ? AND ? "
                        + "AND f.food_name LIKE ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            pstmt.setTimestamp(2, Timestamp.valueOf(endDate.atStartOfDay().plusDays(1)));
            pstmt.setString(3, "%" + searchText + "%");

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                totalRevenue = rs.getDouble("total_revenue");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalRevenue;
    }

    public List<DailyRevenue> getDailyRevenue(
            LocalDate startDate, LocalDate endDate, String searchText) {
        List<DailyRevenue> dailyRevenueList = new ArrayList<>();
        String sql =
                "SELECT CONVERT(DATE, o.order_time) AS order_date, SUM(od.subtotal) AS daily_revenue "
                        + "FROM orders o "
                        + "JOIN order_details od ON o.order_id = od.order_id "
                        + "JOIN foods f ON od.food_id = f.food_id "
                        + "WHERE o.order_time BETWEEN ? AND ? "
                        + "AND f.food_name LIKE ? "
                        + "GROUP BY CONVERT(DATE, o.order_time) "
                        + "ORDER BY CONVERT(DATE, o.order_time)";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay()));
            pstmt.setTimestamp(2, Timestamp.valueOf(endDate.atStartOfDay().plusDays(1)));
            pstmt.setString(3, "%" + searchText + "%");

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LocalDate date = rs.getDate("order_date").toLocalDate();
                double dailyRevenue = rs.getDouble("daily_revenue");
                DailyRevenue dailyRevenueObj = new DailyRevenue(date, dailyRevenue);
                dailyRevenueList.add(dailyRevenueObj);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dailyRevenueList;
    }

}
