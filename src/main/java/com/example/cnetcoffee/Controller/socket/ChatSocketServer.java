package com.example.cnetcoffee.Controller.socket;

import com.example.cnetcoffee.Model.Message;
import com.example.cnetcoffee.dao.MessageDAO;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatSocketServer {
    private static final int PORT = 12345;
    private static final Map<Integer, ObjectOutputStream> clientOutputStreams = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("🚀 Chat Server đang chạy tại cổng " + PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            new ClientHandler(socket).start();
        }
    }

    static class ClientHandler extends Thread {
        private final Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private int userId;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    Message message = (Message) in.readObject();
                    this.userId = message.getSenderId();
                    clientOutputStreams.put(userId, out);

                    System.out.println("📨 Tin nhắn từ " + message.getUsername() + ": " + message.getContent());

                    ObjectOutputStream receiverOut = clientOutputStreams.get(message.getReceiverId());
                    if (receiverOut != null) {
                        receiverOut.writeObject(message);
                        receiverOut.flush();
                    }

                    // Gửi lại cho chính người gửi (nếu khác người nhận)
                    ObjectOutputStream senderOut = clientOutputStreams.get(message.getSenderId());
                    if (senderOut != null && senderOut != receiverOut) {
                        senderOut.writeObject(message);
                        senderOut.flush();
                    }

                    // Ghi DB nếu muốn ở đây
                    new MessageDAO().saveMessage(message);
                }
            } catch (Exception e) {
                System.out.println("❌ Kết nối bị lỗi hoặc đóng: " + e.getMessage());
            } finally {
                try {
                    if (userId != 0) clientOutputStreams.remove(userId);
                    socket.close();
                } catch (IOException ignored) {}
            }
        }
    }
}