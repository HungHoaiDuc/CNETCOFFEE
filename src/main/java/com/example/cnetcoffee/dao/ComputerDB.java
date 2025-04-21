package com.example.cnetcoffee.dao;

import com.example.cnetcoffee.Model.Computer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ComputerDB {
    // Lấy danh sách tất cả máy tính từ database
    public static List<Computer> getAllComputers() {
        List<Computer> computers = new ArrayList<>();
        String query = "SELECT * FROM computers";

        try (Connection conn = DBConnect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int id = rs.getInt("computer_id");
                String name = rs.getString("name");
                String status = rs.getString("status");
                String type = rs.getString("type");
                boolean isAvailable = rs.getBoolean("isAvailable");
                String socketPort = rs.getString("socket_port");

                computers.add(new Computer(id, name, status, type, isAvailable, socketPort));
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi lấy danh sách máy tính: " + e.getMessage());
        }

        return computers;
    }

    // Thêm một máy mới vào database
    public static int addComputer(Computer computer) {
        String query = "INSERT INTO computers (name, status, isAvailable, type, socket_port) OUTPUT INSERTED.computer_id VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, computer.getName());
            stmt.setString(2, computer.getStatus());
            stmt.setBoolean(3, computer.isAvailable());
            stmt.setString(4, computer.getType());
            stmt.setString(5, computer.getSocketPort());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi thêm máy: " + e.getMessage());
        }
        return -1;
    }

    public static int getNextComputerNumber(String type) {
        List<Computer> all = getAllComputers();
        int max = 0;
        String prefix = type.equalsIgnoreCase("VIP") ? "VIP " : "Máy ";

        for (Computer c : all) {
            if (c.getName().startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(c.getName().substring(prefix.length()));
                    if (num > max) {
                        max = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        return max + 1;
    }

    public static void updateComputerStatus(int computerId, String newStatus, boolean isAvailable) {
        String query = "UPDATE computers SET status = ?, isAvailable = ? WHERE computer_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newStatus);
            stmt.setBoolean(2, isAvailable);
            stmt.setInt(3, computerId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("🔄 Cập nhật trạng thái máy ID " + computerId + " thành " + newStatus);
            } else {
                System.out.println("⚠️ Không tìm thấy máy có ID " + computerId);
            }

        } catch (SQLException e) {
            System.out.println("❌ Lỗi cập nhật trạng thái máy: " + e.getMessage());
        }
    }

    public static int getComputerIdByIp(String ipAddress) {
        String sql = "SELECT computer_id FROM computers WHERE socket_port = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ipAddress);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("computer_id");
            }

        } catch (SQLException e) {
            System.out.println("❌ Lỗi truy vấn máy theo IP: " + e.getMessage());
        }
        return -1; // Không tìm thấy
    }

    public static void deleteComputer(int id) {
        String sql = "DELETE FROM computers WHERE computer_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("🗑 Máy tính có ID " + id + " đã bị xóa khỏi database!");

        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi xóa máy: " + e.getMessage());
        }
    }

    public static int getAvailableComputerNumber() {
        String sql = "SELECT name FROM computers WHERE name LIKE 'Máy %'";
        List<Integer> numbers = new ArrayList<>();

        try (Connection conn = DBConnect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("name");
                try {
                    int number = Integer.parseInt(name.replace("Máy ", "").trim());
                    numbers.add(number);
                } catch (NumberFormatException ignored) {}
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi lấy danh sách máy: " + e.getMessage());
        }

        int missingNumber = 1;
        for (int num : numbers) {
            if (num == missingNumber) {
                missingNumber++;
            } else {
                break;
            }
        }
        return missingNumber;
    }

    public static String getStatusById(int computerId) {
        String query = "SELECT status FROM computers WHERE computer_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, computerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("status");
            }
        } catch (SQLException e) {
            System.out.println("❌ Lỗi lấy trạng thái máy: " + e.getMessage());
        }
        return null;
    }

    public static String getComputerType(int computerId) {
        String sql = "SELECT type FROM computers WHERE computer_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, computerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("type");
            }
        } catch (Exception e) {
            System.out.println("❌ Lỗi lấy type máy: " + e.getMessage());
        }
        return "NORMAL";
    }
}
