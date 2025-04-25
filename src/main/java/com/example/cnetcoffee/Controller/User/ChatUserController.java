package com.example.cnetcoffee.Controller.User;

import com.example.cnetcoffee.Model.Message;
import com.example.cnetcoffee.Model.User;
import com.example.cnetcoffee.dao.MessageDAO;
import com.example.cnetcoffee.utils.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class ChatUserController implements Initializable {

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.receiverId = 1;
        this.sessionId = SessionManager.getCurrentSessionId();

        if (sessionId > 0) {
            connectSocket();
            loadChatHistory();

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                Platform.runLater(() -> {
                    loadChatHistory(); // Reload chat history
                });
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        }
        messageField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    sendMessage(new ActionEvent()); // Gọi phương thức sendMessage
                    break;
                default:
                    break;
            }
        });
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
        this.sessionId = SessionManager.getCurrentSessionId();
        connectSocket();
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
                        Platform.runLater(() -> {
                            loadChatHistory();
                        });
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
        Platform.runLater(() -> {
            chatBox.getChildren().clear();
            List<Message> messages = new MessageDAO().getConversationWithUserInSession(
                    SessionManager.getCurrentUser().getId(),
                    sessionId
            );

            // Sắp xếp theo thời gian gửi
            messages.sort(Comparator.comparing(Message::getSentTime));

            for (Message msg : messages) {
                addMessageToChat(msg);
            }
            scrollPane.layout();
            scrollPane.setVvalue(1.0);
        });
    }
}