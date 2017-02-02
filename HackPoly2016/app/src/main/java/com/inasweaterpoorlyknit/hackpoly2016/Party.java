package com.inasweaterpoorlyknit.hackpoly2016;

import java.io.Serializable;

/**
 * Stores data for each party
 * Created by raymond on 6/15/16.
 */

public class Party implements Serializable{
    private int partyID;
    private String partyName;
    private String hostName;
    private double latitude;
    private double longitude;

    public Party()
    {
        partyID = 0;
        partyName = null;
        hostName = null;
        latitude = 0;
        longitude = 0;
    }
    public Party(int partyID, String partyName, String hostName)
    {
        this.partyID = partyID;
        this.partyName = partyName;
        this.hostName = hostName;
    }

    public Party(int partyID, String partyName, String hostName, double latitude, double longitude)
    {
        this.partyID = partyID;
        this.partyName = partyName;
        this.hostName = hostName;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public int getPartyID() {
        return partyID;
    }
    public void setPartyID(int partyID) {
        this.partyID = partyID;
    }
    public String getPartyName() {
        return partyName;
    }
    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }
    public String getHostName() {
        return hostName;
    }
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
