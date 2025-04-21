package com.example.cnetcoffee.Model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L; // Thêm dòng này để đảm bảo version

    private int senderId;
    private int receiverId;
    private String username;
    private String computerName;
    private String content;
    private LocalDateTime sentTime;
    private int sessionId;

    public Message(int senderId, int receiverId, String username, String computerName, String content, LocalDateTime sentTime, int sessionId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.username = username;
        this.computerName = computerName;
        this.content = content;
        this.sentTime = sentTime;
        this.sessionId = sessionId;
    }

    // Getters and setters...
    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getComputerName() { return computerName; }
    public void setComputerName(String computerName) { this.computerName = computerName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getSentTime() { return sentTime; }
    public void setSentTime(LocalDateTime sentTime) { this.sentTime = sentTime; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    @Override
    public String toString() {
        return senderId + "|" + receiverId + "|" + username + "|" + computerName + "|" +
                sentTime + "|" + sessionId + "|" + content.replace("\n", "\\n").replace("|", "⎮");
    }

    public static Message parse(String line) {
        try {
            String[] parts = line.split("\\|", 7);
            int senderId = Integer.parseInt(parts[0]);
            int receiverId = Integer.parseInt(parts[1]);
            String username = parts[2];
            String computerName = parts[3];
            LocalDateTime sentTime = LocalDateTime.parse(parts[4]);
            int sessionId = Integer.parseInt(parts[5]);
            String content = parts[6].replace("\\n", "\n").replace("⎮", "|");
            return new Message(senderId, receiverId, username, computerName, content, sentTime, sessionId);
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi parse tin nhắn: " + e.getMessage());
            return null;
        }
    }
}
