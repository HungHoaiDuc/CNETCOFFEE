package com.example.cnetcoffee;

import com.example.cnetcoffee.Controller.socket.ClientSocketHandler;
import com.example.cnetcoffee.dao.ComputerDB;
import com.example.cnetcoffee.utils.SessionManager;
import com.example.cnetcoffee.utils.StageManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MainUser extends Application{

    @Override
    public void start(Stage stage) throws IOException {

        // Gán mã máy (computer_id) cho máy hiện tại
        String localIpAddress = InetAddress.getLocalHost().getHostAddress();
        int thisComputerId = ComputerDB.getComputerIdByIp(localIpAddress);
        System.out.println("📡 IP của máy: " + InetAddress.getLocalHost().getHostAddress());
        if (thisComputerId == -1) {
            System.out.println("❌ Không tìm thấy máy tương ứng với IP: " + localIpAddress);
            return;
        }

        SessionManager.setAssignedComputerId(thisComputerId);
        StageManager.setPrimaryStage(stage);
        StageManager.switchSceneLoginUser("/com/example/cnetcoffee/user/login_user.fxml", "C-NETCOFFEE!", false, "/img/LOGOC.png");

        ClientSocketHandler.startListeningFromAdmin();

        new Thread(() -> {
            try (Socket socket = new Socket("172.168.10.165", 12346);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                out.println("READY " + thisComputerId);
                System.out.println("Đã gửi yêu cầu tới Admin.");

            } catch (IOException e) {
                System.out.println("Không thể kết nối đến Admin Server.");
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        launch();
    }
}
