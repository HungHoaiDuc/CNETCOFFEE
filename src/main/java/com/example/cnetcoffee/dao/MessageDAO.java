package com.example.cnetcoffee.dao;

import com.example.cnetcoffee.Model.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    // Lưu danh sách tin nhắn vào cơ sở dữ liệu
    public void saveMessage(Message msg) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content, sent_time, session_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, msg.getSenderId());
            stmt.setInt(2, msg.getReceiverId());
            stmt.setString(3, msg.getContent());
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(msg.getSentTime()));
            stmt.setInt(5, msg.getSessionId());
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("❌ Lỗi lưu tin nhắn vào DB: " + e.getMessage());
        }
    }


    // Lấy tin nhắn cuối cùng từ cơ sở dữ liệu cho mỗi người nhận
    public List<Message> getLastMessagesFromDB() {
        List<Message> messages = new ArrayList<>();
        String sql = """
        SELECT m1.*
        FROM messages m1
        JOIN (
            SELECT session_id, MAX(sent_time) AS last_time
            FROM messages
            GROUP BY session_id
        ) m2 ON m1.session_id = m2.session_id AND m1.sent_time = m2.last_time
    """;

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Message msg = new Message(
                        rs.getInt("sender_id"),
                        rs.getInt("receiver_id"),
                        "Unknown",
                        "Máy ?",
                        rs.getString("content"),
                        rs.getTimestamp("sent_time").toLocalDateTime(),
                        rs.getInt("session_id")
                );
                messages.add(msg);
            }

        } catch (Exception e) {
            System.out.println("❌ Lỗi khi lấy tin nhắn cuối cùng từ DB: " + e.getMessage());
        }

        return messages;
    }


    // Lấy toàn bộ tin nhắn giữa user và admin trong một phiên cụ thể
    public List<Message> getConversationWithUserInSession(int userId, int sessionId) {
        List<Message> messages = new ArrayList<>();
        String sql = """
        SELECT m.*, s.username AS sender_username, se.computer_id
        FROM messages m
        LEFT JOIN users s ON m.sender_id = s.user_id
        LEFT JOIN sessions se ON m.session_id = se.session_id
        WHERE (m.sender_id = ? OR m.receiver_id = ?)
          AND m.session_id = ?
        ORDER BY m.sent_time
    """;

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, sessionId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Message msg = new Message(
                        rs.getInt("sender_id"),
                        rs.getInt("receiver_id"),
                        rs.getString("sender_username") != null ? rs.getString("sender_username") : "Unknown",
                        "Máy " + (rs.getObject("computer_id") != null ? rs.getInt("computer_id") : "?"),
                        rs.getString("content"),
                        rs.getTimestamp("sent_time").toLocalDateTime(),
                        rs.getInt("session_id")
                );
                messages.add(msg);
            }
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi đọc lịch sử chat trong phiên: " + e.getMessage());
        }

        return messages;
    }


    public List<Message> getAllMessagesFromDB() {
        List<Message> messages = new ArrayList<>();
        String sql = """
        SELECT
            m.sender_id,
            m.receiver_id,
            m.content,
            m.sent_time,
            m.session_id,
            s.username AS sender_username,
            r.username AS receiver_username,
            se.computer_id
        FROM messages m
        LEFT JOIN users s ON m.sender_id = s.user_id
        LEFT JOIN users r ON m.receiver_id = r.user_id
        LEFT JOIN sessions se ON m.session_id = se.session_id
        ORDER BY m.session_id, m.sent_time
    """;

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Message msg = new Message(
                        rs.getInt("sender_id"),
                        rs.getInt("receiver_id"),
                        rs.getString("sender_username") != null ? rs.getString("sender_username") : "Unknown",
                        "Máy " + (rs.getObject("computer_id") != null ? rs.getInt("computer_id") : "?"),
                        rs.getString("content"),
                        rs.getTimestamp("sent_time").toLocalDateTime(),
                        rs.getInt("session_id")
                );
                messages.add(msg);
            }

        } catch (Exception e) {
            System.out.println("❌ Lỗi khi lấy tất cả tin nhắn từ DB: " + e.getMessage());
        }

        return messages;
    }

}
