package com.example.cnetcoffee.Controller.socket;

import com.example.cnetcoffee.dao.ComputerDB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerSocketHandler {
    private static final int PORT = 12346; // Cổng server
    private static boolean running = true;
    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("🔵 Server đang chạy trên cổng " + PORT);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("🔗 Kết nối từ: " + clientSocket.getInetAddress());

                    threadPool.execute(() -> handleClient(clientSocket));
                }
            } catch (IOException e) {
                System.err.println("❌ Lỗi ServerSocket: " + e.getMessage());
            }
        }).start();
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String command = in.readLine(); // Nhận lệnh từ client
            System.out.println("📩 Nhận lệnh: " + command);

            if (command != null && command.startsWith("READY")){
                int computerId = Integer.parseInt(command.split(" ")[1]);
                ComputerDB.updateComputerStatus(computerId, "READY", true);
                out.println("SUCCESS");
                System.out.println("✅ Máy " + computerId + " đã sẵn sàng sử dụng");
            }else if (command != null && command.startsWith("TURN_ON")) {
                out.println("SUCCESS"); // Phản hồi thành công
                System.out.println("✅ Máy đã bật!");
            } else if (command != null && command.startsWith("TURN_OFF")) {
                out.println("SUCCESS");
                System.out.println("❌ Máy đã tắt!");
            } else if (command != null && command.startsWith("CHECK_STATUS")) {
                out.println("STATUS_OK"); // ✅ Đây là phản hồi mong đợi bên Client
                System.out.println("✅ Kiểm tra trạng thái thành công!");
            } else {
                out.println("UNKNOWN_COMMAND");
                System.out.println("⚠️ Lệnh không hợp lệ!");
            }
        } catch (IOException e) {
            System.err.println("❌ Lỗi khi xử lý client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("❌ Lỗi khi đóng kết nối: " + e.getMessage());
            }
        }
    }

    public static void stopServer() {
        running = false;
        threadPool.shutdown();
        System.out.println("🔴 Server đã dừng.");
    }
}
