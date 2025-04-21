package com.example.cnetcoffee.Controller.User;

import com.example.cnetcoffee.Model.CardProduct;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.io.ByteArrayInputStream;

public class CardController {

    @FXML
    private Label lb_productName;

    @FXML
    private Label lb_product_price;

    @FXML
    private ImageView product_imgView;

    @FXML
    private Spinner<Integer> product_spinner;

    @FXML
    private Button btn_add;

    private CardProduct product;

    public void setData(CardProduct product) {
        this.product = product;

        lb_productName.setText(product.getName());
        lb_product_price.setText(String.format("%.0f vnđ", product.getPrice()));

        // Load ảnh từ byte[]
        if (product.getImage() != null && product.getImage().length > 0) {
            try {
                Image img = new Image(new ByteArrayInputStream(product.getImage()));
                product_imgView.setImage(img);

                // Bo góc ảnh (clip sau khi ảnh load xong)
                product_imgView.setPreserveRatio(true);
                Rectangle clip = new Rectangle(130, 100); // đúng kích thước ImageView
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                product_imgView.setClip(clip);

                System.out.println("✅ Ảnh hiển thị thành công cho: " + product.getName());
            } catch (Exception e) {
                System.out.println("❌ Lỗi hiển thị ảnh: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Không có ảnh cho: " + product.getName());
        }

        // Khởi tạo Spinner
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        product_spinner.setValueFactory(valueFactory);

        // Style nút Add
        styleAddButton();
    }

    private void styleAddButton() {
        btn_add.setStyle("-fx-background-color: #f7941d; -fx-text-fill: white; -fx-background-radius: 5;");
        btn_add.setOnMouseEntered(e -> btn_add.setStyle("-fx-background-color: #ff9e2c; -fx-text-fill: white; -fx-background-radius: 5;"));
        btn_add.setOnMouseExited(e -> btn_add.setStyle("-fx-background-color: #f7941d; -fx-text-fill: white; -fx-background-radius: 5;"));
    }

    public int getQuantity() {
        return product_spinner.getValue();
    }

    public CardProduct getProduct() {
        return product;
    }

    public Button getAddButton() {
        return btn_add;
    }
}
