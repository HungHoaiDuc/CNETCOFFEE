package com.example.cnetcoffee.utils;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

public class StageManager {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void switchScene(String fxmlFile, String title, boolean screen) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(StageManager.class.getResource(fxmlFile)));
            Parent root = loader.load();

            Screen screenSize = Screen.getPrimary();
            Rectangle2D bounds = screenSize.getVisualBounds();

            Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.setResizable(true);
            primaryStage.setMaximized(screen);
            primaryStage.show();
        } catch (IOException | NullPointerException e) {
            System.out.println("❌ Lỗi tải FXML: " + fxmlFile);
            e.printStackTrace();
        }
    }

    public static void switchSceneLoginUser(String fxmlFile, String title, boolean resizable, String iconPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(StageManager.class.getResource(fxmlFile)));
            Parent root = loader.load();


            // Gán scene
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);

            // Full screen thực sự
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

            // Không cho tắt (Alt+F4)
            primaryStage.setOnCloseRequest(event -> event.consume());

            // Set icon nếu có
            if (iconPath != null) {
                primaryStage.getIcons().add(new Image(Objects.requireNonNull(StageManager.class.getResourceAsStream(iconPath))));
            }

            primaryStage.show();

        } catch (IOException | NullPointerException e) {
            System.out.println("❌ Lỗi tải FXML: " + fxmlFile);
            e.printStackTrace();
        }
    }

    public static void switchHomeUser(String fxmlFile, String title, boolean resizable, double width, double height, boolean disableLogout) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(StageManager.class.getResource(fxmlFile)));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof com.example.cnetcoffee.Controller.User.HomeUserController) {
                ((com.example.cnetcoffee.Controller.User.HomeUserController) controller).setLogoutDisabled(disableLogout);
            }

            // Hủy full screen nếu đang bật
            primaryStage.setFullScreen(false);
            primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

            // Gỡ giới hạn cũ (nếu có)
            primaryStage.setMinWidth(0);
            primaryStage.setMinHeight(0);
            primaryStage.setMaxWidth(Double.MAX_VALUE);
            primaryStage.setMaxHeight(Double.MAX_VALUE);

            // Tạo scene mới
            Scene scene = new Scene(root, width, height);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.setResizable(resizable);
            primaryStage.setMinWidth(width);
            primaryStage.setMinHeight(height);
            primaryStage.setMaxWidth(width);
            primaryStage.setMaxHeight(height);

            // Căn phải màn hình
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            double screenWidth = screenBounds.getWidth();
            double windowX = screenWidth - width;
            primaryStage.setX(windowX);
            primaryStage.setY((screenBounds.getHeight() - height) / 2);

            primaryStage.setOnCloseRequest(event -> {
                event.consume();
                primaryStage.setIconified(false);
            });

            primaryStage.show();
        } catch (IOException | NullPointerException e) {
            System.out.println("❌ Lỗi tải FXML: " + fxmlFile);
            e.printStackTrace();
        }
    }

    public static void openPopup(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(StageManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage popup = new Stage();
            popup.initStyle(StageStyle.UTILITY);
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle(title);
            popup.setScene(new Scene(root));
            popup.setResizable(false);

            popup.showAndWait();
        } catch (IOException e) {
            System.out.println("❌ Không thể mở popup: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void openPopupChat(String fxmlPath, String title, Consumer<Object> controllerConsumer) {
        try {
            FXMLLoader loader = new FXMLLoader(StageManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController(); // Lấy controller từ FXML
            controllerConsumer.accept(controller); // Truyền controller vào lambda
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.out.println("❌ Lỗi khi mở popup: " + e.getMessage());
        }
    }
}
