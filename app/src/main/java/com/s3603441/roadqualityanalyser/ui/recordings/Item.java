package com.s3603441.roadqualityanalyser.ui.recordings;

public class Item {
    private String dateTime;
    private int warnings;

    public Item(final String dateTime, final int warnings) {
        setDateTime(dateTime);
        setWarnings(warnings);
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
}