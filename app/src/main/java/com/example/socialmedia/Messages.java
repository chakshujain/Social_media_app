package com.example.socialmedia;

public class Messages {
    String date,from,message,type,time;
    public Messages(){

    }

    public Messages(String date, String from, String message, String type, String time) {
        this.date = date;
        this.from = from;
        this.message = message;
        this.type = type;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
