package com.example.cnetcoffee.Model;

import java.util.List;

public class ChatSummary {
    private int userId;
    private String username;
    private String computerName;
    private int sessionId;
    private boolean isNewMessage;
    private List<Message> messages;

    public ChatSummary(int userId, String username, String computerName, int sessionId, List<Message> messages) {
        this.userId = userId;
        this.username = username;
        this.computerName = computerName;
        this.sessionId = sessionId;
        this.messages = messages;
        this.isNewMessage = true;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getComputerName() {
        return computerName;
    }

    public int getSessionId() {
        return sessionId;
    }

    public boolean isNewMessage() {
        return isNewMessage;
    }

    public void setNewMessage(boolean newMessage) {
        isNewMessage = newMessage;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public String getLastMessageContent() {
        if (messages != null && !messages.isEmpty()) {
            return messages.get(messages.size() - 1).getContent(); // Lấy nội dung tin nhắn cuối cùng
        }
        return "";
    }
}
