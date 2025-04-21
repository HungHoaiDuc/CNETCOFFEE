package com.example.cnetcoffee.Controller.Admin;

import com.example.cnetcoffee.Model.RevenueRecord;
import com.example.cnetcoffee.dao.StatisticalRevenueDAO;
import com.example.cnetcoffee.utils.StageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;

public class StatisticalRevenueController {

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TableView<RevenueRecord> revenueTable;
    @FXML private TableColumn<RevenueRecord, String> colMachine;
    @FXML private TableColumn<RevenueRecord, String> colStartTime;
    @FXML private TableColumn<RevenueRecord, String> colEndTime;
    @FXML private TableColumn<RevenueRecord, String> colType;
    @FXML private TableColumn<RevenueRecord, String> colTotal;
    @FXML private Label totalSessionsLabel;

    private ObservableList<RevenueRecord> revenueList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colMachine.setCellValueFactory(new PropertyValueFactory<>("machine"));
        colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        revenueTable.setItems(revenueList);

        typeComboBox.getItems().clear();
        typeComboBox.getItems().addAll("Tất cả", "NORMAL", "VIP");
        typeComboBox.setValue("Tất cả");

        loadRevenueData();
    }

    @FXML
    private void goToHome(ActionEvent event) {
        StageManager.switchScene("/com/example/cnetcoffee/admin/home-admin.fxml", "Admin Dashboard", true);
    }

    @FXML
    private void filterData() {
        loadRevenueData();
    }

    @FXML
    private void loadRevenueData() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        String selectedType = typeComboBox.getValue();

        // Lấy dữ liệu từ DAO và cập nhật table view
        List<RevenueRecord> records = StatisticalRevenueDAO.getRevenueRecordsByType(fromDate, toDate, selectedType);
        revenueList.setAll(records);

        // Cập nhật tổng số phiên (total sessions)
        updateTotalSessions(records);
    }

    private void updateTotalSessions(List<RevenueRecord> records) {
        int totalSessions = records.size();
        totalSessionsLabel.setText(String.valueOf(totalSessions));
        System.out.println("🔢 Tổng số phiên: " + totalSessions);
    }
}
