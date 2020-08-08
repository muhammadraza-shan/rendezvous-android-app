package com.example.folio9470m.rendezvous_re.models;

import android.graphics.Bitmap;

import com.example.folio9470m.rendezvous_re.models.LatLong;

import java.util.ArrayList;

public class Meetup {
    protected String name;
    protected String placeID;
    protected String location;
    protected boolean isPublic;
    protected LatLong coordinates;
    protected String date;
    protected String time;
    protected String creator;
    protected Bitmap locationImage;
    protected String meetupID;
    protected int locationMethod;
    private String address;
    private String tags;
    private String imagepath;
    private long meetupDateTime;



    public Meetup(){

    }

    public String getMeetupID() {
        return meetupID;
    }

    public void setMeetupID(String meetupID) {
        this.meetupID = meetupID;
    }

    public Bitmap getLocationImage() {
        return locationImage;
    }

    public void setLocationImage(Bitmap locationImage) {
        this.locationImage = locationImage;
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public Meetup(String name, String location, boolean isPublic) {
        this.name = name;
        this.location = location;
        this.isPublic = isPublic;
    }

    public Meetup(String name, boolean isPublic, String date, String time, String creator) {
        this.name = name;
        this.isPublic = isPublic;
        this.date = date;
        this.time = time;
        this.creator = creator;
    }

    public Meetup(String name, String location, boolean isPublic, LatLong coordinates, String date, String time, String creator) {
        this.name = name;
        this.location = location;
        this.isPublic = isPublic;
        this.coordinates = coordinates;
        this.date = date;
        this.time = time;
        this.creator = creator;
    }


    public LatLong getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLong coordinates) {
        this.coordinates = coordinates;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public int getLocationMethod() {
        return locationMethod;
    }

    public void setLocationMethod(int locationMethod) {
        this.locationMethod = locationMethod;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getImagepath() {
        return imagepath;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }

    public long getMeetupDateTime() {
        return meetupDateTime;
    }

    public void setMeetupDateTime(long meetupDateTime) {
        this.meetupDateTime = meetupDateTime;
    }
}
