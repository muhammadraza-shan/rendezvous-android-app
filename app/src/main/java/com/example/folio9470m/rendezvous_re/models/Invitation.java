package com.example.folio9470m.rendezvous_re.models;

public class Invitation {
    private String meetupID;
    private String InviterName;

    public Invitation(){
    }

    public Invitation(String meetupID, String inviterName) {
        this.meetupID = meetupID;
        InviterName = inviterName;
    }

    public String getMeetupID() {
        return meetupID;
    }

    public void setMeetupID(String meetupID) {
        this.meetupID = meetupID;
    }

    public String getInviterName() {
        return InviterName;
    }

    public void setInviterName(String inviterName) {
        InviterName = inviterName;
    }
}
