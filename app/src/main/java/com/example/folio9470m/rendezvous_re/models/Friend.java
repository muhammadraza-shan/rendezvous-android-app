package com.example.folio9470m.rendezvous_re.models;

public class Friend {
    private String name;
    private boolean online;
    private String id;

    public Friend() {
    }

    public Friend(String name, String id,  boolean online) {
        this.name = name;
        this.online = online;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
