package com.example.cnetcoffee.Controller.Admin;

import com.example.cnetcoffee.Model.Message;
import com.example.cnetcoffee.Model.User;
import com.example.cnetcoffee.utils.SessionManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

public class ChatAdminController {

    @FXML
    private VBox chatBox;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextField messageField;

    private int receiverId;
    private int sessionId;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void setReceiverInfo(int receiverId, int sessionId) {
        this.receiverId = receiverId;
        this.sessionId = sessionId;
        connectSocket();
        loadChatHistory();
    }

    private void connectSocket() {
        try {
            socket = new Socket("localhost", 12345);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            Thread receiveThread = new Thread(() -> {
                try {
                    while (true) {
                        Message message = (Message) in.readObject();
                        Platform.runLater(() -> addMessageToChat(message));
                    }
                } catch (Exception e) {
                    System.out.println("❌ Lỗi nhận tin nhắn ở admin: " + e.getMessage());
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

        } catch (IOException e) {
            System.out.println("❌ Không thể kết nối tới server chat (admin): " + e.getMessage());
        }
    }

    @FXML
    void sendMessage(ActionEvent event) {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty()) {
            User sender = SessionManager.getCurrentUser();
            if (sender == null) return;

            Message message = new Message(
                    sender.getUserId(),
                    receiverId,
                    sender.getUsername(),
                    sender.getRole(),
                    msg,
                    LocalDateTime.now(),
                    sessionId
            );

            try {
                out.writeObject(message);
                out.flush();
                messageField.clear();
                // KHÔNG gọi addMessageToChat(message) ở đây!
            } catch (IOException e) {
                System.out.println("❌ Không thể gửi tin nhắn (admin): " + e.getMessage());
            }
        }
    }


    private void addMessageToChat(Message message) {
        Text text = new Text(message.getComputerName() + " (" + message.getUsername() + "): " + message.getContent());
        TextFlow textFlow = new TextFlow(text);
        textFlow.setPadding(new Insets(8));
        textFlow.setMaxWidth(240);

        HBox messageContainer = new HBox(textFlow);
        messageContainer.setMaxWidth(Double.MAX_VALUE);

        boolean isOwnMessage = message.getSenderId() == SessionManager.getCurrentUser().getUserId();
        messageContainer.setAlignment(isOwnMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        textFlow.setStyle(isOwnMessage
                ? "-fx-background-color: #dcf8c6; -fx-background-radius: 15; -fx-font-size: 14;"
                : "-fx-background-color: #ffffff; -fx-background-radius: 15; -fx-font-size: 14;");

        chatBox.getChildren().add(messageContainer);
        scrollPane.layout();
        scrollPane.setVvalue(1.0);
    }

    private void loadChatHistory() {
        int userId = receiverId; // userId của user (không phải admin)
        java.util.List<Message> messages = new com.example.cnetcoffee.dao.MessageDAO()
                .getConversationWithUserInSession(userId, sessionId);
        for (Message msg : messages) {
            addMessageToChat(msg);
        }
    }

}
