package com.example.cnetcoffee.Controller.Admin;

import com.example.cnetcoffee.Model.Computer;
import com.example.cnetcoffee.Model.User;
import com.example.cnetcoffee.dao.ComputerDB;
import com.example.cnetcoffee.dao.UserDB;
import com.example.cnetcoffee.utils.SessionManager;
import com.example.cnetcoffee.utils.StageManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LoginAdminController implements Initializable {
    @FXML
    private AnchorPane apLogin;

    @FXML
    private AnchorPane apLogo;

    @FXML
    private ImageView backgroundImage;

    @FXML
    private Button btCancel;

    @FXML
    private Button btLogin;

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
    void btLogin(ActionEvent event) {
        String username = tfUsername.getText();
        String password = pfPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Lỗi đăng nhập", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        loggedInUser = userDB.authenticateUser(username, password);
        if (loggedInUser != null) {
            String role = loggedInUser.getRole();
            if ("ADMIN".equalsIgnoreCase(role) || "STAFF".equalsIgnoreCase(role)) {
                SessionManager.setCurrentUser(loggedInUser);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng nhập thành công!\nXin chào: " + loggedInUser.getFullName());
                handleUserRoles(role);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Bạn không có quyền truy cập vào hệ thống!");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Sai tên đăng nhập hoặc mật khẩu!");
        }
    }

    private void handleUserRoles(String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            System.out.println("🔹 ADMIN đăng nhập: " + loggedInUser);
            StageManager.switchScene("/com/example/cnetcoffee/admin/home-admin.fxml", "Admin Dashboard", true);
        } else if ("STAFF".equalsIgnoreCase(role)) {
            System.out.println("🔹 STAFF đăng nhập: " + loggedInUser);
            StageManager.switchScene("/com/example/cnetcoffee/admin/home-admin.fxml", "Staff Dashboard", true);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resetAllComputerStatus();
        Platform.runLater(() -> {
            btLogin.setDefaultButton(true);
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

    private void resetAllComputerStatus() {
        List<Computer> allComputers = ComputerDB.getAllComputers();
        for (Computer comp : allComputers) {
            ComputerDB.updateComputerStatus(comp.getId(), "AVAILABLE", false);
        }
        System.out.println("🔁 Đã reset toàn bộ trạng thái máy về AVAILABLE trước khi user chạy.");
    }
}
