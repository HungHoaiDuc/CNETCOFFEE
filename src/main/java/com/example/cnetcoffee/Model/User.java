package com.example.cnetcoffee.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private BigDecimal balance;
    private String role;
    private LocalDateTime createdAt;
    private int assignedComputerId;
    private String status;
    private String remainingTime;

    // Constructor không tham số
    public User() {}

    // Constructor đầy đủ
    public User(int userId, String username, String password, String fullName,
                String email, String phone, BigDecimal balance, String role, LocalDateTime createdAt, int assignedComputerId, String status) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.balance = balance;
        this.role = role;
        this.createdAt = createdAt;
        this.assignedComputerId = assignedComputerId;
        this.status = status;
    }

    public User(int userId, String username, BigDecimal balance, String role, LocalDateTime createdAt, String status) {
        this.userId = userId;
        this.username = username;
        this.balance = balance;
        this.role = role;
        this.createdAt = createdAt;
        this.status = status;
    }


    // Getter & Setter
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAssignedComputerId() {
        return assignedComputerId;
    }

    public void setAssignedComputerId(int assignedComputerId) {
        this.assignedComputerId = assignedComputerId;
    }

    // Phương thức kiểm tra user có phải admin không
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }
    public boolean isStaff() {
        return "STAFF".equalsIgnoreCase(this.role);
    }

    public String getRemainingTime() {
        return remainingTime;
    }

    public String getDisplayName() {
        return this.role + ": " + this.username;
    }

    public void setRemainingTime(String remainingTime) {
        this.remainingTime = remainingTime;
    }
    // Phương thức toString() để hiển thị thông tin user
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", balance=" + balance +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                ", assignedComputerId=" + assignedComputerId +
                '}';
    }
    public int getId() {
        return userId;
    }

}
