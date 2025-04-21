package com.example.cnetcoffee.Controller.Admin;

import com.example.cnetcoffee.Model.ChatSummary;
import com.example.cnetcoffee.Model.Message;
import com.example.cnetcoffee.Model.User;
import com.example.cnetcoffee.dao.MessageDAO;
import com.example.cnetcoffee.utils.SessionManager;
import com.example.cnetcoffee.utils.StageManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TableChatAdminController {

    @FXML
    private TableView<ChatSummary> chatTable;

    @FXML
    private TableColumn<ChatSummary, String> computerColumn;

    @FXML
    private TableColumn<ChatSummary, String> usernameColumn;

    @FXML
    private TableColumn<ChatSummary, String> messageColumn;

    private ObservableList<ChatSummary> chatSummaries = FXCollections.observableArrayList();

    private Timeline timeline;

    private static final Set<Integer> readSessions = new HashSet<>();

    final int adminUserId = 1;

    @FXML
    public void initialize() {
        computerColumn.setCellValueFactory(new PropertyValueFactory<>("computerName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("lastMessageContent"));

        loadChatSummaries();

        chatTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ChatSummary selectedSummary = chatTable.getSelectionModel().getSelectedItem();
                if (selectedSummary != null) {
                    openChatWindow(selectedSummary);
                }
            }
        });

        timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> loadChatSummaries()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void loadChatSummaries() {
        chatSummaries.clear();

        // Các phiên đang hoạt động (online)
        List<Integer> allSessionIds = SessionManager.getAllActiveSessionIds();
        for (int sessionId : allSessionIds) {
            User user = SessionManager.getUserBySessionId(sessionId); // Cần đảm bảo SessionManager có hàm này
            if (user != null) {
                ChatSummary summary = new ChatSummary(
                        user.getUserId(),
                        user.getUsername(),
                        "Máy " + SessionManager.getAssignedComputerId(user.getUserId()),
                        sessionId,
                        new ArrayList<>()
                );
                summary.setNewMessage(false); // Có thể cập nhật trạng thái này qua socket trong tương lai
                chatSummaries.add(summary);
            }
        }

        // Các phiên đã kết thúc (lấy từ DB)
        List<Message> dbMessages = new MessageDAO().getAllMessagesFromDB();
        Map<Integer, List<Message>> groupedMessages = dbMessages.stream()
                .collect(Collectors.groupingBy(Message::getSessionId));

        for (Map.Entry<Integer, List<Message>> entry : groupedMessages.entrySet()) {
            int sessionId = entry.getKey();
            List<Message> messages = entry.getValue();
            if (!messages.isEmpty()) {
                Message lastMessage = messages.get(messages.size() - 1);
                ChatSummary summary = new ChatSummary(
                        lastMessage.getSenderId(),
                        lastMessage.getUsername(),
                        lastMessage.getComputerName(),
                        sessionId,
                        messages
                );
                // Đánh dấu là "chưa trả lời" nếu tin nhắn cuối cùng không phải của admin
                summary.setNewMessage(lastMessage.getSenderId() != adminUserId);
                chatSummaries.add(summary);
            }
        }

        FXCollections.sort(chatSummaries, (a, b) -> {
            if (a.isNewMessage() == b.isNewMessage()) {
                // Sắp xếp theo thời gian tin nhắn cuối cùng (mới nhất lên trên)
                LocalDateTime aTime = a.getMessages().isEmpty() ? LocalDateTime.MIN : a.getMessages().get(a.getMessages().size() - 1).getSentTime();
                LocalDateTime bTime = b.getMessages().isEmpty() ? LocalDateTime.MIN : b.getMessages().get(b.getMessages().size() - 1).getSentTime();
                return bTime.compareTo(aTime);
            }
            return Boolean.compare(b.isNewMessage(), a.isNewMessage());
        });

        chatTable.setItems(chatSummaries);
        chatTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ChatSummary item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.isNewMessage()) {
                    setStyle("-fx-background-color: orange;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void openChatWindow(ChatSummary summary) {
        int userId = summary.getUserId();
        int sessionId = summary.getSessionId();

        readSessions.add(sessionId);
        summary.setNewMessage(false);
        chatTable.refresh();

        StageManager.openPopupChat("/com/example/cnetcoffee/admin/chat_admin.fxml", "Chat với " + summary.getUsername(), controller -> {
            ChatAdminController chatController = (ChatAdminController) controller;
            chatController.setReceiverInfo(userId, sessionId);
        });
    }
}
