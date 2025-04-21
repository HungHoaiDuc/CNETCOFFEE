package com.example.cnetcoffee.Controller.Admin;

import com.example.cnetcoffee.Model.User;
import com.example.cnetcoffee.dao.AdminManagerDAO;
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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Optional;

public class AdminManagerController {

    @FXML
    private Button btnCreate, btnSearch;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<User> tableUsers;

    @FXML
    private TableColumn<User, Integer> colID;

    @FXML
    private TableColumn<User, String> colUsername, colBalance,  colStatus, colCreatedAt;

    @FXML
    private TableColumn<User, String> colRole;

    private final AdminManagerDAO adminManagerDAO = new AdminManagerDAO();

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


        colRole.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRole()));


        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));

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
                        setStyle("-fx-background-color: #d4fcd4;");
                    } else if ("inactive".equalsIgnoreCase(status)) {
                        setStyle("-fx-background-color: #ffe5b4;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private String formatBalance(BigDecimal balance) {
        DecimalFormat df = new DecimalFormat("#,###.00");
        return df.format(balance);
    }

    private void loadUsers() {
        ObservableList<User> userList = adminManagerDAO.getAllUsers();
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

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mật khẩu");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Nhập lại mật khẩu");

        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("STAFF");
        roleComboBox.setValue("STAFF");

        grid.add(new Label("Tên đăng nhập:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Mật khẩu:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Xác nhận mật khẩu:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        grid.add(new Label("Chức vụ:"), 0, 3);
        grid.add(roleComboBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Node createButton = dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        Runnable validateInputs = () -> {
            createButton.setDisable(
                    usernameField.getText().trim().isEmpty() ||
                            passwordField.getText().trim().isEmpty() ||
                            confirmPasswordField.getText().trim().isEmpty() ||
                            roleComboBox.getValue() == null
            );
        };

        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validateInputs.run());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateInputs.run());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> validateInputs.run());
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateInputs.run());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Pair<>(
                        usernameField.getText() + "|" + roleComboBox.getValue(),
                        passwordField.getText()
                );
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(userData -> {
            String[] usernameRole = userData.getKey().split("\\|");
            String username = usernameRole[0];
            String role = usernameRole[1];
            String password = userData.getValue();
            String confirmPassword = confirmPasswordField.getText();

            if (!username.trim().isEmpty() && !password.trim().isEmpty()) {
                if (!password.equals(confirmPassword)) {
                    showAlert("❌ Mật khẩu xác nhận không khớp!");
                    return;
                }
                // Gọi hàm tạo user với role được chỉ định
                adminManagerDAO.createUser(username, password, role);
                showAlert("✅ Người dùng đã được tạo thành công!");
                loadUsers();
            } else {
                showAlert("Tên đăng nhập và mật khẩu không được để trống!");
            }
        });
    }



    @FXML
    private void handleSearchUser() {
        String searchQuery = searchField.getText().trim();

        if (searchQuery.isEmpty()) {
            loadUsers();
        } else {
            ObservableList<User> searchResults = adminManagerDAO.searchUsers(searchQuery);
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
                    adminManagerDAO.depositMoney(user.getUserId(), amount);
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
        ContextMenu contextMenu = new ContextMenu();

        MenuItem depositItem = new MenuItem("Nạp tiền");
        depositItem.setOnAction(event -> {
            User selectedUser = tableUsers.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                showDepositDialog(selectedUser);
            }
        });

        MenuItem changePasswordItem = new MenuItem("Đổi mật khẩu");
        changePasswordItem.setOnAction(event -> {
            User selectedUser = tableUsers.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                showChangePasswordDialog(selectedUser);
            }
        });

        MenuItem deleteUserItem = new MenuItem("Xóa người dùng");
        deleteUserItem.setOnAction(event -> {
            User selectedUser = tableUsers.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                deleteUser(selectedUser);
            }
        });

        contextMenu.getItems().addAll(depositItem, changePasswordItem, deleteUserItem);
        tableUsers.setContextMenu(contextMenu);
    }

    private void showChangePasswordDialog(User user) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Đổi mật khẩu");
        dialog.setHeaderText("Đổi mật khẩu cho: " + user.getUsername());
        dialog.setContentText("Nhập mật khẩu mới:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            if (!newPassword.trim().isEmpty()) {
                adminManagerDAO.changePassword(user.getUserId(), newPassword);
                showAlert("Mật khẩu đã được cập nhật!");
            } else {
                showAlert("Mật khẩu không được để trống!");
            }
        });
    }

    private void deleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xóa người dùng");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa " + user.getUsername() + " không?");
        alert.setContentText("Hành động này không thể hoàn tác!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            adminManagerDAO.deleteUser(user.getUserId());
            loadUsers();
            showAlert("Người dùng đã bị xóa!");
        }
    }

}
