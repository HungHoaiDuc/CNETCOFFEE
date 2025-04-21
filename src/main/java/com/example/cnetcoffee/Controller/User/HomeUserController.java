package com.example.cnetcoffee.Controller.User;

import com.example.cnetcoffee.Controller.socket.ClientSocketHandler;
import com.example.cnetcoffee.Model.Session;
import com.example.cnetcoffee.Model.User;
import com.example.cnetcoffee.dao.ComputerDB;
import com.example.cnetcoffee.dao.SessionDAO;
import com.example.cnetcoffee.dao.UserDB;
import com.example.cnetcoffee.dao.UserManagerDAO;
import com.example.cnetcoffee.utils.SessionManager;
import com.example.cnetcoffee.utils.StageManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class HomeUserController implements Initializable {
    @FXML
    private Button btLogOut;
    @FXML
    private Button btProfile;
    @FXML
    private Button btnChatUser;
    @FXML
    private TextField tfTotalTime;
    @FXML
    private TextField tfUsedTime;
    @FXML
    private TextField tfRemainingTime;
    @FXML
    private TextField tfTotalCost;
    @FXML
    private TextField tfBalance;
    @FXML
    private Button btn_menuOrder;

    private boolean disableLogout = false;

    private Timeline timeline;

    public void setLogoutDisabled(boolean disable) {
        this.disableLogout = disable;
    }

    @FXML
    void btnChatUser(ActionEvent event) {
        int receiverId = UserDB.getActiveAdminOrStaffId();
        int sessionId = SessionManager.getCurrentSessionId();

        if (receiverId > 0 && sessionId > 0) {
            SessionManager.setReceiverUserId(receiverId);
            SessionManager.setCurrentSessionId(sessionId);
            StageManager.openPopup("/com/example/cnetcoffee/user/chat.fxml", "Message");
        } else {
            System.out.println("❌ Không tìm thấy admin/staff đang hoạt động hoặc session chưa khởi tạo.");
        }
    }

    @FXML
    void btLogOut(ActionEvent event) {

        // Dừng Timeline trước khi đăng xuất
        if (timeline != null) {
            timeline.stop();
            System.out.println("✅ Timeline đã dừng.");
        }

        int computerId = SessionManager.getAssignedComputerId();

        // Cập nhật trạng thái máy tính
        ComputerDB.updateComputerStatus(computerId, "READY", true);
        ClientSocketHandler.sendCommand("READY " + computerId);

        // Lưu tin nhắn vào DB trước khi kết thúc phiên
        int sessionId = SessionManager.getCurrentSessionId();
        SessionManager.unregisterUserSession(sessionId);
        System.out.println("📥 Lưu tin nhắn vào DB cho sessionId: " + sessionId);

        SessionDAO sessionDAO = new SessionDAO();
        Session endedSession = sessionDAO.endSession(sessionId);

        if (endedSession != null) {
            System.out.println("🕒 Kết thúc lúc: " + endedSession.getEndTime());
            System.out.println("⏱ Tổng phút đã chơi: " + endedSession.getTotalMinutes());
        }

        // Lấy total_cost vừa tính xong (ưu tiên lấy từ endedSession)
        BigDecimal totalCost = (endedSession != null) ? endedSession.getTotalCost() : null;

        // Cập nhật trạng thái người dùng
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null && totalCost != null) {
            BigDecimal newBalance = currentUser.getBalance().subtract(totalCost);
            // Cập nhật balance vào DB
            UserManagerDAO.updateUserBalance(currentUser.getUserId(), newBalance);
            // Cập nhật lại balance trong SessionManager (nếu cần)
            currentUser.setBalance(newBalance);

            UserManagerDAO.updateUserStatus(currentUser.getUserId(), "inactive");
            new UserManagerDAO().updateAssignedComputerId(currentUser.getUserId(), null);
        } else if (currentUser != null) {
            // Nếu không có totalCost, vẫn cập nhật trạng thái user
            UserManagerDAO.updateUserStatus(currentUser.getUserId(), "inactive");
            new UserManagerDAO().updateAssignedComputerId(currentUser.getUserId(), null);
        }

        // Reset toàn bộ trạng thái
        SessionManager.reset();

        // Chuyển về màn hình đăng nhập
        Stage stage = StageManager.getPrimaryStage();
        stage.setMinWidth(0);
        stage.setMinHeight(0);
        stage.setMaxWidth(Double.MAX_VALUE);
        stage.setMaxHeight(Double.MAX_VALUE);

        StageManager.switchSceneLoginUser(
                "/com/example/cnetcoffee/user/login_user.fxml",
                "C-NETCOFFEE!",
                false,
                "/img/LOGOC.png"
        );
    }

    @FXML
    void btProfile(ActionEvent event) {
        StageManager.openPopup("/com/example/cnetcoffee/user/profile_user.fxml", "Thông tin người dùng");
    }

    public void btn_menuOrder(ActionEvent actionEvent) {
        StageManager.openPopup("/com/example/cnetcoffee/user/menuOrder.fxml", "MENU ORDER");
    }

    public void updateUserData(String totalTime, String usedTime, String remainingTime, String totalCost, String balance) {
        tfTotalTime.setText(totalTime);
        tfUsedTime.setText(usedTime);
        tfRemainingTime.setText(remainingTime);
        tfTotalCost.setText(totalCost);
        tfBalance.setText(balance);
    }

    public void loadUserData() {
        User currentUser = SessionManager.getCurrentUser();
        Session currentSession = SessionManager.getCurrentSession();

        if (currentUser != null && currentSession != null) {
            BigDecimal balance = currentUser.getBalance();
            LocalDateTime startTime = currentSession.getStartTime();
            String computerType = currentSession.getComputerType();

            System.out.println("Start Time: " + startTime); // Debug giá trị startTime
            System.out.println("Computer Type: " + computerType); // Debug giá trị computerType

            String usedTime = calculateUsedTime(startTime);
            String remainingTime = calculateRemainingTime(balance, computerType, startTime);
            String totalTime = estimateTotalTime(balance, computerType);
            String totalCost = calculateTotalCost(computerType, startTime);
            String balanceStr = String.format("%,.2f", balance); // hiển thị số có dấu phẩy

            System.out.println("Used Time: " + usedTime); // Debug giá trị thời gian đã sử dụng
            System.out.println("Total Cost: " + totalCost); // Debug giá trị tổng chi phí


            updateUserData(totalTime, usedTime, remainingTime, totalCost, balanceStr);
        }
    }

    private String calculateUsedTime(LocalDateTime startTime) {
        LocalDateTime now = LocalDateTime.now();
        if (startTime == null) return "00:00:00";

        long elapsedSeconds = Duration.between(startTime, now).getSeconds();
        if (elapsedSeconds < 1) elapsedSeconds = 1; // Giả lập thời gian tối thiểu là 1 giây

        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String calculateRemainingTime(BigDecimal balance, String type, LocalDateTime startTime) {
        int pricePerHour = com.example.cnetcoffee.dao.PriceDAO.getPricePerHour(type);
        double pricePerSecond = pricePerHour / 3600.0;

        long elapsedSeconds = Duration.between(startTime, LocalDateTime.now()).getSeconds();
        double amountUsed = elapsedSeconds * pricePerSecond;
        double remainingBalance = balance.doubleValue() - amountUsed;

        if (remainingBalance <= 0) return "00:00:00";

        double remainingSeconds = remainingBalance / pricePerSecond;
        long rs = (long) remainingSeconds;

        long hours = rs / 3600;
        long minutes = (rs % 3600) / 60;
        long seconds = rs % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String estimateTotalTime(BigDecimal balance, String type) {
        int pricePerHour = com.example.cnetcoffee.dao.PriceDAO.getPricePerHour(type); // Lấy giá từ DB
        if (pricePerHour <= 0) return "00:00:00"; // Tránh chia cho 0

        double totalSeconds = balance.doubleValue() * 3600 / pricePerHour;
        long seconds = (long) totalSeconds;

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    private String calculateTotalCost(String type, LocalDateTime startTime) {
        LocalDateTime now = LocalDateTime.now();
        if (type == null || startTime == null) return "0.00";

        int pricePerHour = com.example.cnetcoffee.dao.PriceDAO.getPricePerHour(type);
        double pricePerSecond = pricePerHour / 3600.0;

        long elapsedSeconds = Duration.between(startTime, now).getSeconds();
        if (elapsedSeconds < 1) elapsedSeconds = 1; // Giả lập thời gian tối thiểu là 1 giây

        double cost = elapsedSeconds * pricePerSecond;

        return String.format("%,.2f", cost);
    }

    private BigDecimal calculateUpdatedBalance(BigDecimal balance, String type, LocalDateTime startTime) {
        int pricePerHour = com.example.cnetcoffee.dao.PriceDAO.getPricePerHour(type);
        double pricePerSecond = pricePerHour / 3600.0;

        long elapsedSeconds = Duration.between(startTime, LocalDateTime.now()).getSeconds();
        double amountUsed = elapsedSeconds * pricePerSecond;

        double updatedBalance = balance.doubleValue() - amountUsed;
        return BigDecimal.valueOf(Math.max(updatedBalance, 0)); // Đảm bảo không âm
    }

    private void updateRealTimeData() {
        Session currentSession = SessionManager.getCurrentSession();

        if (currentSession != null) {
            LocalDateTime startTime = currentSession.getStartTime();
            String computerType = currentSession.getComputerType();

            String usedTime = calculateUsedTime(startTime);
            String totalCost = calculateTotalCost(computerType, startTime);

            tfUsedTime.setText(usedTime);
            tfTotalCost.setText(totalCost);

            // Khách vãng lai: thời gian còn lại và số dư tài khoản có thể để mặc định
            if (SessionManager.isGuestMode()) {
                tfRemainingTime.setText("9999");
                tfBalance.setText("0.00");
            } else {
                // User thật: tính toán như cũ
                User currentUser = SessionManager.getCurrentUser();
                if (currentUser != null) {
                    BigDecimal balance = currentUser.getBalance();
                    String remainingTime = calculateRemainingTime(balance, computerType, startTime);
                    BigDecimal updatedBalance = calculateUpdatedBalance(balance, computerType, startTime);
                    tfRemainingTime.setText(remainingTime);
                    tfBalance.setText(String.format("%,.2f", updatedBalance));
                }
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        javafx.application.Platform.runLater(() -> {
            if (disableLogout && btLogOut != null) {
                btLogOut.setDisable(true);
            }
            if (SessionManager.isGuestMode()) {
                tfTotalTime.setText("9999");

                // Lấy session ACTIVE cho guest_user và máy này
                int guestUserId = com.example.cnetcoffee.dao.UserDB.getGuestUserId();
                int computerId = SessionManager.getAssignedComputerId();
                Session session = new com.example.cnetcoffee.dao.SessionDAO().getActiveSessionByUserAndComputer(guestUserId, computerId);
                SessionManager.setCurrentSession(session);

                // Nếu muốn, có thể set balance 0 cho khách vãng lai
                // (hoặc sửa updateRealTimeData để không lấy balance từ user)
            } else {
                loadUserData();
            }


            // Khởi tạo Timeline để cập nhật thời gian thực
            timeline = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), event -> {
                updateRealTimeData(); // Cập nhật dữ liệu mỗi giây
            }));
            timeline.setCycleCount(Animation.INDEFINITE); // Lặp vô hạn
            timeline.play();
            System.out.println("✅ Timeline đã bắt đầu.");
        });
    }
}
