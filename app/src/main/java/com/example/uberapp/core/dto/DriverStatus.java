package com.example.uberapp.core.dto;

public class DriverStatus {
    private boolean online;

    public DriverStatus(boolean online) {
        this.online = online;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
