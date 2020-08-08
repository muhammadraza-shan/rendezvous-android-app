package com.example.folio9470m.rendezvous_re.models;

import java.util.ArrayList;

public class User {
    private String name;
    private String email;
    private String userID;
    private String type;
    private String phone;
    private String facebookid;

    public User(){

    }

    public User(String name, String email, String userID, String type) {
        this.name = name;
        this.email = email;
        this.userID = userID;
        this.type = type;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFacebookid() {
        return facebookid;
    }

    public void setFacebookid(String facebookid) {
        this.facebookid = facebookid;
    }
}
