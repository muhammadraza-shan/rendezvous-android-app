package com.example.folio9470m.rendezvous_re.models;

import com.example.folio9470m.rendezvous_re.models.LatLong;

import java.util.Date;

public class UserLocation {
    private LatLong location;
    private String name;
    private long locationTime;
    private String userID;

    public UserLocation() {
    }

    public UserLocation( String name,LatLong location, String userID) {
        this.location = location;
        this.name = name;
        locationTime = new Date().getTime();
        this.userID = userID;
    }

    public LatLong getLocation() {
        return location;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setLocation(LatLong location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLocationTime() {
        return locationTime;
    }

    public void setLocationTime(long locationTime) {
        this.locationTime = locationTime;
    }


}
