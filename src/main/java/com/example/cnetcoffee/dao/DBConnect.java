package com.example.cnetcoffee.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnect {
    // Cấu hình kết nối tới SQL Server (máy chủ tự cài)
    private static final String HOST = "172.168.10.165";
    private static final String PORT = "1433";
    private static final String DATABASE = "NetCafeDB";
    private static final String USER = "hung283";
    private static final String PASSWORD = "123456";

    private static final String URL = String.format(
            "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=false;trustServerCertificate=true",
            HOST, PORT, DATABASE
    );

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("❌ Lỗi kết nối SQL Server: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        Connection conn = getConnection();
        if (conn != null) {
            System.out.println("✅ Đã kết nối thành công tới SQL Server!");
        } else {
            System.out.println("❌ Kết nối thất bại.");
        }
    }
}
