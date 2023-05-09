package com.example.catfeederapp;

public class Schedule {


    private String sched_token;
    private String sched_time;
    private String sched_repeat;
    private String body_weight;
    private String total_grams;
    private String date_created;

    private boolean isEnabled;

    private boolean isDone;

    public Schedule(String sched_token,
                    String sched_time,
                    String sched_repeat,
                    String body_weight,
                    String total_grams,
                    String date_created,
                    boolean isEnabled,
                    boolean isDone) {

        this.sched_token = sched_token;
        this.sched_time = sched_time;
        this.sched_repeat = sched_repeat;
        this.body_weight = body_weight;
        this.total_grams = total_grams;
        this.date_created = date_created;
        this.isEnabled = isEnabled;
        this.isDone = isDone;
    }

    public Schedule() {
    }

    public String getSched_token() {
        return sched_token;
    }

    public void setSched_token(String sched_token) {
        this.sched_token = sched_token;
    }

    public String getSched_time() {
        return sched_time;
    }

    public void setSched_time(String sched_time) {
        this.sched_time = sched_time;
    }

    public String getSched_repeat() {
        return sched_repeat;
    }

    public void setSched_repeat(String sched_repeat) {
        this.sched_repeat = sched_repeat;
    }

    public String getBody_weight() {
        return body_weight;
    }

    public void setBody_weight(String body_weight) {
        this.body_weight = body_weight;
    }

    public String getTotal_grams() {
        return total_grams;
    }

    public void setTotal_grams(String total_grams) {
        this.total_grams = total_grams;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }


}
