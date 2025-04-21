package com.example.cnetcoffee.Controller.Admin;

import com.example.cnetcoffee.dao.DBConnect;
import com.example.cnetcoffee.dao.UserManagerDAO;
import com.example.cnetcoffee.utils.StageManager;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatisticalServiceController {

    private static final Logger logger = Logger.getLogger(StatisticalServiceController.class.getName());

    @FXML
    private TableView<Map<String, Object>> tableUser;

    @FXML
    private TableColumn<Map<String, Object>, Integer> colId;
    @FXML
    private TableColumn<Map<String, Object>, String> colUsername;
    @FXML
    private TableColumn<Map<String, Object>, String> colFullname;
    @FXML
    private TableColumn<Map<String, Object>, String> colStatus;
    @FXML
    private TableColumn<Map<String, Object>, String> colFoodName;
    @FXML
    private TableColumn<Map<String, Object>, String> colOrderStatus;
    @FXML
    private TableColumn<Map<String, Object>, String> colComputerId;
    @FXML
    private TableColumn<Map<String, Object>, String> colQuantity;
    @FXML
    private TableColumn<Map<String, Object>, String> colPrice;
    @FXML
    private TableColumn<Map<String, Object>, String> colTotalPrice;
    @FXML
    private TableColumn<Map<String, Object>, String> colOrderTime;

    private Connection conn; // Thêm biến kết nối

    @FXML
    public void initialize() {
        try {
            conn = DBConnect.getConnection(); // Khởi tạo kết nối
            if (conn == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể kết nối đến cơ sở dữ liệu!");
                return;
            }

            // Cấu hình các cột trong bảng
            colId.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>((Integer) cell.getValue().get("user_id")));
            colUsername.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(getSafeString(cell.getValue().get("username"))));
            colFullname.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(getSafeString(cell.getValue().get("full_name"))));
            colStatus.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(getSafeString(cell.getValue().get("status"))));
            colComputerId.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(getSafeString(cell.getValue().get("name"))));
            colQuantity.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(getSafeString(cell.getValue().get("quantity"))));
            colPrice.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(getSafeString(cell.getValue().get("price"))));
            colFoodName.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(getSafeString(cell.getValue().get("food_name"))));
            colTotalPrice.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(getSafeString(cell.getValue().get("total_price"))));
            colOrderTime.setCellValueFactory(cell -> {
                Object ts = cell.getValue().get("order_time");
                return new ReadOnlyObjectWrapper<>(ts != null ? ts.toString() : "NULL");
            });
            colOrderStatus.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(getSafeString(cell.getValue().get("order_status"))));

            // Thêm ComboBox vào cột Trạng thái đơn hàng
            colOrderStatus.setCellFactory(param -> {
                TableCell<Map<String, Object>, String> cell = new TableCell<Map<String, Object>, String>() {
                    private final ComboBox<String> comboBox = new ComboBox<>();

                    {
                        comboBox.getItems().addAll("PENDING", "BEING PREPARED", "COMPLETED", "CANCELLED");
                        comboBox.setOnAction(event -> {
                            String selectedStatus = comboBox.getSelectionModel().getSelectedItem();
                            Map<String, Object> row = getTableRow().getItem();
                            if (row != null) {
                                int orderId = (Integer) row.get("order_id");
                                updateOrderStatusInDatabase(orderId, selectedStatus);
                                row.put("order_status", selectedStatus);
                                applyStatusStyle(comboBox, selectedStatus);
                                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Trạng thái đơn hàng đã được cập nhật!");
                            }
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        } else {
                            comboBox.setValue(item);
                            applyStatusStyle(comboBox, item);
                            setGraphic(comboBox);
                        }
                    }
                };
                return cell;
            });

            loadActiveUsers();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Lỗi trong quá trình khởi tạo", e);
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi trong quá trình khởi tạo: " + e.getMessage());
        }
    }

    // Hàm tiện ích để xử lý null
    private String getSafeString(Object obj) {
        return obj != null ? obj.toString() : "NULL";
    }

    // Update dữ liệu người dùng
    private void loadActiveUsers() {
        List<Map<String, Object>> data = UserManagerDAO.getUsersWithOrders();
        tableUser.getItems().setAll(data);
    }

    private void updateOrderStatusInDatabase(int orderId, String status) {
        if (conn == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không có kết nối đến cơ sở dữ liệu!");
            return;
        }

        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi");
                    alert.setHeaderText("Không thể cập nhật trạng thái đơn hàng");
                    alert.setContentText("Không tìm thấy đơn hàng với ID: " + orderId);
                    alert.showAndWait();
                });
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Lỗi khi cập nhật trạng thái đơn hàng", e);
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText("Không thể cập nhật trạng thái đơn hàng");
                alert.setContentText("Đã xảy ra lỗi: " + e.getMessage());
                alert.showAndWait();
            });
        }
    }

    @FXML
    void btBack(ActionEvent event) {
        StageManager.switchScene("/com/example/cnetcoffee/admin/home-admin.fxml", "Admin Dashboard", true);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void applyStatusStyle(ComboBox<String> comboBox, String status) {
        comboBox.setStyle(""); // reset
        switch (status) {
            case "Đã xác nhận":
                comboBox.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                break;
            case "Đang chuẩn bị":
                comboBox.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
                break;
            case "Đã giao":
                comboBox.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                break;
            case "Đã hủy":
                comboBox.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
                break;
            default:
                comboBox.setStyle("-fx-background-color: transparent;");
        }
    }
}
