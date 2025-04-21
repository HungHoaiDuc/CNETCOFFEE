package com.example.cnetcoffee.Controller.socket;

import com.example.cnetcoffee.Model.Session;
import com.example.cnetcoffee.Model.User;
import com.example.cnetcoffee.dao.ComputerDB;
import com.example.cnetcoffee.dao.SessionDAO;
import com.example.cnetcoffee.dao.UserDB;
import com.example.cnetcoffee.utils.SessionManager;
import com.example.cnetcoffee.utils.StageManager;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientSocketHandler {

    public static boolean sendCommand(String command) {
        try (Socket socket = new Socket("192.168.1.30", 12346);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            writer.println(command); // Gửi lệnh đến server
            String response = reader.readLine(); // Đọc phản hồi từ server

            if ("STATUS_OK".equalsIgnoreCase(response) || "SUCCESS".equalsIgnoreCase(response)) {
                return true; // Server phản hồi thành công
            } else {
                System.out.println("Phản hồi từ server: " + response);
                return false; // Server phản hồi lỗi
            }
        } catch (IOException e) {
            System.out.println("Không thể kết nối đến server: " + e.getMessage());
            return false; // Kết nối thất bại
        }
    }

    public static void startListeningFromAdmin() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(13000)) {
                System.out.println("🟢 User đang lắng nghe Admin trên cổng 13000");

                while (true) {
                    Socket socket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String command = in.readLine();

                    int computerId = SessionManager.getAssignedComputerId();

                    if (command != null) {
                        if (command.equalsIgnoreCase("TURN_ON " + computerId)) {
                            System.out.println("⚡ TURN_ON từ Admin");
                            Platform.runLater(() -> {
                                if (SessionManager.getCurrentUser() == null) {
                                    SessionManager.setGuestMode(true);

                                    int guestUserId = UserDB.getGuestUserId();
                                    UserDB userDB = new UserDB();
                                    User guestUser = userDB.getUserById(guestUserId);

                                    if (guestUser != null) {
                                        SessionManager.setCurrentUser(guestUser); // ✅ Cần dòng này để Chat hoạt động
                                        System.out.println("👤 Đã gán guest_user vào SessionManager.");
                                    } else {
                                        System.out.println("❌ Không lấy được guest_user từ DB!");
                                    }

                                    SessionDAO sessionDAO = new SessionDAO();
                                    Session session = sessionDAO.getActiveSessionByUserAndComputer(guestUserId, computerId);
                                    if (session != null) {
                                        SessionManager.setCurrentSessionId(session.getSessionId());
                                        SessionManager.setCurrentSession(session);
                                        System.out.println("Đã set sessionId cho guest: " + session.getSessionId());
                                    } else {
                                        System.out.println("❌ Không tìm thấy session ACTIVE cho guest_user và máy này!");
                                    }

                                    StageManager.switchHomeUser(
                                            "/com/example/cnetcoffee/user/home_user.fxml",
                                            "User Dashboard",
                                            false,
                                            410, 682,
                                            true
                                    );
                                } else {
                                    System.out.println("⚠️ Đã có user đăng nhập, bỏ qua TURN_ON cho khách vãng lai.");
                                }
                            });
                        }
                        else if (command.equalsIgnoreCase("TURN_OFF " + computerId)) {
                            System.out.println("🔌 TURN_OFF từ Admin");
                            ComputerDB.updateComputerStatus(computerId, "READY", true);

                            // KẾT THÚC PHIÊN LÀM VIỆC (CẬP NHẬT SESSION) CHO CẢ KHÁCH VÃNG LAI VÀ USER THẬT
                            int sessionId = SessionManager.getCurrentSessionId();
                            System.out.println("SessionId khi tắt máy: " + sessionId);
                            if (sessionId > 0) {
                                SessionDAO sessionDAO = new SessionDAO();
                                sessionDAO.endSession(sessionId);
                                System.out.println("✅ Đã cập nhật end_time cho session " + sessionId);
                            }

                            Platform.runLater(() -> {
                                Stage stage = StageManager.getPrimaryStage();
                                stage.setMinWidth(0);
                                stage.setMinHeight(0);
                                stage.setMaxWidth(Double.MAX_VALUE);
                                stage.setMaxHeight(Double.MAX_VALUE);

                                // Reset trạng thái khi tắt máy
                                SessionManager.reset();

                                StageManager.switchSceneLoginUser(
                                        "/com/example/cnetcoffee/user/login_user.fxml",
                                        "C-NETCOFFEE!",
                                        false,
                                        "/img/LOGOC.png"
                                );
                            });
                        }

                    }
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("❌ Lỗi khi lắng nghe Admin: " + e.getMessage());
            }
        }).start();
    }

}
