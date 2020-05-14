package com.s3603441.roadqualityanalyser.db.accelerometer;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "accelerometer")
public class Accelerometer {
    @PrimaryKey
    private int id;

    @ColumnInfo(name="datetime")
    private String datetime;

    @ColumnInfo(name="x")
    private float x;

    @ColumnInfo(name="y")
    private float y;

    @ColumnInfo(name="z")
    private float z;

    @ColumnInfo(name="time_created")
    private long timeCreated;

    @ColumnInfo(name="current_time")
    private String currentTime;

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(final String datetime) {
        this.datetime = datetime;
    }

    public float getX() {
        return x;
    }

    public void setX(final float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(final float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(final float z) {
        this.z = z;
    }

    public long getTimeCreated() {
        return this.timeCreated;
    }

    public void setTimeCreated(final long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getCurrentTime() {
        return this.currentTime;
    }

    public void setCurrentTime(final String currentTime) {
        this.currentTime = currentTime;
    }
}
