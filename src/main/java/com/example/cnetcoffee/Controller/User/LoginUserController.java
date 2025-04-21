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
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginUserController implements Initializable {
    @FXML
    private AnchorPane apLogin;

    @FXML
    private AnchorPane apLogo;

    @FXML
    private ImageView backgroundImage;

    @FXML
    private Button btCancel;

    @FXML
    private Button btLoginUser;

    @FXML
    private PasswordField pfPassword;

    @FXML
    private TextField tfUsername;
    private User loggedInUser;
    private final UserDB userDB = new UserDB();

    @FXML
    void btCancel(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    void btLoginUser(ActionEvent event) {
        String username = tfUsername.getText();
        String password = pfPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Lỗi đăng nhập", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        loggedInUser = userDB.authenticateUser(username, password);

        // ⚠️ Kiểm tra null trước khi gọi bất kỳ hàm nào lên loggedInUser
        if (loggedInUser != null && !loggedInUser.isStaff() && !loggedInUser.isAdmin()) {
            // Kiểm tra trạng thái tài khoản
            if ("locked".equalsIgnoreCase(loggedInUser.getStatus())) {
                showAlert(Alert.AlertType.ERROR, "Tài khoản bị khóa", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên để được hỗ trợ!");
                return;
            }

            // Kiểm tra số dư
            if (loggedInUser.getBalance() == null || loggedInUser.getBalance().doubleValue() <= 0) {
                showAlert(Alert.AlertType.ERROR, "Không đủ số dư", "Tài khoản của bạn không đủ số dư để sử dụng máy. Vui lòng nạp thêm tiền!");
                return;
            }

            SessionManager.setGuestMode(false);
            SessionManager.setCurrentUser(loggedInUser);

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng nhập thành công!\nXin chào: " + loggedInUser.getFullName());

            int computerId = SessionManager.getAssignedComputerId();
            if (computerId > 0) {
                loggedInUser.setAssignedComputerId(computerId);
                SessionManager.setCurrentUser(loggedInUser);

                new UserManagerDAO().updateAssignedComputerId(loggedInUser.getUserId(), computerId);

                SessionDAO sessionDAO = new SessionDAO();
                String computerType = ComputerDB.getComputerType(computerId); // Lấy loại máy
                int sessionId = sessionDAO.createSession(loggedInUser.getUserId(), computerId, computerType);

                SessionManager.setCurrentSessionId(sessionId);

                // ✅ Lấy session đầy đủ từ DB và lưu vào SessionManager
                Session session = sessionDAO.getSessionById(sessionId);
                SessionManager.setCurrentSession(session);
                SessionManager.registerUserSession(sessionId, loggedInUser);

                boolean sent = ClientSocketHandler.sendCommand("TURN_ON " + computerId);
                if (sent) {
                    ComputerDB.updateComputerStatus(computerId, "IN USE", true);
                    StageManager.switchHomeUser(
                            "/com/example/cnetcoffee/user/home_user.fxml",
                            "User Dashboard",
                            false,
                            410, 682,
                            false
                    );
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể bật máy!");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy máy của bạn!");
            }

            UserManagerDAO.updateUserStatus(loggedInUser.getUserId(), "active");
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Sai tên đăng nhập hoặc không phải User!");
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            btLoginUser.setDefaultButton(true);
            Stage stage = (Stage) backgroundImage.getScene().getWindow();

            // Đảm bảo background tự động thay đổi theo kích thước cửa sổ
            backgroundImage.fitWidthProperty().bind(stage.widthProperty());
            backgroundImage.fitHeightProperty().bind(stage.heightProperty());

            backgroundImage.setPreserveRatio(true);
            // Lắng nghe thay đổi kích thước cửa sổ và căn giữa các thành phần
            stage.widthProperty().addListener((obs, oldVal, newVal) -> centerNodes(stage));
            stage.heightProperty().addListener((obs, oldVal, newVal) -> centerNodes(stage));

            // Căn giữa lúc khởi động
            centerNodes(stage);
            backgroundImage.getScene().setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case ESCAPE:
                    case F4:
                        event.consume();
                        break;
                }
            });
        });
    }

    private void centerNodes(Stage stage) {
        double width = stage.getWidth();
        double height = stage.getHeight();

        if (width > 0 && height > 0) {
            apLogo.setLayoutX((width - apLogo.getPrefWidth()) / 2);
            apLogo.setLayoutY(height * 0.1);  // Cách trên 20% chiều cao cửa sổ

            apLogin.setLayoutX((width - apLogin.getPrefWidth()) / 2);
            apLogin.setLayoutY(height * 0.4); // Đặt ở giữa
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}