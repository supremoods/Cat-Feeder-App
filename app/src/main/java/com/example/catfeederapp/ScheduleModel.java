package com.example.catfeederapp;

public class ScheduleModel {
    private String token;
    private String time;
    private String days;
    private Boolean isEnabled;

    public ScheduleModel(String token, String time, String days, Boolean isEnabled) {
        this.token = token;
        this.time = time;
        this.days = days;
        this.isEnabled = isEnabled;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public void setEnabled(Boolean enabled) {
        isEnabled = enabled;
    }
}
