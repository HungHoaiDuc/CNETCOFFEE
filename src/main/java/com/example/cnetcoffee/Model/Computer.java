package com.example.cnetcoffee.Model;

public class Computer {
    private int id;
    private String name;
    private String status;
    private String type;
    private boolean isAvailable;
    private String socketPort;

    public Computer(int id, String name, String status, String type, boolean isAvailable, String socketPort) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.type = type;
        this.isAvailable = isAvailable;
        this.socketPort = socketPort;
    }

    // Getter và Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getSocketPort() {
        return socketPort;
    }

    public void setSocketPort(String socketPort) {
        this.socketPort = socketPort;
    }

    @Override
    public String toString() {
        return "Computer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
