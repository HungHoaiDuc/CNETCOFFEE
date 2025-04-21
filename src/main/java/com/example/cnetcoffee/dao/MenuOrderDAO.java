package com.example.cnetcoffee.dao;

import com.example.cnetcoffee.Model.MenuOrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MenuOrderDAO {
    private static final Logger logger = Logger.getLogger(MenuOrderDAO.class.getName());
    private Connection conn;

    public MenuOrderDAO(Connection conn) {
        this.conn = conn;
    }

    public int createOrder(int userId, Integer sessionId, List<MenuOrderItem> orderItems) throws SQLException {

        int orderId = -1; // Giá trị mặc định nếu có lỗi
        PreparedStatement psOrder = null;
        PreparedStatement psOrderDetail = null;

        try {
            conn.setAutoCommit(false); // Bắt đầu transaction

            // 1. Insert vào bảng "orders"
            String sqlOrder = "INSERT INTO orders (user_id, session_id, order_time, total_price, status) " +
                    "VALUES (?, ?, GETDATE(), 0, 'PENDING'); " +
                    "SELECT SCOPE_IDENTITY();";

            psOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, userId);
            if (sessionId == null) {
                psOrder.setNull(2, Types.INTEGER); // Set giá trị NULL cho cột session_id
            } else {
                psOrder.setInt(2, sessionId);
            }


            int affectedRows = psOrder.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            try (ResultSet generatedKeys = psOrder.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }


            // 2. Insert vào bảng "order_details"
            String sqlOrderDetail = "INSERT INTO order_details (order_id, food_id, quantity, subtotal) VALUES (?, ?, ?, ?)";
            psOrderDetail = conn.prepareStatement(sqlOrderDetail);

            double totalPrice = 0;
            for (MenuOrderItem item : orderItems) {
                psOrderDetail.setInt(1, orderId);
                psOrderDetail.setInt(2, item.getFoodId());
                psOrderDetail.setInt(3, item.getQuantity());
                psOrderDetail.setDouble(4, item.getSubtotal());
                psOrderDetail.addBatch(); // Thêm vào batch để thực hiện cùng lúc
                totalPrice += item.getSubtotal();
            }
            psOrderDetail.executeBatch(); // Thực hiện batch insert

            // 3. Cập nhật "total_price" trong bảng "orders"
            String sqlUpdateTotal = "UPDATE orders SET total_price = ? WHERE order_id = ?";
            try (PreparedStatement psUpdateTotal = conn.prepareStatement(sqlUpdateTotal)) {
                psUpdateTotal.setDouble(1, totalPrice);
                psUpdateTotal.setInt(2, orderId);
                psUpdateTotal.executeUpdate();
            }


            conn.commit(); // Commit transaction nếu mọi thứ thành công
            logger.log(Level.INFO, "Order created successfully with orderId: " + orderId);
            return orderId;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    System.err.print("Transaction is being rolled back");
                    conn.rollback(); // Rollback transaction nếu có lỗi
                } catch (SQLException excep) {
                    logger.log(Level.SEVERE, "Error rolling back transaction", excep);
                }
            }
            logger.log(Level.SEVERE, "Error creating order", e);
            throw e; // Re-throw exception để controller xử lý
        } finally {
            if (psOrder != null) {
                try {
                    psOrder.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error closing PreparedStatement", e);
                }
            }
            if (psOrderDetail != null) {
                try {
                    psOrderDetail.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Error closing PreparedStatement", e);
                }
            }
            try {
                conn.setAutoCommit(true); // Reset lại auto-commit
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Error setting auto-commit to true", e);
            }
        }
    }

    public void createOrderItem(int orderId, MenuOrderItem item) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, food_id, product_name, quantity, price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, item.getFoodId());
            ps.setString(3, item.getProductName());
            ps.setInt(4, item.getQuantity());
            ps.setDouble(5, item.getPrice());
            ps.executeUpdate();
        }
    }

    public Integer getLastIncompleteOrderId() {
        String sql = "SELECT TOP 1 order_id FROM orders WHERE status = 'PENDING' ORDER BY order_id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("order_id");
            } else {
                return null; // Không tìm thấy đơn hàng chưa hoàn thành nào
            }
        } catch (SQLException e) {
            // Ghi log lỗi hoặc xử lý ngoại lệ khác
            e.printStackTrace();
            return null;
        }
    }

    public List<MenuOrderItem> getOrderItemsForOrderId(int orderId) {
        List<MenuOrderItem> orderItems = new ArrayList<>();
        String sql = "SELECT food_id, product_name, quantity, price FROM order_items WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int foodId = rs.getInt("food_id");
                String productName = rs.getString("product_name");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");

                MenuOrderItem item = new MenuOrderItem(foodId, productName, quantity, price);
                orderItems.add(item);
            }
        } catch (SQLException e) {
            // Ghi log lỗi hoặc xử lý ngoại lệ khác
            e.printStackTrace();
        }
        return orderItems;
    }
}
