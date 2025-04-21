package com.example.cnetcoffee.Model;

import java.time.LocalDate;

public class DailyRevenue {
    private LocalDate date;
    private double dailyRevenue;

    public DailyRevenue(LocalDate date, double dailyRevenue) {
        this.date = date;
        this.dailyRevenue = dailyRevenue;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getDailyRevenue() {
        return dailyRevenue;
    }

    public void setDailyRevenue(double dailyRevenue) {
        this.dailyRevenue = dailyRevenue;
    }
}
