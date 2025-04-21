package com.example.cnetcoffee.dao;

import com.example.cnetcoffee.Model.RevenueData;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RevenueDAO {

    public List<RevenueData> getRevenueData(LocalDate startDate,
                                            LocalDate endDate) {
        List<RevenueData> revenueList = new ArrayList<>();
        String sql = "SELECT " +
                "    CAST(s.start_time AS DATE) AS revenue_date, " +
                "    SUM(CASE WHEN f.category IN ('FOOD', 'DRINK') THEN od.subtotal ELSE 0 END) AS total_service_revenue, " +
                "    SUM(CASE WHEN c.type = 'NORMAL' THEN s.total_cost ELSE 0 " +
                "END) AS total_normal_pc_revenue, " +
                "    SUM(CASE WHEN c.type = 'VIP' THEN s.total_cost ELSE 0 END) " +
                "AS total_vip_pc_revenue " +
                "FROM sessions s " +
                "    LEFT JOIN orders o ON s.session_id = o.session_id " +
                "    LEFT JOIN order_details od ON o.order_id = od.order_id " +
                "    LEFT JOIN foods f ON od.food_id = f.food_id " +
                "    INNER JOIN computers c ON s.computer_id = c.computer_id " +
                "WHERE CAST(s.start_time AS DATE) BETWEEN ? AND ? " +
                "GROUP BY CAST(s.start_time AS DATE) " +
                "ORDER BY revenue_date;";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LocalDate date = rs.getDate("revenue_date").toLocalDate();
                // Sửa tên cột ở đây
                double serviceRevenue = rs.getDouble("total_service_revenue");
                double pcNormalRevenue = rs.getDouble("total_normal_pc_revenue");
                double pcVipRevenue = rs.getDouble("total_vip_pc_revenue");

                RevenueData revenueData =
                        new RevenueData(date, serviceRevenue, pcNormalRevenue, pcVipRevenue);
                revenueList.add(revenueData);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle errors appropriately (e.g., log them, show a message to the
            // user)
        }

        return revenueList;
    }

}