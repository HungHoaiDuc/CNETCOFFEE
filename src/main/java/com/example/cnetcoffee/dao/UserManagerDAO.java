package com.example.cnetcoffee.dao;

import com.example.cnetcoffee.Model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManagerDAO {

    private Connection connectDB() throws SQLException {
        return DBConnect.getConnection();
    }

    public ObservableList<User> getAllUsers() {
        ObservableList<User> userList = FXCollections.observableArrayList();
        String query = "SELECT user_id, username, balance, role, created_at, status FROM users WHERE role = 'USER'";

        try (Connection conn = connectDB();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Tạo đối tượng User từ kết quả truy vấn
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getBigDecimal("balance"),
                        rs.getString("role"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getString("status")
                );

                // Nếu trạng thái là 'active', tính toán thời gian còn lại và cập nhật balance
                if ("active".equalsIgnoreCase(user.getStatus())) {
                    String remainingTime = calculateRemainingTimeAndUpdateBalance(user.getUserId(), user.getBalance());
                    user.setRemainingTime(remainingTime);
                } else {
                    // Nếu trạng thái là 'inactive', không hiển thị thời gian còn lại
                    user.setRemainingTime(null);
                }

                // Thêm người dùng vào danh sách
                userList.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    public ObservableList<User> searchUsers(String keyword) {
        ObservableList<User> userList = FXCollections.observableArrayList();
        String query =
                "SELECT user_id, username, balance, role, created_at FROM users " + "WHERE username LIKE ?";

        try (Connection conn = connectDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + keyword + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                userList.add(
                        new User(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getBigDecimal("balance"),
                                rs.getString("role"),
                                rs.getTimestamp("created_at").toLocalDateTime(),
                                rs.getString("status")));
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

    public void createUser(String username, String password, String fullname) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println("ERROR: Username cannot be NULL or empty!");
            return;
        }

        if (isUsernameExists(username)) {
            System.out.println("ERROR: Username already exists!");
            return;
        }

        String sql =
                "INSERT INTO users (username, password, role, balance) VALUES (?, ?, 'USER', 0)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
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
        String insertDepositQuery =
                "INSERT INTO deposits (user_id, amount, deposit_time) VALUES (?, ?, GETDATE())";

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
                        rs.getString("status"));
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

    // thống kê người dùng và order
    public static List<Map<String, Object>> getUsersWithOrders() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql =
                """
                        SELECT
                            u.user_id,
                            u.username,
                            u.full_name,
                            u.status,
                            o.order_id,
                            o.status AS order_status,
                            o.total_price,
                            o.order_time,
                            od.quantity,
                            f.price,
                            f.food_name,
                            c.name
                        FROM users u
                        LEFT JOIN orders o ON u.user_id = o.user_id
                        LEFT JOIN order_details od ON o.order_id = od.order_id
                        LEFT JOIN foods f ON f.food_id = od.food_id
                        LEFT JOIN sessions s ON o.session_id = s.session_id
                        LEFT JOIN computers c ON s.computer_id = c.computer_id AND c.status ='IN USE'
                        WHERE u.status = 'ACTIVE'
                            OR o.status IN ('PENDING', 'BEING PREPARED', 'COMPLETED', 'CANCELLED')
                        """;

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Map<String, Object> row = new HashMap<>();
                row.put("user_id", rs.getInt("user_id"));
                row.put("username", rs.getString("username"));
                row.put("full_name", rs.getString("full_name"));
                row.put("status", rs.getString("status"));
                row.put("name", rs.getString("name"));

                row.put("order_status", rs.getString("order_status"));
                row.put("order_time", rs.getString("order_time"));
                row.put("price", rs.getString("price"));
                row.put("quantity", rs.getString("quantity"));
                row.put("food_name", rs.getString("food_name"));
                row.put("total_price", rs.getString("total_price"));

                row.put("order_id", rs.getInt("order_id")); // thêm dòng này

                list.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void updateAssignedComputerId(int userId, Integer computerId) {
        String sql = "UPDATE users SET assigned_computer_id = ? WHERE user_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (computerId == null) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, computerId);
            }
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            System.out.println("✅ Gán máy " + computerId + " cho user " + userId);
        } catch (Exception e) {
            System.out.println("❌ Lỗi cập nhật máy cho user: " + e.getMessage());
        }
    }

    public String calculateRemainingTimeAndUpdateBalance(int userId, BigDecimal balance) {
        String query = "SELECT c.type, s.start_time, s.total_seconds_used " +
                "FROM users u " +
                "JOIN computers c ON u.assigned_computer_id = c.computer_id " +
                "JOIN sessions s ON u.user_id = s.user_id " +
                "WHERE u.user_id = ? AND s.status = 'ACTIVE' " +
                "ORDER BY s.start_time DESC";

        String updateSessionQuery = "UPDATE sessions SET total_seconds_used = ? WHERE user_id = ? AND status = 'ACTIVE'";
        String updateBalanceQuery = "UPDATE users SET balance = ? WHERE user_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             PreparedStatement updateSessionStmt = conn.prepareStatement(updateSessionQuery);
             PreparedStatement updateBalanceStmt = conn.prepareStatement(updateBalanceQuery)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String type = rs.getString("type");
                Timestamp startTime = rs.getTimestamp("start_time");
                int totalSecondsUsed = rs.getInt("total_seconds_used");

                double pricePerHour = com.example.cnetcoffee.dao.PriceDAO.getPricePerHour(type.toUpperCase());
                double pricePerSecond = pricePerHour / 3600; // Giá tiền mỗi giây

                // Tính thời gian mới sử dụng kể từ lần cập nhật cuối cùng
                long elapsedSeconds = (System.currentTimeMillis() - startTime.getTime()) / 1000;
                long newSecondsUsed = elapsedSeconds - totalSecondsUsed;

                // Nếu không có thời gian mới sử dụng, trả về thời gian còn lại hiện tại
                if (newSecondsUsed <= 0) {
                    double totalSeconds = balance.doubleValue() * 3600 / pricePerHour; // Tổng số giây còn lại
                    long remainingSeconds = (long) totalSeconds;

                    long hours = remainingSeconds / 3600;
                    long minutes = (remainingSeconds % 3600) / 60;
                    long seconds = remainingSeconds % 60;

                    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
                }

                // Tính số tiền đã sử dụng
                double amountUsed = newSecondsUsed * pricePerSecond;

                // Tính số tiền còn lại
                double remainingBalance = balance.doubleValue() - amountUsed;

                // Nếu số tiền còn lại <= 0, trả về "00:00:00" và không cập nhật thêm
                if (remainingBalance <= 0) {
                    updateBalanceStmt.setBigDecimal(1, BigDecimal.ZERO);
                    updateBalanceStmt.setInt(2, userId);
                    updateBalanceStmt.executeUpdate();

                    updateSessionStmt.setInt(1, (int) elapsedSeconds);
                    updateSessionStmt.setInt(2, userId);
                    updateSessionStmt.executeUpdate();

                    return "00:00:00";
                }

                // Cập nhật số giây đã sử dụng vào cơ sở dữ liệu
                updateSessionStmt.setInt(1, (int) elapsedSeconds);
                updateSessionStmt.setInt(2, userId);
                updateSessionStmt.executeUpdate();

                // Cập nhật số tiền còn lại vào cơ sở dữ liệu
                updateBalanceStmt.setBigDecimal(1, BigDecimal.valueOf(remainingBalance));
                updateBalanceStmt.setInt(2, userId);
                updateBalanceStmt.executeUpdate();

                // Tính thời gian còn lại
                double totalSeconds = remainingBalance * 3600 / pricePerHour; // Tổng số giây còn lại
                long remainingSeconds = (long) totalSeconds;

                long hours = remainingSeconds / 3600;
                long minutes = (remainingSeconds % 3600) / 60;
                long seconds = remainingSeconds % 60;

                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "00:00:00";
    }
    public static void updateUserBalance(int userId, BigDecimal newBalance) {
        String sql = "UPDATE users SET balance = ? WHERE user_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, newBalance);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            System.out.println("✅ Đã cập nhật balance cho user " + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
