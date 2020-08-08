package com.example.folio9470m.rendezvous_re;

import android.app.Application;
import android.content.Context;


import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.leakcanary.LeakCanary;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Rendezvous extends Application {

    private String userName ;
    private String meetupID;
    private String userID;
    private ArrayList<String> currentMeetups = new ArrayList<>();

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMeetupID() {
        return meetupID;
    }

    public void setMeetupID(String meetupID) {
        this.meetupID = meetupID;
    }

    public ArrayList<String> getCurrentMeetups() {
        return currentMeetups;
    }

    public void setCurrentMeetups(ArrayList<String> currentMeetups) {
        this.currentMeetups = currentMeetups;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        userName = "";
    }
}
