package com.example.cnetcoffee.Controller.Admin;

import com.example.cnetcoffee.Model.Product;
import com.example.cnetcoffee.dao.ProductDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BtnProductController {
    @FXML private TextField lb_productName;
    @FXML private TextField lb_price;
    @FXML private ComboBox<String> categoryBox;
    @FXML private ComboBox<String> availabilityBox;
    @FXML private ImageView imageView;
    @FXML private Button save_btn;
    @FXML private Button cancel_btn;

    private ProductDAO productDAO = new ProductDAO();
    private byte[] productImageBytes;
    private Product product;
    private ProductController productController;
    private boolean addMode = false; // Initialize addMode to false

    public void setProductController(ProductController productController) {
        this.productController = productController;
    }

    public void setAddMode(boolean addMode) {
        this.addMode = addMode;
    }

    public boolean isAddMode() {
        return addMode;
    }

    // Khởi tạo các ComboBox
    public void initialize() {
        categoryBox.getItems().addAll("FOOD", "DRINK");
        availabilityBox.getItems().addAll("Available", "Stop Doing Business");
    }

    @FXML
    private void importImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser
                .getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                "Image Files", "*.png", "*.jpg", "*.jpeg")); // Fixed file extension filter
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file)) {
                productImageBytes = fis.readAllBytes(); // Đọc ảnh thành byte[]
                imageView.setImage(new Image(file.toURI().toString()));
            } catch (IOException e) {
                showAlert("Lỗi", "Không thể đọc tệp hình ảnh.");
            }
        }
    }

    public void setProductForUpdate(Product product) {
        this.product = product;
        lb_productName.setText(product.getProductName());
        lb_price.setText(String.valueOf(product.getPrice()));
        categoryBox.setValue(product.getCategory());
        availabilityBox.setValue(product.getAvailability());

        // Nếu có ảnh, hiển thị ảnh lên ImageView
        if (product.getImageBytes() != null) {
            Image image =
                    new Image(
                            new ByteArrayInputStream(
                                    product.getImageBytes())); // Chuyển byte[] thành Image
            imageView.setImage(image); // Hiển thị ảnh trên ImageView
        }
        this.addMode = false; // Indicate that it's an update operation
    }

    @FXML
    private void saveProduct(ActionEvent event) {
        String name = lb_productName.getText().trim();
        double price = 0.0;
        try {
            price = Double.parseDouble(lb_price.getText());
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Định dạng giá không hợp lệ! Vui lòng nhập một số hợp lệ cho giá."

            );
            return;
        }

        String category = categoryBox.getValue();
        String availability = availabilityBox.getValue();

        if (name.isEmpty() || category == null || availability == null) {
            showAlert("Lỗi", "Vui lòng điền đầy đủ tất cả các trường!");
            return;
        }

        // Kiểm tra giá trị availability và thay thế nếu cần thiết
        if ("OUT_OF_STOCK".equalsIgnoreCase(availability)) {
            availability =
                    "Stop Doing Business"; // Thay đổi thành giá trị hợp lệ trong cơ sở dữ liệu
        }

        // Check if adding new product and if the name already exists
        if (addMode && productController.isProductNameExists(name)) {
            showAlert("Lỗi", "Tên sản phẩm đã tồn tại!");
            return; // Do not proceed with saving
        }

        try {
            Product product;

            if (this.product == null) {
                // Thêm sản phẩm mới
                product =
                        new Product(
                                0, name, price, category, availability, productImageBytes);
                if (productDAO.addProduct(product)) {
                    showAlert("Thành công", "Sản phẩm đã được thêm thành công!");
                    updateTableAndClose();
                } else {
                    showAlert("Lỗi", "Không thể thêm sản phẩm.");
                }
            } else {
                // Cập nhật sản phẩm
                byte[] imageToSave =
                        (productImageBytes != null)
                                ? productImageBytes
                                : this.product.getImageBytes();
                product =
                        new Product(
                                this.product.getProductId(),
                                name,
                                price,
                                category,
                                availability,
                                imageToSave);
                if (productDAO.updateProduct(product)) {
                    showAlert("Thành công", "Sản phẩm đã được cập nhật thành công!");
                    updateTableAndClose();
                } else {
                    showAlert("Lỗi", "Không thể cập nhật sản phẩm.");
                }
            }
        } catch (Exception e) {
            showAlert("Lỗi", "Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    private void updateTableAndClose() {
        if (productController != null) {
            productController.loadTable(); // Cập nhật lại bảng sản phẩm
        }
        Stage stage = (Stage) save_btn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void cancelAction(ActionEvent event) {
        Stage stage = (Stage) cancel_btn.getScene().getWindow();
        stage.close();
    }

    // Hiển thị thông báo
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
