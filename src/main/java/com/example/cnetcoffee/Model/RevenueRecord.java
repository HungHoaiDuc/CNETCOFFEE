package com.example.cnetcoffee.Model;

public class RevenueRecord {
    private final int sessionId;
    private final String machine;
    private final String startTime;
    private final String endTime;
    private final String type;
    private final String total;

    public RevenueRecord(int sessionId, String machine, String startTime, String endTime, String type, String total) {
        this.sessionId = sessionId;
        this.machine = machine;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.total = total;
    }

    public int getSessionId() {
        return sessionId;
    }

    public String getMachine() {
        return machine;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getType() {
        return type;
    }

    public String getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "RevenueRecord{" +
                "sessionId=" + sessionId +
                ", machine='" + machine + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", type='" + type + '\'' +
                ", total='" + total + '\'' +
                '}';
    }
}
