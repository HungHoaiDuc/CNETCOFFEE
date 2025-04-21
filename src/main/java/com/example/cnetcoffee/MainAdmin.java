package com.example.cnetcoffee;

import com.example.cnetcoffee.Controller.socket.ServerSocketHandler;
import com.example.cnetcoffee.dao.UserManagerDAO;
import com.example.cnetcoffee.utils.StageManager;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class MainAdmin extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        StageManager.setPrimaryStage(stage);
        UserManagerDAO.resetAllUserStatus();
        StageManager.switchScene("/com/example/cnetcoffee/admin/login.fxml", "C-NETCOFFEE!", true);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/LOGOC.png"))));
        ServerSocketHandler.startServer();

    }

    public static void main(String[] args) {
        launch();
    }
}