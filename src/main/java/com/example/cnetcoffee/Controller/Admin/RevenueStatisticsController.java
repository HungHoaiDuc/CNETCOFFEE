package com.example.cnetcoffee.Controller.Admin;

import com.example.cnetcoffee.Model.DailyRevenue;
import com.example.cnetcoffee.Model.ProductRevenue;
import com.example.cnetcoffee.dao.ProductRevenueDAO;
import com.example.cnetcoffee.utils.StageManager;
import com.example.cnetcoffee.utils.StringUtils;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class RevenueStatisticsController implements Initializable {

    @FXML private DatePicker datepicker1;

    @FXML private DatePicker datepicker2;

    @FXML private TextField textField_search;

    @FXML private Button btn_search;

    @FXML private Label lb_Total_service_revenue;

    @FXML private TableView<ProductRevenue> tableView;

    @FXML private TableColumn<ProductRevenue, String> col_nameProduct;

    @FXML private TableColumn<ProductRevenue, Image> col_imageProduct;

    @FXML private TableColumn<ProductRevenue, Double> col_Price;

    @FXML private TableColumn<ProductRevenue, Integer> col_Quantity;

    @FXML private TableColumn<ProductRevenue, Double> col_Total_product_revenue;

    @FXML private Button btn_back;

    @FXML private TableView<DailyRevenue> tableView_Daily_Revenue;

    @FXML private TableColumn<DailyRevenue, LocalDate> col_date;

    @FXML private TableColumn<DailyRevenue, Double> col_Daily_Revenue;

    private ProductRevenueDAO productRevenueDAO;

    private ObservableList<ProductRevenue> productRevenueList;
    private FilteredList<ProductRevenue> filteredData;
    private ObservableList<DailyRevenue> dailyRevenueList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        productRevenueDAO = new ProductRevenueDAO();
        productRevenueList = FXCollections.observableArrayList();
        dailyRevenueList = FXCollections.observableArrayList();

        // Khởi tạo FilteredList TRƯỚC KHI gọi loadData
        filteredData = new FilteredList<>(productRevenueList, p -> true);

        // Thiết lập các cột cho bảng Daily Revenue
        col_date.setCellValueFactory(new PropertyValueFactory<>("date"));
        col_Daily_Revenue.setCellValueFactory(new PropertyValueFactory<>("dailyRevenue"));

        // Định dạng tiền tệ cho cột Daily Revenue
        col_Daily_Revenue.setCellFactory(
                column -> {
                    return new TableCell<DailyRevenue, Double>() {
                        private final NumberFormat currencyFormat =
                                NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

                        @Override
                        protected void updateItem(Double item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item == null || empty) {
                                setText(null);
                            } else {
                                setText(currencyFormat.format(item));
                            }
                        }
                    };
                });

        // Thiết lập các cột trong TableView
        col_nameProduct.setCellValueFactory(new PropertyValueFactory<>("nameProduct"));
        col_imageProduct.setCellValueFactory(new PropertyValueFactory<>("imageProduct"));
        col_Price.setCellValueFactory(new PropertyValueFactory<>("price"));
        col_Quantity.setCellValueFactory(new PropertyValueFactory<>("quantityOrdered"));
        col_Total_product_revenue.setCellValueFactory(new PropertyValueFactory<>("totalProductRevenue"));

        // Custom cell factory cho cột hình ảnh
        col_imageProduct.setCellFactory(
                param -> {
                    final ImageView imageView = new ImageView();
                    imageView.setFitWidth(50); // Điều chỉnh kích thước hình ảnh
                    imageView.setFitHeight(50);
                    TableCell<ProductRevenue, Image> cell =
                            new TableCell<ProductRevenue, Image>() {
                                @Override
                                public void updateItem(Image item, boolean empty) {
                                    super.updateItem(item, empty);
                                    if (empty) {
                                        setGraphic(null);
                                    } else {
                                        imageView.setImage(item);
                                        setGraphic(imageView);
                                    }
                                }
                            };
                    return cell;
                });

        // Vô hiệu hóa các ngày trước ngày bắt đầu trong datepicker2
        datepicker2.setDayCellFactory(
                picker ->
                        new DateCell() {
                            @Override
                            public void updateItem(LocalDate date, boolean empty) {
                                super.updateItem(date, empty);
                                LocalDate startDate = datepicker1.getValue();
                                if (startDate != null && date.isBefore(startDate)) {
                                    setDisable(true);
                                }
                            }
                        });

        // Realtime search
        textField_search
                .textProperty()
                .addListener((observable, oldValue, newValue) -> updateFilteredData(newValue));

        // Load dữ liệu ban đầu (ví dụ: từ đầu tháng đến hiện tại)
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();
        loadData(startDate, endDate, ""); // Không search text khi khởi tạo
        tableView.setItems(filteredData);
        tableView_Daily_Revenue.setItems(dailyRevenueList);
    }

    @FXML
    void btn_search(ActionEvent event) {
        LocalDate startDate = datepicker1.getValue();
        LocalDate endDate = datepicker2.getValue();
        String searchText = textField_search.getText();

        if (startDate == null || endDate == null) {
            // Xử lý lỗi: Hiển thị thông báo yêu cầu chọn ngày
            System.out.println("Vui lòng chọn ngày bắt đầu và ngày kết thúc.");
            return;
        }

        loadData(startDate, endDate, searchText);
    }

    private void loadData(LocalDate startDate, LocalDate endDate, String searchText) {
        // Load Product Revenue data
        List<ProductRevenue> data =
                productRevenueDAO.getProductRevenue(startDate, endDate, searchText);
        productRevenueList.clear();
        productRevenueList.addAll(data);
        updateFilteredData(textField_search.getText());

        // Load Daily Revenue data
        List<DailyRevenue> dailyData =
                productRevenueDAO.getDailyRevenue(startDate, endDate, searchText);
        dailyRevenueList.clear();
        dailyRevenueList.addAll(dailyData);
        tableView_Daily_Revenue.setItems(dailyRevenueList);

        // Cập nhật tổng doanh thu
        double totalRevenue =
                productRevenueDAO.getTotalServiceRevenue(startDate, endDate, searchText);
        lb_Total_service_revenue.setText(String.format("%.2f vnđ", totalRevenue));
    }

    private void updateFilteredData(String searchText) {
        filteredData.setPredicate(
                productRevenue -> {
                    if (searchText == null || searchText.isEmpty()) {
                        return true;
                    }
                    String lowerCaseFilter = StringUtils.removeAccent(searchText.toLowerCase());
                    String productName = StringUtils.removeAccent(productRevenue.getNameProduct().toLowerCase());
                    return productName.contains(lowerCaseFilter);
                });
    }

    @FXML
    void btn_back(ActionEvent event) {
        StageManager.switchScene("/com/example/cnetcoffee/admin/home-admin.fxml", "Admin Dashboard", true);
    }
}
