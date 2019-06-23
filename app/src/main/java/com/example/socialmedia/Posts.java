package com.example.socialmedia;

public class Posts {
    public String uid,description,time,date,fullname,postimage,profileimage;

    public Posts(String uid, String description, String time, String date, String fullname, String postimage, String profileimage) {
        this.uid = uid;
        this.description = description;
        this.time = time;
        this.date = date;
        this.fullname = fullname;
        this.postimage = postimage;
        this.profileimage = profileimage;
    }
    public Posts(){

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }
}
