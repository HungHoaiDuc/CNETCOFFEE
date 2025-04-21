package com.example.cnetcoffee.dao;

import com.example.cnetcoffee.Model.RevenueRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StatisticalRevenueDAO {

    public static List<RevenueRecord> getRevenueRecordsByType(LocalDate fromDate, LocalDate toDate, String type) {
        System.out.println("📅 Từ ngày: " + fromDate);
        System.out.println("📅 Đến ngày: " + toDate);
        System.out.println("🖥️ Loại máy: " + type);

        List<RevenueRecord> revenueList = new ArrayList<>();
        String sql = "SELECT s.session_id, c.name AS machine, s.start_time, s.end_time, c.type AS machine_type, s.total_cost " +
                "FROM sessions s " +
                "JOIN computers c ON s.computer_id = c.computer_id " +
                "WHERE 1=1 ";

        if (fromDate != null) {
            sql += " AND CAST(s.start_time AS DATE) >= ? ";
        }
        if (toDate != null) {
            sql += " AND CAST(s.end_time AS DATE) <= ? ";
        }
        if (!"Tất cả".equals(type)) {
            sql += " AND c.type = ? ";
        }

        sql += " ORDER BY s.start_time DESC";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;

            if (fromDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(fromDate));
            }
            if (toDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(toDate));
            }
            if (!"Tất cả".equals(type)) {
                stmt.setString(paramIndex++, type);
            }

            System.out.println("📝 SQL Query: " + stmt);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                RevenueRecord revenueRecord = new RevenueRecord(
                        rs.getInt("session_id"),
                        rs.getString("machine"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("machine_type"),
                        String.format("%,.0fđ", rs.getDouble("total_cost"))
                );
                revenueList.add(revenueRecord);
            }

            if (revenueList.isEmpty()) {
                System.out.println("⚠️ Không có dữ liệu nào được trả về!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Lỗi lấy dữ liệu doanh thu: " + e.getMessage());
        }

        return revenueList;
    }
}
