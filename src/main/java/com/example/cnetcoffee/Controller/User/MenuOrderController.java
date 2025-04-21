package com.example.cnetcoffee.Controller.User;

import com.example.cnetcoffee.Model.CardProduct;
import com.example.cnetcoffee.Model.MenuOrderItem;
import com.example.cnetcoffee.Model.User;
import com.example.cnetcoffee.dao.CardProductDAO;
import com.example.cnetcoffee.dao.DBConnect;
import com.example.cnetcoffee.dao.MenuOrderDAO;
import com.example.cnetcoffee.utils.SessionManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MenuOrderController {
    private static final Logger logger =
            Logger.getLogger(MenuOrderController.class.getName());

    @FXML private GridPane menu_gridPane;
    @FXML private TableView<MenuOrderItem> menu_tableView;
    @FXML private TableColumn<MenuOrderItem, String> menu_col_productName;
    @FXML private TableColumn<MenuOrderItem, Integer> menu_col_quantity;
    @FXML private TableColumn<MenuOrderItem, Double> menu_col_price;
    @FXML private Label lb_total;
    @FXML private TextField textField_search;
    @FXML private Button btn_decrease, btn_Order, btn_receipt;
    @FXML private Tab tab1_order;

    @FXML private Tab tab2_bill;

    @FXML private TabPane tabPane_order;
    //@FXML private TextArea receipt_area; // Đã xóa
    @FXML private Label order_status;

    // Thêm ListView để hiển thị thông tin hóa đơn
    @FXML private ListView<String> listview_menu;

    private int orderId; // Thêm biến instance để lưu orderId

    private Integer guestSessionId = null; // Biến để lưu session_id của khách

    private ObservableList<MenuOrderItem> orderList =
            FXCollections.observableArrayList();
    private Connection conn;
    private CardProductDAO cardProductDAO;
    private MenuOrderDAO menuOrderDAO;

    public void initialize() {
        try {
            conn = DBConnect.getConnection();
            if (conn == null) {
                showAlert("Lỗi", "Không thể kết nối đến cơ sở dữ liệu!");
                return;
            }
            cardProductDAO = new CardProductDAO(conn);
            menuOrderDAO = new MenuOrderDAO(conn);

            // Khởi tạo ListView
            listview_menu.setItems(FXCollections.observableArrayList());

            // Initialize TableView columns
            menu_col_productName.setCellValueFactory(cellData -> cellData.getValue().productNameProperty());
            menu_col_quantity.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
            menu_col_price.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());

            menu_tableView.setItems(orderList);

            // Tải dữ liệu đơn hàng từ cơ sở dữ liệu
            loadOrderData();

            loadProducts();

            textField_search
                    .textProperty()
                    .addListener(
                            (observable, oldValue, newValue) -> {
                                realTimeSearch(newValue);
                            });

            btn_decrease.setOnAction(e -> decreaseQuantity());
            btn_Order.setOnAction(e -> handleOrder());

            // Đăng ký lắng nghe sự kiện đăng xuất
            SessionManager.setOnLogoutListener(this::clearOrderData);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Lỗi trong quá trình khởi tạo", e);
            showAlert("Lỗi", "Đã xảy ra lỗi trong quá trình khởi tạo: " + e.getMessage());
        }
    }

    private void loadOrderData() {
        // 1. Lấy order_id chưa hoàn thành cuối cùng cho phiên hiện tại.
        Integer lastIncompleteOrderId = menuOrderDAO.getLastIncompleteOrderId();

        if (lastIncompleteOrderId != null) {
            // 2. Lấy thông tin các mục trong đơn hàng cho order_id đó từ bảng `order_items`.
            List<MenuOrderItem> orderItems = menuOrderDAO.getOrderItemsForOrderId(lastIncompleteOrderId);

            // 3. Tạo các đối tượng MenuOrderItem từ dữ liệu đã lấy và thêm chúng vào orderList.
            orderList.addAll(orderItems);

            // 4. Cập nhật ListView với dữ liệu từ orderList.
            updateListViewAfterOrder();

            // 5. Tính toán và hiển thị tổng tiền.
            calculateTotal();

            logger.log(Level.INFO, "Đã tải dữ liệu đơn hàng chưa hoàn thành với orderId: " + lastIncompleteOrderId);
        } else {
            logger.log(Level.INFO, "Không tìm thấy đơn hàng chưa hoàn thành nào.");
        }
    }


    private void clearOrderData() {
        Platform.runLater(
                () -> {
                    orderList.clear(); // Xóa dữ liệu order
                    listview_menu.getItems().clear(); // Xóa listview
                    lb_total.setText("0 VNĐ"); // Reset tổng tiền
                    //   menu_tableView.refresh(); // Cập nhật lại table view - No need with
                    // ObservableList
                });
    }

    private void displayProducts(List<CardProduct> products) throws Exception {
        menu_gridPane.getChildren().clear();
        int col = 0, row = 0;
        for (CardProduct product : products) {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/com/example/cnetcoffee/user/cardProduct.fxml"));
            Parent card = loader.load();

            CardController controller = loader.getController();
            controller.setData(product);
            controller
                    .getAddButton()
                    .setOnAction(
                            e -> {
                                try {
                                    addProductToOrder(controller.getProduct(), controller.getQuantity());
                                } catch (SQLException ex) {
                                    throw new RuntimeException(ex);
                                }
                            });
            menu_gridPane.add(card, col, row);
            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }
    }

    private void loadProducts() throws Exception {
        displayProducts(cardProductDAO.getAllProducts());
    }

    private void addProductToOrder(CardProduct product, int quantity) throws SQLException {
        // Tạo một MenuOrderItem mới để biểu diễn sản phẩm được thêm
        MenuOrderItem newItem =
                new MenuOrderItem(
                        product.getId(), product.getName(), quantity, product.getPrice() * quantity);

        // Tìm kiếm sản phẩm trong orderList
        boolean found = false;
        for (MenuOrderItem existingItem : orderList) {
            if (existingItem.getFoodId() == product.getId()) {
                // Sản phẩm đã tồn tại, cập nhật số lượng và giá
                int newQuantity = existingItem.getQuantity() + quantity;
                double newPrice = product.getPrice() * newQuantity;

                existingItem.setQuantity(newQuantity);
                existingItem.setPrice(newPrice);

                found = true;

                break;
            }
        }

        if (!found) {
            // Sản phẩm chưa tồn tại, thêm mới vào orderList
            orderList.add(newItem);
        }
    }

    private double calculateTotal() {
        double total = orderList.stream().mapToDouble(MenuOrderItem::getPrice).sum();
        lb_total.setText(String.format("%.0f vnđ", total));
        return total;
    }

    private void decreaseQuantity() {
        MenuOrderItem selectedItem = menu_tableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            if (selectedItem.getQuantity() > 1) {
                int newQuantity = selectedItem.getQuantity() - 1;
                double newPrice =
                        selectedItem.getPrice() / (selectedItem.getQuantity() + 1) * newQuantity;
                selectedItem.setQuantity(newQuantity);
                selectedItem.setPrice(newPrice);

                calculateTotal();
            } else {
                orderList.remove(selectedItem);
            }
            calculateTotal();
        }
    }

    private String removeDiacritics(String str) {
        if (str == null) return "";
        String normalized = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        return java.util.regex.Pattern.compile("\\p{M}")
                .matcher(normalized)
                .replaceAll("")
                .replace("đ", "d")
                .replace("Đ", "D");
    }

    private void realTimeSearch(String keyword) {
        try {
            String processedKeyword = removeDiacritics(keyword.toLowerCase().trim());
            List<CardProduct> products = cardProductDAO.getAllProducts();
            List<CardProduct> filtered =
                    products.stream()
                            .filter(p -> removeDiacritics(p.getName().toLowerCase()).contains(processedKeyword))
                            .toList();
            displayProducts(filtered);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearOrderTable() {
        orderList.clear();
        lb_total.setText("0 VNĐ");
    }

    private void showAlert(String title, String msg) {
        Platform.runLater(
                () -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(title);
                    alert.setHeaderText(null);
                    alert.setContentText(msg);
                    alert.showAndWait();
                });
    }

    private void handleOrder() {
        try {
            // 1. Lấy thông tin cần thiết
            int userId = getUserId();
            Integer sessionId = getSessionId();
            ObservableList<MenuOrderItem> orderItems = FXCollections.observableArrayList(orderList);

            // Kiểm tra xem order có món nào không
            if (orderItems == null || orderItems.isEmpty()) {
                showAlert("Thông báo", "Vui lòng chọn món trước khi đặt hàng.");
                return;
            }

            // 2. Gọi phương thức createOrder trong DAO
            orderId = menuOrderDAO.createOrder(userId, sessionId, orderItems);

            // 3. Lưu trữ các mục trong đơn hàng vào bảng `order_items`
            if (orderId > 0) {
                for (MenuOrderItem item : orderItems) {
                    menuOrderDAO.createOrderItem(orderId, item);
                }

                showAlert("Thông báo", "Đặt hàng thành công! Mã đơn hàng của bạn là: " + orderId);
                logger.log(Level.INFO, "Order created successfully. Order ID: " + orderId);

                // Cập nhật ListView sau khi đặt hàng thành công
                updateListViewAfterOrder();
                calculateTotal();

                clearOrderTable(); // Xóa giỏ hàng sau khi đặt hàng thành công
                startOrderStatusTracking(); // Bắt đầu theo dõi trạng thái đơn hàng
            } else {
                showAlert("Lỗi", "Đã có lỗi xảy ra trong quá trình đặt hàng. Vui lòng thử lại sau.");
                logger.log(Level.SEVERE, "Failed to create order. Order ID: " + orderId);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating order", e);
            showAlert(
                    "Lỗi", "Đã có lỗi xảy ra trong quá trình đặt hàng. Vui lòng thử lại sau.\n" + e.getMessage());
        }
    }

    private void updateListViewAfterOrder() {
        ObservableList<String> items = listview_menu.getItems();

        for (MenuOrderItem item : orderList) {
            boolean found = false;
            for (int i = 0; i < items.size(); i++) {
                String listItem = items.get(i);
                if (listItem.startsWith(item.getProductName())) {
                    // Sản phẩm đã tồn tại trong ListView, cộng thêm số lượng
                    String[] parts = listItem.split(" x");
                    String productName = parts[0];
                    String quantityPrice = parts[1];

                    String[] quantityAndPriceParts = quantityPrice.split(": ");
                    int oldQuantity = Integer.parseInt(quantityAndPriceParts[0]);
                    double pricePerItem = item.getPrice() / item.getQuantity(); //Giá trên 1 sản phẩm
                    int newQuantity = item.getQuantity() + oldQuantity;
                    double newPrice = newQuantity * pricePerItem;

                    String itemDetails = String.format("%s x%d: %.0f vnđ", productName, newQuantity, newPrice);
                    items.set(i, itemDetails);
                    found = true;
                    break;
                }
            }

            if (!found) {
                // Sản phẩm chưa tồn tại trong ListView, thêm mới
                String itemDetails = String.format("%s x%d: %.0f vnđ", item.getProductName(), item.getQuantity(), item.getPrice());
                items.add(itemDetails);
            }
        }

        // Refresh ListView để hiển thị các thay đổi
        Platform.runLater(() -> {
            listview_menu.refresh();
        });
    }


    private int getUserId() {
        // Lấy userId dựa trên trạng thái đăng nhập
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            // Nếu có người dùng đăng nhập, lấy userId từ đối tượng User
            return currentUser.getUserId();
        } else {
            // Nếu không có người dùng đăng nhập, trả về ID của user "Guest" (cần lấy từ
            // database)
            return getGuestUserIdFromDatabase();
        }
    }

    private int getGuestUserIdFromDatabase() {
        // TODO: Implement the logic to retrieve the user_id of the "Guest" user from the
        // database.
        // Example:
        String sql = "SELECT user_id FROM users WHERE username = 'Guest'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving Guest user ID", e);
            // Handle the exception appropriately (e.g., show an error message to the user)
            showAlert("Lỗi", "Không thể lấy ID của Guest user từ database.");
        }
        return -1; // Return a default value or throw an exception if the Guest user is not
        // found
    }

    private Integer getSessionId() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            // If the user is logged in, retrieve the assigned_computer_id from the users
            // table
            Integer assignedComputerId = currentUser.getAssignedComputerId();
            if (assignedComputerId != null) {
                // Truy vấn database để lấy session_id
                String sql = "SELECT session_id FROM sessions WHERE computer_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, assignedComputerId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        return rs.getInt("session_id");
                    } else {
                        showAlert("Lỗi", "Không tìm thấy session cho máy tính này.");
                        return null;
                    }
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Lỗi khi lấy session_id từ database", e);
                    showAlert("Lỗi", "Lỗi khi lấy session_id từ database.");
                    return null;
                }
            } else {
                showAlert("Lỗi", "Người dùng chưa được gán cho máy tính nào.");
                return null; // Trả về null nếu không có assignedComputerId
            }
        } else {
            // If the user is a guest and doesn't have a session, create a new session
            if (guestSessionId == null) {
                guestSessionId = createGuestSession();
            }
            logger.log(Level.INFO, "Khách đang sử dụng session ID: " + guestSessionId);
            return guestSessionId; // Return the guest's session_id
        }
    }

    private Integer createGuestSession() {
        // Get the current computer_id (logic needs to be implemented)
        int computerId = getCurrentComputerId();
        int guestUserId = getGuestUserIdFromDatabase(); // Lấy user_id của người dùng Guest

        // Create a new session in the database, including the user_id
        String sql =
                "INSERT INTO sessions (computer_id, user_id, start_time) VALUES (?, ?, GETDATE()); SELECT SCOPE_IDENTITY();";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, computerId);
            ps.setInt(2, guestUserId); // Insert the Guest user's ID
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    showAlert("Lỗi", "Không thể tạo session cho khách.");
                    return null;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Lỗi khi tạo session cho khách", e);
            showAlert("Lỗi", "Lỗi khi tạo session cho khách.");
            return null;
        }
    }

    private int getCurrentComputerId() {
        // TODO: Implement logic to get the current computer ID
        return 1; // Replace with the actual computer ID
    }

    private void startOrderStatusTracking() {
        // Kiểm tra xem orderId đã được khởi tạo hay chưa
        if (orderId <= 0) {
            logger.warning("Không thể bắt đầu theo dõi trạng thái đơn hàng vì orderId không hợp lệ.");
            return;
        }

        Timeline timeline =
                new Timeline(
                        new KeyFrame(
                                Duration.seconds(5),
                                event -> { // Kiểm tra mỗi 5 giây
                                    String currentStatus = order_status.getText();
                                    String newStatus =
                                            getOrderStatusFromDatabase(); // Lấy trạng thái từ database

                                    if (!currentStatus.equals(newStatus)) {
                                        Platform.runLater(
                                                () -> { // Cập nhật giao diện trên JavaFX thread
                                                    order_status.setText(newStatus);
                                                });
                                    }
                                }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private String getOrderStatusFromDatabase() {
        // Kiểm tra xem kết nối có hợp lệ hay không
        if (conn == null) {
            logger.severe("Không có kết nối cơ sở dữ liệu!");
            return "Lỗi kết nối";
        }

        String sql = "SELECT status FROM orders WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("status");
            } else {
                logger.warning("Không tìm thấy đơn hàng với orderId: " + orderId);
                return "Không tìm thấy";
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Lỗi khi truy vấn trạng thái đơn hàng từ cơ sở dữ liệu", e);
            e.printStackTrace(); // In stack trace để gỡ lỗi
            return "Lỗi truy vấn"; // Hoặc một giá trị mặc định khác
        }
    }

    // Correct equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuOrderController that = (MenuOrderController) o;
        return orderId == that.orderId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
}
