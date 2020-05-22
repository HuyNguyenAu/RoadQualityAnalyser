package com.s3603441.roadqualityanalyser.ui.recordings;

public class Item {
    private String dateTime;
    private int warnings;
    private double latitude;
    private double longitude;

    public Item(final String dateTime, final int warnings, final double latitude, final double longitude) {
        setDateTime(dateTime);
        setWarnings(warnings);
        setLatitude(latitude);
        setLongitude(longitude);
    }

    public int getWarnings() {
        return warnings;
    }

    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }
}