package com.example.cnetcoffee.dao;

import com.example.cnetcoffee.Controller.User.ChatUserController;
import com.example.cnetcoffee.Model.Session;
import com.example.cnetcoffee.Model.User;

import java.sql.*;
import java.time.LocalDateTime;

public class SessionDAO {

    public int createSession(int userId, int computerId, String computerType) {
        String sql = "INSERT INTO sessions (user_id, computer_id, computer_type) VALUES (?, ?, ?)";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, computerId);
            stmt.setString(3, computerType);

            stmt.executeUpdate();

            var rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int sessionId = rs.getInt(1);
                System.out.println("✅ Tạo session mới: ID " + sessionId);
                return sessionId;
            }

        } catch (Exception e) {
            System.out.println("❌ Lỗi tạo session: " + e.getMessage());
        }
        return -1;
    }

    public Session endSession(int sessionId) {
        String sql = """
            UPDATE sessions
            SET 
                end_time = GETDATE(),
                total_minutes = 
                    CASE 
                        WHEN DATEDIFF(MINUTE, start_time, GETDATE()) = 0 AND DATEDIFF(SECOND, start_time, GETDATE()) > 0 THEN 1
                        ELSE DATEDIFF(MINUTE, start_time, GETDATE())
                        END,
                    total_seconds_used = DATEDIFF(SECOND, start_time, GETDATE()),
                    status = 'ENDED'
                OUTPUT inserted.end_time, inserted.total_minutes, inserted.total_seconds_used
            WHERE session_id = ?
            """;



        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Timestamp endTime = rs.getTimestamp("end_time");
                int totalMinutes = rs.getInt("total_minutes");

                // ✅ Gọi procedure tính tổng tiền sau khi kết thúc
                try (CallableStatement cs = conn.prepareCall("{call sp_calculate_total_cost(?)}")) {
                    cs.setInt(1, sessionId);
                    cs.execute();
                    System.out.println("✅ Đã tính total_cost cho session ID " + sessionId);
                }
                catch (Exception e) {
                    System.out.println("⚠️ Không thể tính total_cost: " + e.getMessage());
                }

                Session session = new Session();
                session.setSessionId(sessionId);
                session.setEndTime(endTime.toLocalDateTime());
                session.setTotalMinutes(totalMinutes);
                session.setStatus("ENDED");
                return session;
            }

        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi kết thúc session: " + e.getMessage());
        }

        return null;
    }


    public Session getSessionById(int sessionId) {
        String sql = "SELECT * FROM sessions WHERE session_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Session session = new Session();
                session.setSessionId(rs.getInt("session_id"));
                session.setUserId(rs.getInt("user_id"));
                session.setComputerId(rs.getInt("computer_id"));
                session.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                session.setEndTime(rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null);
                session.setTotalMinutes(rs.getInt("total_minutes"));
                session.setTotalCost(rs.getBigDecimal("total_cost"));
                session.setStatus(rs.getString("status"));
                session.setComputerType(rs.getString("computer_type"));
                return session;
            }

        } catch (SQLException e) {
            System.out.println("❌ Lỗi khi lấy session: " + e.getMessage());
        }

        return null;
    }

    public Session getActiveSessionByUserAndComputer(int userId, int computerId) {
        String sql = "SELECT * FROM sessions WHERE user_id = ? AND computer_id = ? AND status = 'ACTIVE' ORDER BY start_time DESC";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, computerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Session session = new Session();
                session.setSessionId(rs.getInt("session_id"));
                session.setUserId(rs.getInt("user_id"));
                session.setComputerId(rs.getInt("computer_id"));
                session.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
                session.setEndTime(rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null);
                session.setTotalMinutes(rs.getInt("total_minutes"));
                session.setTotalCost(rs.getBigDecimal("total_cost"));
                session.setStatus(rs.getString("status"));
                session.setComputerType(rs.getString("computer_type"));
                return session;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getActiveSessionIdByUser(int userId) {
        String query = "SELECT session_id FROM sessions WHERE user_id = ? AND status = 'ACTIVE'";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("session_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Session getLastEndedSessionByUserAndComputer(int userId, int computerId) {
        String sql = "SELECT TOP 1 * FROM sessions WHERE user_id = ? AND computer_id = ? AND status = 'ENDED' ORDER BY end_time DESC";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, computerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Session session = new Session();
                session.setSessionId(rs.getInt("session_id"));
                session.setTotalCost(rs.getBigDecimal("total_cost"));
                return session;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
