package com.example.folio9470m.rendezvous_re.models;

import com.example.folio9470m.rendezvous_re.models.LatLong;

public class LocationVote {
    private String placeName;
    private String placeID;
    private LatLong coordinates;
    private int voteCount;
    private String key;
    private String address;

    public LocationVote() {
    }

    public LocationVote(String placeName, String placeID, LatLong coordinates, String key, String address) {
        this.placeName = placeName;
        this.placeID = placeID;
        this.coordinates = coordinates;
        voteCount = 0;
        this.key = key;
        this.address = address;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public LatLong getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLong coordinates) {
        this.coordinates = coordinates;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
