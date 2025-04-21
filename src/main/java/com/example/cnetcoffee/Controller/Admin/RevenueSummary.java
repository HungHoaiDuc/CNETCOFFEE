package com.example.cnetcoffee.Controller.Admin;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

import com.example.cnetcoffee.Model.RevenueData;
import com.example.cnetcoffee.dao.RevenueDAO;
import com.example.cnetcoffee.utils.StageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class RevenueSummary implements Initializable {

    @FXML
    private Button btn_back;

    @FXML
    private Button btn_search;

    @FXML
    private TableColumn<?, ?> col_PCnormal;

    @FXML
    private TableColumn<?, ?> col_PCvip;

    @FXML
    private TableColumn<?, ?> col_Service_revenue;

    @FXML
    private TableColumn<?, ?> col_date1;

    @FXML
    private TableColumn<?, ?> col_date2;

    @FXML
    private DatePicker datepicker1;

    @FXML
    private DatePicker datepicker2;

    @FXML
    private Label lb_PC;

    @FXML
    private Label lb_RevenueSummary;

    @FXML
    private Label lb_Service_revenue;

    @FXML
    private TableView<RevenueData> tableView;

    @FXML
    private TableView<RevenueData> tableView2;

    private RevenueDAO revenueDAO = new RevenueDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configure columns for service revenue table
        col_date1.setCellValueFactory(new PropertyValueFactory<>("date"));
        col_Service_revenue.setCellValueFactory(
                new PropertyValueFactory<>("serviceRevenue"));

        // Configure columns for PC revenue table
        col_date2.setCellValueFactory(new PropertyValueFactory<>("date"));
        col_PCnormal.setCellValueFactory(new PropertyValueFactory<>("pcNormalRevenue"));
        col_PCvip.setCellValueFactory(new PropertyValueFactory<>("pcVipRevenue"));

        // Set default dates
        datepicker1.setValue(LocalDate.now().minusDays(7));
        datepicker2.setValue(LocalDate.now());

        // Load data on initialization
        loadRevenueData();
    }

    @FXML
    private void loadRevenueData() {
        LocalDate startDate = datepicker1.getValue();
        LocalDate endDate = datepicker2.getValue();

        if (startDate != null && endDate != null) {
            List<RevenueData> revenueList = revenueDAO.getRevenueData(startDate, endDate);
            ObservableList<RevenueData> observableList =
                    FXCollections.observableArrayList(revenueList);

            // Update TableView for service revenue
            tableView.setItems(observableList);

            // Update TableView for PC revenue
            tableView2.setItems(observableList);

            // Calculate and display totals
            double totalRevenue = 0;
            double totalServiceRevenue = 0;
            double totalNormalPCRevenue = 0;
            double totalVipPCRevenue = 0;

            for (RevenueData data : revenueList) {
                totalServiceRevenue += data.getServiceRevenue();
                totalNormalPCRevenue += data.getPcNormalRevenue();
                totalVipPCRevenue += data.getPcVipRevenue();
            }

            totalRevenue = totalServiceRevenue + totalNormalPCRevenue + totalVipPCRevenue;

            lb_RevenueSummary.setText(String.format("%.2f vnđ", totalRevenue));
            lb_Service_revenue.setText(String.format("%.2f vnđ", totalServiceRevenue));
            lb_PC.setText(String.format("%.2f vnđ", totalNormalPCRevenue + totalVipPCRevenue));
        }
    }

    public void btn_search(ActionEvent actionEvent) {
        loadRevenueData();
    }

    @FXML
    void btn_back(ActionEvent event) {
        StageManager.switchScene("/com/example/cnetcoffee/admin/home-admin.fxml", "Admin Dashboard", true);
    }


}
