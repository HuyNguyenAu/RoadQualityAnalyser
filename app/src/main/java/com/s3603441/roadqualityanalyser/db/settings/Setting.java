package com.s3603441.roadqualityanalyser.db.settings;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "setting")
public class Setting {
    @PrimaryKey
    private int id;

    @ColumnInfo(name="setting")
    private String setting;

    @ColumnInfo(name="value")
    private int value;

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(final String setting) {
        this.setting = setting;
    }

    public int getValue() {
        return value;
    }

    public void setValue(final int value) {
        this.value = value;
    }
}
