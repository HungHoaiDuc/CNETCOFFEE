package com.example.cnetcoffee.Controller.Admin;

import com.example.cnetcoffee.Model.Product;
import com.example.cnetcoffee.dao.ProductDAO;
import com.example.cnetcoffee.utils.StageManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class ProductController {
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> col_productID;
    @FXML private TableColumn<Product, String> col_productName;
    @FXML private TableColumn<Product, Double> col_price;
    @FXML private TableColumn<Product, String> col_category;
    @FXML private TableColumn<Product, String> col_availability;
    @FXML private TableColumn<Product, String> col_imagePath;

    @FXML private Button searchButton;

    @FXML private TextField searchField;

    @FXML private ProductDAO productDAO = new ProductDAO();

    @FXML
    public void initialize() {
        setupTable();
        Platform.runLater(
                () -> {
                    loadTable();
                    setupRealTimeSearch(); // <== Gọi hàm search real-time ở đây
                });
    }

    private void setupRealTimeSearch() {
        searchField
                .textProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            String keyword = removeDiacritics(newValue.trim().toLowerCase());

                            if (keyword.isEmpty()) {
                                productTable.setItems(productDAO.getAllProducts());
                            } else {
                                ObservableList<Product> filteredList = FXCollections.observableArrayList();
                                for (Product product : productDAO.getAllProducts()) {
                                    String name = removeDiacritics(product.getProductName().toLowerCase());
                                    String category = removeDiacritics(product.getCategory().toLowerCase());
                                    String availability =
                                            removeDiacritics(product.getAvailability().toLowerCase());

                                    if (name.contains(keyword)
                                            || category.contains(keyword)
                                            || availability.contains(keyword)) {
                                        filteredList.add(product);
                                    }
                                }
                                productTable.setItems(filteredList);
                            }
                        });
    }

    // Hàm loại bỏ dấu tiếng Việt
    private String removeDiacritics(String str) {
        if (str == null) return "";
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return Pattern.compile("\\p{M}")
                .matcher(normalized)
                .replaceAll("")
                .replace("đ", "d")
                .replace("Đ", "D");
    }

    private void setupTable() {
        col_productID.setCellValueFactory(new PropertyValueFactory<>("productId"));
        col_productName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        col_price.setCellValueFactory(new PropertyValueFactory<>("price"));
        col_category.setCellValueFactory(new PropertyValueFactory<>("category"));
        col_availability.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getAvailability()));
        col_availability.setCellFactory(
                column ->
                        new TableCell<Product, String>() {
                            @Override
                            protected void updateItem(String availability, boolean empty) {
                                super.updateItem(availability, empty);
                                if (empty || availability == null) {
                                    setText(null);
                                    setStyle("");
                                } else {
                                    setText(availability);
                                    if (availability.equalsIgnoreCase("Stop Doing Business")) {
                                        setStyle("-fx-background-color: red; -fx-font-weight: bold;");
                                    } else {
                                        setStyle("-fx-background-color: #09fa01; -fx-font-weight: bold;");
                                    }
                                }
                            }
                        });

        // Cột cho chuỗi hex thay vì ảnh
        col_imagePath.setCellValueFactory(
                cellData -> {
                    byte[] imageBytes = cellData.getValue().getImageBytes();
                    if (imageBytes != null) {
                        return new SimpleStringProperty(byteArrayToHexString(imageBytes));
                    }
                    return new SimpleStringProperty("");
                });

        // Nếu bạn muốn hiển thị ảnh thay vì chuỗi hex trong một cột riêng, sử dụng như sau:
        col_imagePath.setCellFactory(
                param ->
                        new TableCell<Product, String>() {
                            @Override
                            protected void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);

                                if (empty || item == null) {
                                    setGraphic(null);
                                } else {
                                    byte[] imageBytes =
                                            getTableView().getItems().get(getIndex()).getImageBytes();
                                    if (imageBytes != null) {
                                        // Hiển thị ảnh nếu có
                                        Image image = new Image(new ByteArrayInputStream(imageBytes));
                                        ImageView imageView = new ImageView(image);
                                        imageView.setFitWidth(50);
                                        imageView.setFitHeight(50);
                                        setGraphic(imageView);
                                    } else {
                                        setGraphic(null);
                                    }
                                }
                            }
                        });
    }

    public String byteArrayToHexString(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    void loadTable() {
        ObservableList<Product> list = productDAO.getAllProducts();
        productTable.setItems(list);
        productTable.refresh();
        productTable.getItems().setAll(productDAO.getAllProducts());
    }

    @FXML
    private void addProduct(ActionEvent event) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/com/example/cnetcoffee/admin/BtnProduct.fxml"));
            AnchorPane addProductPane = loader.load();

            BtnProductController controller = loader.getController();
            controller.setProductController(this);
            controller.setAddMode(true); // Set add mode to true

            Stage stage = new Stage();
            stage.setTitle("Add Product");
            stage.setScene(new Scene(addProductPane));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadTable();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở cửa sổ Thêm sản phẩm.");
        }
    }

    @FXML
    private void updateProduct(ActionEvent event) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Lỗi", "Chưa chọn sản phẩm!");
            return;
        }
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/com/example/cnetcoffee/admin/BtnProduct.fxml"));
            AnchorPane updateProductPane = loader.load();
            BtnProductController controller = loader.getController();
            controller.setProductController(this);
            controller.setProductForUpdate(selected);
            controller.setAddMode(false);

            Stage stage = new Stage();
            stage.setTitle("Update Product");
            stage.setScene(new Scene(updateProductPane));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            loadTable();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi", "Không thể mở cửa sổ Cập nhật sản phẩm.");
        }
    }

    @FXML
    private void stopBusinessProduct(ActionEvent event) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Kiểm tra nếu sản phẩm đã ngừng kinh doanh
            if ("Stop Doing Business".equalsIgnoreCase(selected.getAvailability())) {
                showAlert("Thông báo", "Sản phẩm này không còn sẵn có.");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Xác nhận");
            confirmAlert.setHeaderText("Sản phẩm không còn kinh doanh");
            confirmAlert.setContentText("Bạn có chắc chắn muốn ngừng bán sản phẩm này không?");
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    selected.setAvailability("Stop Doing Business");
                    if (productDAO.updateProduct(selected)) {
                        showAlert("Thành công!", "Sản phẩm đã ngừng sản xuất!");
                        loadTable();
                    } else {
                        showAlert("Lỗi", "Không thể ngừng sản phẩm này.");
                    }
                }
            });
        } else {
            showAlert("Lỗi", "Vui lòng chọn sản phẩm!");
        }
    }

    @FXML
    private void handleBackAction(ActionEvent event) {
        StageManager.switchScene("/com/example/cnetcoffee/admin/home-admin.fxml", "Admin Dashboard", true);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isProductNameExists(String productName) {
        ObservableList<Product> products = productDAO.getAllProducts();
        for (Product product : products) {
            if (product.getProductName().equalsIgnoreCase(productName)) {
                return true;
            }
        }
        return false;
    }
}
