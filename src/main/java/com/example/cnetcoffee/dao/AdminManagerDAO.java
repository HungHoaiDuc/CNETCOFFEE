package com.example.cnetcoffee.dao;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.example.cnetcoffee.Model.User;

import java.math.BigDecimal;
import java.sql.*;

public class AdminManagerDAO {

    private Connection connectDB() throws SQLException {
        return DBConnect.getConnection();
    }

    public ObservableList<User> getAllUsers() {
        ObservableList<User> userList = FXCollections.observableArrayList();
        // Sử dụng NOT IN để lọc các giá trị role không phải 'ADMIN' và 'USER'
        String query = "SELECT user_id, username, balance, role, created_at, status FROM users WHERE role NOT IN ('ADMIN', 'USER')";

        try (Connection conn = connectDB();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                userList.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getBigDecimal("balance"),
                        rs.getString("role"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    public ObservableList<User> searchUsers(String keyword) {
        ObservableList<User> userList = FXCollections.observableArrayList();
        String query = "SELECT user_id, username, balance, role, created_at FROM users " +
                "WHERE username LIKE ?";

        try (Connection conn = connectDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + keyword + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                userList.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getBigDecimal("balance"),
                        rs.getString("role"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    public boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createUser(String username, String password, String role) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("ERROR: Username cannot be NULL or empty!");
            return;
        }

        if (isUsernameExists(username)) {
            System.out.println("ERROR: Username already exists!");
            return;
        }

        String sql = "INSERT INTO users (username, password, role, balance) VALUES (?, ?, ?, 0)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.executeUpdate();
            System.out.println("User created: " + username);
        } catch (SQLException e) {
            System.out.println("Error inserting user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void updateUserStatus(int userId, String newStatus) {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("❌ Lỗi cập nhật trạng thái người dùng: " + e.getMessage());
        }
    }

    public static void resetAllUserStatus() {
        String sql = "UPDATE users SET status = 'inactive'";
        try (Connection conn = DBConnect.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("❌ Lỗi reset trạng thái người dùng: " + e.getMessage());
        }
    }

    public void depositMoney(int userId, BigDecimal amount) {
        String updateBalanceQuery = "UPDATE users SET balance = balance + ? WHERE user_id = ?";
        String insertDepositQuery = "INSERT INTO deposits (user_id, amount, deposit_time) VALUES (?, ?, GETDATE())";

        try (Connection conn = connectDB()) {
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1️⃣ Cập nhật số dư
            try (PreparedStatement updateStmt = conn.prepareStatement(updateBalanceQuery)) {
                updateStmt.setBigDecimal(1, amount);
                updateStmt.setInt(2, userId);
                updateStmt.executeUpdate();
            }

            // 2️⃣ Ghi log vào bảng deposits
            try (PreparedStatement insertStmt = conn.prepareStatement(insertDepositQuery)) {
                insertStmt.setInt(1, userId);
                insertStmt.setBigDecimal(2, amount);
                insertStmt.executeUpdate();
            }

            conn.commit(); // Xác nhận transaction
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void changePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword); // Anh có cần mã hóa mật khẩu không?
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User getUserById(int userId) {
        String query = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = connectDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getBigDecimal("balance"),
                        rs.getString("role"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getInt("assigned_computer_id"),
                        rs.getString("status")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void updateUserProfile(User user) {
        String query = "UPDATE users SET password = ?, full_name = ?, email = ?, phone = ? WHERE user_id = ?";
        try (Connection conn = connectDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getPassword());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPhone());
            stmt.setInt(5, user.getUserId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

