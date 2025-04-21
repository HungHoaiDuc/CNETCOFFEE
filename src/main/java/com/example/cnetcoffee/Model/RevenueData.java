package com.example.cnetcoffee.Model;

import java.time.LocalDate;

public class RevenueData {
    private LocalDate date;
    private double serviceRevenue;
    private double pcNormalRevenue;
    private double pcVipRevenue;

    public RevenueData(LocalDate date, double serviceRevenue, double pcNormalRevenue,
                       double pcVipRevenue) {
        this.date = date;
        this.serviceRevenue = serviceRevenue;
        this.pcNormalRevenue = pcNormalRevenue;
        this.pcVipRevenue = pcVipRevenue;
    }

    // Getters and setters
    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) { this.date = date; }

    public double getServiceRevenue() { return serviceRevenue; }

    public void setServiceRevenue(double serviceRevenue) {
        this.serviceRevenue = serviceRevenue;
    }

    public double getPcNormalRevenue() { return pcNormalRevenue; }

    public void setPcNormalRevenue(double pcNormalRevenue) {
        this.pcNormalRevenue = pcNormalRevenue;
    }

    public double getPcVipRevenue() { return pcVipRevenue; }

    public void setPcVipRevenue(double pcVipRevenue) {
        this.pcVipRevenue = pcVipRevenue;
    }
}