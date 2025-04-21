package com.example.cnetcoffee.utils;

import com.example.cnetcoffee.Model.Session;
import com.example.cnetcoffee.Model.User;

import java.io.File;
import java.util.*;


public class SessionManager {
    public static User currentUser;
    private static User loggedInUser;
    private static int currentSessionId = -1;
    private static Session currentSession;
    private static LogoutListener logoutListener;
    private static boolean guestMode = false;
    private static int receiverUserId;
    private static int assignedComputerId;

    // Map lưu thông tin sessionId -> User đang online
    private static final Map<Integer, User> activeUserSessions = new HashMap<>();

    public static User getLoggedInUser() {
        return loggedInUser;
    }

    public static void setLoggedInUser(User user) {
        loggedInUser = user;
    }

    public static void setCurrentSessionId(int sessionId) {
        currentSessionId = sessionId;
    }

    public static int getCurrentSessionId() {
        return currentSessionId;
    }

    public static void clearSession() {
        currentSessionId = -1;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void logout() {
        currentUser = null; // Đăng xuất
        if (logoutListener != null) {
            logoutListener.onLogout();
        }
    }

    public static void setAssignedComputerId(int id) {
        assignedComputerId = id;
    }

    public static int getAssignedComputerId() {
        return assignedComputerId;
    }

    public static int getAssignedComputerId(int userId) {
        // Giả sử bạn có map userId -> computerId, ở đây trả về ID mặc định
        return assignedComputerId;
    }

    public static Session getCurrentSession() {
        return currentSession;
    }

    public static void setCurrentSession(Session session) {
        currentSession = session;
    }

    public static void setGuestMode(boolean mode) {
        guestMode = mode;
    }

    public static boolean isGuestMode() {
        return guestMode;
    }

    public static void setReceiverUserId(int id) {
        receiverUserId = id;
    }

    public static int getReceiverUserId() {
        return receiverUserId;
    }

    public static void reset() {
        currentUser = null;
        currentSessionId = -1;
        currentSession = null;
        guestMode = false;
        activeUserSessions.clear();
    }

    public static void registerUserSession(int sessionId, User user) {
        activeUserSessions.put(sessionId, user);
    }

    public static void unregisterUserSession(int sessionId) {
        activeUserSessions.remove(sessionId);
    }

    public static List<Integer> getAllActiveSessionIds() {
        return new ArrayList<>(activeUserSessions.keySet());
    }

    public static User getUserBySessionId(int sessionId) {
        return activeUserSessions.get(sessionId);
    }

    // Định nghĩa interface LogoutListener
    public interface LogoutListener {
        void onLogout();
    }

    public static void setOnLogoutListener(LogoutListener listener) {
        logoutListener = listener;
    }
}
