package com.example.cnetcoffee.Controller.Admin;

import com.example.cnetcoffee.Controller.socket.ClientSocketHandler;
import com.example.cnetcoffee.Model.Computer;
import com.example.cnetcoffee.Model.Session;
import com.example.cnetcoffee.Model.User;
import com.example.cnetcoffee.dao.ComputerDB;
import com.example.cnetcoffee.dao.DBConnect;
import com.example.cnetcoffee.dao.SessionDAO;
import com.example.cnetcoffee.dao.UserDB;
import com.example.cnetcoffee.utils.SessionManager;
import com.example.cnetcoffee.utils.StageManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class HomeAdminController implements Initializable {
    @FXML
    private AnchorPane apHome;

    @FXML
    private AnchorPane apMenu;

    @FXML
    private AnchorPane apTitle;

    @FXML
    private TextField tfSearchPC;

    @FXML
    private Button btHome;

    @FXML
    private Button btIndividual;

    @FXML
    private ImageView btLogout;

    @FXML
    private Button btManagement;

    @FXML
    private Button btStatistical;

    @FXML
    private AnchorPane homeAdmin;

    @FXML
    private Label Notification;

    @FXML
    private Text user;

    @FXML
    private Button btAddPC;

    @FXML
    private FlowPane flowPaneNormal;

    @FXML
    private FlowPane flowPaneVIP;

    // thông báo
    @FXML
    private Label lbOrderCount;

    @FXML
    void btAdminChat(ActionEvent event) {
        StageManager.openPopup("/com/example/cnetcoffee/admin/Chat_manager.fxml", "Message");
    }


    // Phương thức để cập nhật số lượng đơn hàng đã xác nhận
    public void updateOrderCount() {
        int confirmedOrderCount = getConfirmedOrderCount();
        lbOrderCount.setText(String.valueOf(confirmedOrderCount));
    }
    public static int getConfirmedOrderCount() {
        int confirmedOrderCount = 0;
        String query = "SELECT COUNT(*) FROM orders WHERE status = 'PENDING'";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                confirmedOrderCount = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return confirmedOrderCount;
    }


    @FXML
    void btAddPC(ActionEvent event) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Thêm máy mới");
        dialog.setHeaderText("Chọn loại máy và nhập địa chỉ IP");

        ButtonType addButtonType = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        ChoiceBox<String> typeChoice = new ChoiceBox<>();
        typeChoice.getItems().addAll("NORMAL", "VIP");
        typeChoice.setValue("NORMAL");

        TextField ipField = new TextField();
        ipField.setPromptText("Nhập địa chỉ IP (VD: 192.168.1.30)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.add(new Label("Loại máy:"), 0, 0);
        grid.add(typeChoice, 1, 0);
        grid.add(new Label("Địa chỉ IP:"), 0, 1);
        grid.add(ipField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        ipField.textProperty().addListener((obs, oldVal, newVal) -> {
            addButton.setDisable(newVal.trim().isEmpty() ||
                    !newVal.matches("^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.|$)){4}$"));
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Pair<>(typeChoice.getValue(), ipField.getText().trim());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(data -> {
            String selectedType = data.getKey();
            String ipAddress = data.getValue();

            int newNumber = ComputerDB.getNextComputerNumber(selectedType);
            String newName = (selectedType.equals("VIP") ? "VIP " : "Máy ") + newNumber;

            Computer newComputer = new Computer(
                    0,
                    newName,
                    "AVAILABLE",
                    selectedType,
                    true,
                    ipAddress  // 👈 Truyền IP
            );

            ComputerDB.addComputer(newComputer);
            addComputerToUI(newComputer);

            System.out.println("✅ Đã thêm máy: " + newComputer.getName() + " [" + ipAddress + "]");
        });
    }

    @FXML
    void btUpdatePrice(ActionEvent event) {
        // Lấy giá hiện tại
        int currentNormalPrice = com.example.cnetcoffee.dao.PriceDAO.getPricePerHour("NORMAL");
        int currentVipPrice = com.example.cnetcoffee.dao.PriceDAO.getPricePerHour("VIP");

        // Tạo Dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Cập nhật giá tiền");
        dialog.setHeaderText("Nhập giá mới cho từng loại máy");

        // Nút OK và Cancel
        ButtonType okButtonType = new ButtonType("Cập nhật", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Tạo UI nhập liệu
        TextField tfNormal = new TextField();
        tfNormal.setPromptText("Giá máy thường");
        TextField tfVip = new TextField();
        tfVip.setPromptText("Giá máy VIP");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Thêm dòng hiển thị giá hiện tại
        grid.add(new Label("Giá máy thường hiện tại:"), 0, 0);
        grid.add(new Label(String.valueOf(currentNormalPrice)), 1, 0);
        grid.add(new Label("Giá máy VIP hiện tại:"), 0, 1);
        grid.add(new Label(String.valueOf(currentVipPrice)), 1, 1);

        // Thêm các trường nhập liệu bên dưới
        grid.add(new Label("Máy thường mới:"), 0, 2);
        grid.add(tfNormal, 1, 2);
        grid.add(new Label("Máy VIP mới:"), 0, 3);
        grid.add(tfVip, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Disable OK nếu chưa nhập đủ
        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(true);

        tfNormal.textProperty().addListener((obs, oldVal, newVal) -> {
            okButton.setDisable(tfNormal.getText().trim().isEmpty() || tfVip.getText().trim().isEmpty());
        });
        tfVip.textProperty().addListener((obs, oldVal, newVal) -> {
            okButton.setDisable(tfNormal.getText().trim().isEmpty() || tfVip.getText().trim().isEmpty());
        });

        // Xử lý khi nhấn OK
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                try {
                    int normalPrice = Integer.parseInt(tfNormal.getText().trim());
                    int vipPrice = Integer.parseInt(tfVip.getText().trim());
                    com.example.cnetcoffee.dao.PriceDAO.updatePrice("NORMAL", normalPrice);
                    com.example.cnetcoffee.dao.PriceDAO.updatePrice("VIP", vipPrice);
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật giá tiền!");
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập giá hợp lệ!");
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void addComputerToUI(Computer computer) {
        // Tạo Pane cho máy tính
        Pane computerPane = new Pane();
        computerPane.setPrefSize(135, 140);
        computerPane.setCursor(Cursor.HAND);
        computerPane.setTranslateX(10);
        computerPane.setTranslateY(10);
        updateComputerStyle(computerPane, computer.getStatus(), computer.getType());


        // Tạo ImageView cho hình ảnh máy tính
        ImageView imageView = new ImageView(new Image(getClass().getResource("/img/pc.jpg").toExternalForm()));
        imageView.setFitWidth(122);
        imageView.setFitHeight(102);
        imageView.setLayoutX(6);
        imageView.setLayoutY(6);

        // Tạo Label cho tên máy tính
        Label nameLabel = new Label(computer.getName());
        nameLabel.setLayoutX(47);
        nameLabel.setLayoutY(110);
        nameLabel.getStyleClass().add("computer-name-label"); // Thêm lớp CSS

        // Tạo menu chuột phải
        ContextMenu contextMenu = new ContextMenu();
        MenuItem startItem = new MenuItem("Bật máy");
        MenuItem shutdownItem = new MenuItem("Tắt máy");
        MenuItem detailsItem = new MenuItem("Chi tiết");
        MenuItem deleteItem = new MenuItem("🗑 Xóa máy");

        startItem.setOnAction(event -> {
            turnOnComputerForUser(computer, computerPane);
        });

        shutdownItem.setOnAction(event -> {
            turnOffComputer(computer, computerPane);
        });

        deleteItem.setOnAction(event -> {
            ComputerDB.deleteComputer(computer.getId());

            if ("VIP".equalsIgnoreCase(computer.getType())) {
                flowPaneVIP.getChildren().remove(computerPane);
            } else {
                flowPaneNormal.getChildren().remove(computerPane);
            }

            System.out.println("🗑 Đã xóa máy: " + computer.getName());
        });


        contextMenu.getItems().addAll(startItem, shutdownItem, detailsItem, deleteItem);

        // Hiển thị menu khi click chuột phải
        computerPane.setOnContextMenuRequested(event -> contextMenu.show(computerPane, event.getScreenX(), event.getScreenY()));

        computerPane.getChildren().addAll(imageView, nameLabel);
        if ("VIP".equalsIgnoreCase(computer.getType())) {
            flowPaneVIP.getChildren().add(computerPane);
        } else {
            flowPaneNormal.getChildren().add(computerPane);
        }
    }

    @FXML
    void btHome(ActionEvent event) {

    }

    @FXML
    void btIndividual(ActionEvent event) {
        StageManager.openPopup("/com/example/cnetcoffee/admin/personal.fxml", "Cá nhân");
    }

    @FXML
    void btLogout(MouseEvent event) {
        StageManager.switchScene("/com/example/cnetcoffee/admin/login.fxml", "Đăng nhập", true);
    }

    // nút button quản lý
    @FXML
    private VBox managementMenu, statisticalMenu;
    private boolean isManagementExpanded = false;
    private boolean isStatisticalExpanded = false;
    @FXML
    void toggleManagementMenu(ActionEvent event) {
        if (isManagementExpanded) {
            managementMenu.getChildren().removeIf(node -> node instanceof Button && !node.equals(btManagement));
        } else {
            Button userManager = new Button("Quản lý người dùng");
            Button accountManager = new Button("Quản lý tài khoản");
            Button productManager = new Button("Quản lý sản phẩm");

            userManager.getStyleClass().add("menu-button");
            accountManager.getStyleClass().add("menu-button");
            productManager.getStyleClass().add("menu-button");


            userManager.setOnAction(event1 -> StageManager.switchScene("/com/example/cnetcoffee/admin/user_manager.fxml", "Account Manager", true));
            accountManager.setOnAction(event1 -> StageManager.switchScene("/com/example/cnetcoffee/admin/admin_manager.fxml", "Account Manager", true));
            productManager.setOnAction(event1 -> StageManager.switchScene("/com/example/cnetcoffee/admin/product.fxml", "Product Manager", true));
            managementMenu.getChildren().addAll(userManager, productManager);

            if (SessionManager.isAdmin()) {
                managementMenu.getChildren().add(accountManager);
            }
        }
        isManagementExpanded = !isManagementExpanded;
    }


    @FXML
    void btStatistical(ActionEvent event) {
        if (isStatisticalExpanded) {
            // Thu gọn menu - Xóa các nút con, giữ lại nút gốc (btStatistical)
            statisticalMenu.getChildren().removeIf(node -> node instanceof Button && !node.equals(btStatistical));
        } else {
            // Mở rộng menu - Thêm các nút chức năng

            // Tạo nút "Thống kê doanh thu máy"
            Button statisticalRevenueMachine = new Button("Thống kê doanh thu máy");
            statisticalRevenueMachine.getStyleClass().add("menu-button");
            statisticalRevenueMachine.setOnAction(event1 -> StageManager.switchScene("/com/example/cnetcoffee/admin/statistical_revenue.fxml", "Thống kê doanh thu máy", true));

            // Tạo nút "Thống kê doanh thu dịch vụ"
            Button statisticalRevenueService = new Button("Thống kê doanh thu dịch vụ");
            statisticalRevenueService.getStyleClass().add("menu-button");
            statisticalRevenueService.setOnAction(event1 -> StageManager.switchScene("/com/example/cnetcoffee/admin/Service_revenue_statistics.fxml", "Thống kê doanh thu dịch vụ", true));

            //Tạo nút thống kê tổng
            Button staticticalRevenueSummary = new Button("Thống kê doanh thu tiệm");
            staticticalRevenueSummary.getStyleClass().add("menu-button");
            staticticalRevenueSummary.setOnAction(event1 -> StageManager.switchScene("/com/example/cnetcoffee/admin/revenue_summary.fxml","Thống kê doanh thu tiệm", true));

            // Thêm các nút vào menu
            statisticalMenu.getChildren().addAll(statisticalRevenueMachine, statisticalRevenueService,staticticalRevenueSummary);
        }
        isStatisticalExpanded = !isStatisticalExpanded;
    }



    @FXML
    public void btServiceUser(ActionEvent event)
    {
        StageManager.switchScene("/com/example/cnetcoffee/admin/statiscal_service.fxml", "Dịch vụ", true);
    }

    private static User currentUser;

    @FXML
    void btTurnOffAllPc(ActionEvent event) {
        User currentUser = SessionManager.getCurrentUser(); // Lấy user đang đăng nhập

        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không có người dùng đăng nhập!");
            return;
        }

        // Hiển thị cảnh báo trước khi tắt tất cả máy
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận tắt tất cả máy");
        alert.setHeaderText("⚠️ Bạn có chắc chắn muốn tắt tất cả máy không?");
        alert.setContentText("Hành động này sẽ tắt tất cả máy đang hoạt động.");

        ButtonType buttonYes = new ButtonType("Đồng ý", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonNo = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonYes, buttonNo);

        alert.showAndWait().ifPresent(response -> {
            if (response == buttonYes) {
                Dialog<String> dialog = new Dialog<>();
                dialog.setTitle("Xác thực quản trị");
                dialog.setHeaderText("🔒 Nhập mật khẩu để xác nhận");

                ButtonType confirmButton = new ButtonType("Xác nhận", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(confirmButton, ButtonType.CANCEL);

                PasswordField passwordField = new PasswordField();
                passwordField.setPromptText("Nhập mật khẩu");

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.add(new Label("Mật khẩu:"), 0, 0);
                grid.add(passwordField, 1, 0);

                dialog.getDialogPane().setContent(grid);

                // Chỉ cho phép nhấn "Xác nhận" khi mật khẩu không rỗng
                Node confirmButtonNode = dialog.getDialogPane().lookupButton(confirmButton);
                confirmButtonNode.setDisable(true);

                passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
                    confirmButtonNode.setDisable(newValue.trim().isEmpty());
                });

                // Lấy giá trị mật khẩu khi nhấn xác nhận
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == confirmButton) {
                        return passwordField.getText();
                    }
                    return null;
                });

                Optional<String> passwordOpt = dialog.showAndWait();
                passwordOpt.ifPresent(password -> {
                    if (isCorrectPassword(currentUser.getUsername(), password)) {
                        turnOffAllComputers();
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Tất cả máy đã được tắt.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi xác thực", "❌ Mật khẩu không chính xác! Vui lòng thử lại.");
                    }
                });
            }
        });
    }

    // Xác thực mật khẩu từ database
    private boolean isCorrectPassword(String username, String password) {
        UserDB userDB = new UserDB();
        User user = userDB.authenticateUser(username, password);
        return user != null && "admin".equalsIgnoreCase(user.getRole()); // Chỉ cho phép admin
    }

    // Hàm tắt tất cả máy
    private void turnOffAllComputers() {
        List<Computer> computers = ComputerDB.getAllComputers();
        boolean hasActiveComputer = false;

        for (Computer computer : computers) {
            if ("IN USE".equalsIgnoreCase(computer.getStatus())) {
                hasActiveComputer = true;
                computer.setStatus("AVAILABLE");
                computer.setAvailable(false);
                ComputerDB.updateComputerStatus(computer.getId(), "AVAILABLE", false);
            }
        }

        // Cập nhật giao diện
        Platform.runLater(() -> {
            for (javafx.scene.Node node : flowPaneNormal.getChildren()) {
                if (node instanceof Pane) {
                    ((Pane) node).setStyle("-fx-background-color: #BB0000;");
                }
            }

            for (javafx.scene.Node node : flowPaneVIP.getChildren()) {
                if (node instanceof Pane) {
                    ((Pane) node).setStyle("-fx-background-color: #BB0000;");
                }
            }
        });


        if (hasActiveComputer) {
            System.out.println("✅ Tất cả máy đã được tắt!");
        } else {
            System.out.println("⚠️ Không có máy nào đang bật!");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // Lấy thông tin người dùng hiện tại từ SessionManager
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            System.out.println("❌ Không có người dùng đăng nhập!");
            return;
        }

        // Hiển thị tên người dùng trên giao diện
        user.setText("Xin chào, " + currentUser.getFullName());

        // Kiểm tra vai trò của người dùng
        if (!currentUser.isAdmin()) {
            btStatistical.setDisable(true);
            statisticalMenu.setDisable(true);
        }

        if (flowPaneNormal == null || flowPaneVIP == null) {
            System.out.println("❌ Một trong hai FlowPane chưa được khởi tạo!");
        } else {
            System.out.println("✅ flowPaneNormal và flowPaneVIP đã được khởi tạo.");
        }

        loadComputers();

        tfSearchPC.setOnKeyReleased(event -> searchPC());
        Timeline autoRefresh = new Timeline(
                new KeyFrame(Duration.seconds(3), event -> {
                    loadComputers();
                })
        );
        autoRefresh.setCycleCount(Animation.INDEFINITE);
        autoRefresh.play();
        // Update số lượng đơn hàng đã xác nhận
        updateOrderCount();

        // Update sau 15s
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(15), e -> updateOrderCount())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

    }

    private void updateComputerStyle(Pane computerPane, String status, String type) {
        computerPane.getStyleClass().clear();
        if ("VIP".equalsIgnoreCase(type)) {
            switch (status) {
                case "READY":
                    computerPane.getStyleClass().add("computer-pane-ready");
                    break;
                case "IN USE":
                    computerPane.getStyleClass().add("computer-pane-in-use");
                    break;
                case "AVAILABLE":
                default:
                    computerPane.getStyleClass().add("computer-pane");
                    break;
            }
        } else {
            switch (status) {
                case "READY":
                    computerPane.getStyleClass().add("computer-pane-ready");
                    break;
                case "IN USE":
                    computerPane.getStyleClass().add("computer-pane-in-use");
                    break;
                case "AVAILABLE":
                default:
                    computerPane.getStyleClass().add("computer-pane");
                    break;
            }
        }
    }

    // Bật máy cho khách
    private void turnOnComputerForUser(Computer computer, Pane computerPane) {
        computer.setStatus("IN USE");
        computer.setAvailable(true);

        boolean isSuccess = ClientSocketHandler.sendCommand("TURN_ON " + computer.getId());

        if (isSuccess) {
            ComputerDB.updateComputerStatus(computer.getId(), "IN USE", true);
            updateComputerStyle(computerPane, "IN USE", computer.getType());
            System.out.println("✅ Máy " + computer.getName() + " đã bật và đang chờ người dùng.");

            // --- Tạo session cho khách ---
            int guestUserId = UserDB.getGuestUserId();
            if (guestUserId == -1) {
                System.out.println("❌ Không tìm thấy người dùng mặc định (guest_user)!");
                return;
            }
            User guestUser = new UserDB().getUserById(guestUserId);
            if (guestUser == null) {
                System.out.println("❌ Không thể lấy thông tin người dùng mặc định!");
                return;
            }

            String computerType = computer.getType();
            int sessionId = new SessionDAO().createSession(guestUserId, computer.getId(), computerType);

            // --- Lưu thông tin vào SessionManager (nếu cần) ---
            SessionManager.setGuestMode(true);
            SessionManager.setAssignedComputerId(computer.getId());
            SessionManager.setCurrentSessionId(sessionId);
            Session session = new SessionDAO().getSessionById(sessionId);
            SessionManager.setCurrentSession(session);

            // --- Gửi lệnh cho client để hiển thị home_user.fxml ---
            try (Socket userSocket = new Socket("localhost", 13000);
                 PrintWriter out = new PrintWriter(userSocket.getOutputStream(), true)) {
                out.println("TURN_ON " + computer.getId());
            } catch (IOException e) {
                System.out.println("⚠️ Không gửi được TURN_ON đến máy user");
            }
        } else {
            System.out.println("⚠️ Không thể bật máy " + computer.getName());
        }
    }

    // Tắt máy
    private void turnOffComputer(Computer computer, Pane computerPane) {
        computer.setStatus("READY");
        computer.setAvailable(false);

        boolean isSuccess = ClientSocketHandler.sendCommand("TURN_OFF " + computer.getId());
        if (isSuccess) {
            ComputerDB.updateComputerStatus(computer.getId(), "READY", true);
            computerPane.getStyleClass().remove("computer-pane-in-use");
            computerPane.getStyleClass().add("computer-pane");
            System.out.println("❌ Máy " + computer.getName() + " đã tắt.");
            try (Socket userSocket = new Socket("localhost", 13000);
                 PrintWriter out = new PrintWriter(userSocket.getOutputStream(), true)) {
                out.println("TURN_OFF " + computer.getId());

                // Đợi client xử lý xong (có thể điều chỉnh thời gian nếu cần)
                Thread.sleep(500);

                // Lấy user_id của guest_user (hoặc user thật nếu cần)
                int guestUserId = UserDB.getGuestUserId();
                // Lấy session ENDED mới nhất cho máy này
                Session endedSession = new SessionDAO().getLastEndedSessionByUserAndComputer(guestUserId, computer.getId());
                if (endedSession != null && endedSession.getTotalCost() != null) {
                    double totalCost = endedSession.getTotalCost().doubleValue();
                    showAlert(Alert.AlertType.INFORMATION, "Thanh toán",
                            "Số tiền cần thanh toán cho " + computer.getName() + ": " + String.format("%,.0f", totalCost) + " đ");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Thông báo", "Không tìm thấy thông tin thanh toán cho máy này!");
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("⚠️ Không gửi được TURN_OFF đến máy user");
            }
        } else {
            System.out.println("⚠️ Không thể tắt máy " + computer.getName());
        }
    }

    private void loadComputers() {
        flowPaneNormal.getChildren().clear();
        flowPaneVIP.getChildren().clear();
        List<Computer> computerList = ComputerDB.getAllComputers();
        for (Computer computer : computerList) {
            addComputerToUI(computer);
        }

    }

    // tìm kiếm pc
    private void searchPC() {
        String searchText = removeDiacritics(tfSearchPC.getText().trim().toLowerCase());

        flowPaneNormal.getChildren().clear();
        flowPaneVIP.getChildren().clear();

        if (searchText.isEmpty()) {
            List<Computer> allComputers = ComputerDB.getAllComputers();
            for (Computer computer : allComputers) {
                addComputerToUI(computer);
            }
            return;
        }

        List<Computer> computers = ComputerDB.getAllComputers();
        boolean found = false;

        for (Computer computer : computers) {
            String computerName = removeDiacritics(computer.getName().toLowerCase());

            if (computerName.contains(searchText)) {
                addComputerToUI(computer);
                found = true;
            }
        }

        if (!found) {
            Label noResultLabel = new Label("Không tìm thấy máy: " + searchText);
            noResultLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: red; -fx-font-weight: bold;");
            flowPaneNormal.getChildren().add(noResultLabel);
            flowPaneVIP.getChildren().add(noResultLabel);
        }
    }

    // Hàm loại bỏ dấu tiếng Việt
    private String removeDiacritics(String str) {
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return Pattern.compile("\\p{M}").matcher(normalized).replaceAll("").replace("đ", "d").replace("Đ", "D");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
