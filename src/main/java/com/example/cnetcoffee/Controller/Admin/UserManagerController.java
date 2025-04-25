package com.example.cnetcoffee.Controller.Admin;

import com.example.cnetcoffee.dao.UserManagerDAO;
import com.example.cnetcoffee.Model.User;
import com.example.cnetcoffee.utils.StageManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Optional;

public class UserManagerController {

    @FXML
    private Button btnCreate, btnSearch;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<User> tableUsers;

    @FXML
    private TableColumn<User, Integer> colID;

    @FXML
    private TableColumn<User, String> colUsername, colBalance,colRole,  colStatus, colCreatedAt;

    @FXML
    private TableColumn<User, String> colRemainingTime;

    private final UserManagerDAO userManagerDAO = new UserManagerDAO();

    @FXML
    void btBack(ActionEvent event) {
        StageManager.switchScene("/com/example/cnetcoffee/admin/home-admin.fxml", "Admin Dashboard", true);
    }

    @FXML
    public void initialize() {
        colID.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getUserId()).asObject());

        colUsername.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUsername()));

        colBalance.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatBalance(cellData.getValue().getBalance())));

        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));

        colRemainingTime.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRemainingTime()));


        colCreatedAt.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreatedAt().toString()));

        loadUsers();

        tableUsers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setupContextMenu();

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> loadUsers()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    setAlignment(Pos.CENTER);

                    if ("active".equalsIgnoreCase(status)) {
                        setStyle("-fx-background-color: #d4fcd4;"); // Xanh nhạt
                    } else if ("inactive".equalsIgnoreCase(status)) {
                        setStyle("-fx-background-color: #ffe5b4;"); // Vàng nhạt
                    } else if ("locked".equalsIgnoreCase(status)) {
                        setStyle("-fx-background-color: #ffb4b4;"); // Đỏ nhạt (tài khoản bị khóa)
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private String formatBalance(BigDecimal balance) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(balance);
    }

    private void loadUsers() {
        ObservableList<User> userList = userManagerDAO.getAllUsers();
        tableUsers.setItems(userList);
    }

    @FXML
    private void handleCreateUser(MouseEvent event) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Tạo Người Dùng Mới");
        dialog.setHeaderText("Nhập thông tin người dùng:");

        ButtonType createButtonType = new ButtonType("Tạo", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setAlignment(Pos.CENTER);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Tên đăng nhập");

        TextField fullnameField = new TextField();
        fullnameField.setPromptText("Họ và tên");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mật khẩu");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Nhập lại mật khẩu");

        grid.add(new Label("Tên đăng nhập:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Họ và tên:"), 0, 1);
        grid.add(fullnameField, 1, 1);
        grid.add(new Label("Mật khẩu:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Xác nhận mật khẩu:"), 0, 3);
        grid.add(confirmPasswordField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Chỉ cho nhấn nút "Tạo" khi nhập đủ
        Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        // Lắng nghe thay đổi trên các trường nhập liệu
        ChangeListener<String> fieldListener = (obs, oldVal, newVal) -> {
            createButton.setDisable(
                    usernameField.getText().trim().isEmpty() || fullnameField.getText().trim().isEmpty() || passwordField.getText().trim().isEmpty() || confirmPasswordField.getText().trim().isEmpty()
            );
        };

        usernameField.textProperty().addListener(fieldListener);
        fullnameField.textProperty().addListener(fieldListener);
        passwordField.textProperty().addListener(fieldListener);
        confirmPasswordField.textProperty().addListener(fieldListener);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Pair<>(usernameField.getText(), passwordField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(userData -> {
            String username = userData.getKey();
            String password = userData.getValue();
            String confirmPassword = confirmPasswordField.getText();
            String fullname = fullnameField.getText(); // Lấy giá trị từ fullnameField

            if (!username.trim().isEmpty() && !password.trim().isEmpty() && !fullname.trim().isEmpty()) {
                if (!password.equals(confirmPassword)) {
                    showAlert("❌ Mật khẩu xác nhận không khớp!");
                    return;
                }

                // Gọi phương thức tạo người dùng với fullname
                userManagerDAO.createUser(username, password, fullname);
                showAlert("✅ Người dùng đã được tạo thành công!");
                loadUsers();
            } else {
                showAlert("Tên đăng nhập, họ và tên, và mật khẩu không được để trống!");
            }
        });
    }




    @FXML
    private void handleSearchUser() {
        String searchQuery = searchField.getText().trim();

        if (searchQuery.isEmpty()) {
            loadUsers();
        } else {
            ObservableList<User> searchResults = userManagerDAO.searchUsers(searchQuery);
            tableUsers.setItems(searchResults);
        }
    }

    private void showDepositDialog(User user) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Deposit Money");
        dialog.setHeaderText("Nạp tiền cho: " + user.getUsername());
        dialog.setContentText("Nhập số tiền:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                BigDecimal amount = new BigDecimal(amountStr);
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    userManagerDAO.depositMoney(user.getUserId(), amount);
                    loadUsers(); // Cập nhật lại danh sách
                } else {
                    showAlert("Số tiền phải lớn hơn 0!");
                }
            } catch (NumberFormatException e) {
                showAlert("Vui lòng nhập một số hợp lệ!");
            }
        });
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupContextMenu() {
        tableUsers.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    User selectedUser = row.getItem();
                    ContextMenu contextMenu = new ContextMenu();

                    MenuItem depositItem = new MenuItem("Nạp tiền");
                    depositItem.setOnAction(e -> showDepositDialog(selectedUser));

                    MenuItem changePasswordItem = new MenuItem("Đổi mật khẩu");
                    changePasswordItem.setOnAction(e -> showChangePasswordDialog(selectedUser));

                    String status = selectedUser.getStatus();
                    if ("locked".equalsIgnoreCase(status)) {
                        // Nếu đang locked thì cho mở khóa
                        MenuItem unlockUserItem = new MenuItem("Mở khóa tài khoản");
                        unlockUserItem.setOnAction(e -> unlockUser(selectedUser));
                        contextMenu.getItems().addAll(depositItem, changePasswordItem, unlockUserItem);
                    } else {
                        // Nếu là active hoặc inactive thì cho khóa
                        MenuItem lockUserItem = new MenuItem("Khóa tài khoản");
                        lockUserItem.setOnAction(e -> lockUser(selectedUser));
                        contextMenu.getItems().addAll(depositItem, changePasswordItem, lockUserItem);
                    }

                    row.setContextMenu(contextMenu);
                } else {
                    row.setContextMenu(null);
                }
            });
            return row;
        });
    }


    private void showChangePasswordDialog(User user) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Đổi mật khẩu");
        dialog.setHeaderText("Đổi mật khẩu cho: " + user.getUsername());
        dialog.setContentText("Nhập mật khẩu mới:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            if (!newPassword.trim().isEmpty()) {
                userManagerDAO.changePassword(user.getUserId(), newPassword);
                showAlert("Mật khẩu đã được cập nhật!");
            } else {
                showAlert("Mật khẩu không được để trống!");
            }
        });
    }

    private void lockUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Khóa tài khoản");
        alert.setHeaderText("Bạn có chắc chắn muốn khóa tài khoản " + user.getUsername() + " không?");
        alert.setContentText("Tài khoản sẽ không thể đăng nhập cho đến khi được mở lại.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userManagerDAO.updateUserStatus(user.getUserId(), "locked"); // hoặc "locked" nếu bạn muốn
            loadUsers();
            showAlert("Tài khoản đã bị khóa!");
        }
    }

    private void unlockUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Mở khóa tài khoản");
        alert.setHeaderText("Bạn có chắc chắn muốn mở khóa tài khoản " + user.getUsername() + " không?");
        alert.setContentText("Tài khoản sẽ có thể đăng nhập lại.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userManagerDAO.updateUserStatus(user.getUserId(), "inactive");
            loadUsers();
            showAlert("Tài khoản đã được mở khóa!");
        }
    }
}
