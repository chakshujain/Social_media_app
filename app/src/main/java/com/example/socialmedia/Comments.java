package com.example.socialmedia;

public class Comments {
    public String date,uid,time,comment,username;

    public Comments(){

    }

    public Comments(String date, String uid, String time, String comment, String username) {
        this.date = date;
        this.uid = uid;
        this.time = time;
        this.comment = comment;
        this.username = username;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
