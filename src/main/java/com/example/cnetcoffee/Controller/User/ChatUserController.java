package com.example.cnetcoffee.Controller.User;

import com.example.cnetcoffee.Model.Message;
import com.example.cnetcoffee.Model.User;
import com.example.cnetcoffee.dao.MessageDAO;
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
import java.util.List;

public class ChatUserController {

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

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
        this.sessionId = SessionManager.getCurrentSessionId();
        connectSocket();
    }

    @FXML
    public void initialize() {
        this.receiverId = 1;
        this.sessionId = SessionManager.getCurrentSessionId();

        if (sessionId > 0) {
            connectSocket();
            loadChatHistory();
        }
    }

    private void connectSocket() {
        try {
            socket = new Socket("localhost", 12345); // IP Server nếu chạy trên LAN
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            Thread receiveThread = new Thread(() -> {
                try {
                    while (true) {
                        Message message = (Message) in.readObject();
                        Platform.runLater(() -> addMessageToChat(message));
                    }
                } catch (Exception e) {
                    System.out.println("❌ Lỗi nhận tin nhắn ở user: " + e.getMessage());
                }
            });
            receiveThread.setDaemon(true);
            receiveThread.start();

        } catch (IOException e) {
            System.out.println("❌ Không thể kết nối tới server chat: " + e.getMessage());
        }
    }

    @FXML
    void sendMessage(ActionEvent event) {
        String msg = messageField.getText().trim();
        if (!msg.isEmpty()) {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                System.out.println("❌ Không có người dùng hiện tại hoặc đang ở chế độ guest!");
                return;
            }

            Message message = new Message(
                    currentUser.getId(),
                    receiverId,
                    currentUser.getUsername(),
                    "Máy " + SessionManager.getAssignedComputerId(),
                    msg,
                    LocalDateTime.now(),
                    sessionId
            );

            try {
                out.writeObject(message);
                out.flush();
                messageField.clear();
                // Không gọi addMessageToChat ở đây!
            } catch (IOException e) {
                System.out.println("❌ Không thể gửi tin nhắn: " + e.getMessage());
            }
        }
    }

    private void addMessageToChat(Message message) {
        Text text = new Text(message.getUsername() + ": " + message.getContent());
        TextFlow textFlow = new TextFlow(text);

        HBox messageContainer = new HBox(textFlow);
        messageContainer.setMaxWidth(Double.MAX_VALUE);
        textFlow.setPadding(new Insets(8));
        textFlow.setMaxWidth(240);
        textFlow.setStyle("-fx-background-color: " +
                (message.getUsername().equals(SessionManager.getCurrentUser().getUsername()) ? "#dcf8c6;" : "#ffffff;") +
                "-fx-background-radius: 12;" +
                "-fx-font-size: 14;");

        messageContainer.setAlignment(
                message.getUsername().equals(SessionManager.getCurrentUser().getUsername())
                        ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT
        );

        chatBox.getChildren().add(messageContainer);
        scrollPane.layout();
        scrollPane.setVvalue(1.0);
    }

    public void loadChatHistory() {
        List<Message> messages = new MessageDAO().getConversationWithUserInSession(
                SessionManager.getCurrentUser().getId(), // userId hiện tại
                sessionId // sessionId hiện tại
        );
        for (Message msg : messages) {
            addMessageToChat(msg);
        }
    }

}
