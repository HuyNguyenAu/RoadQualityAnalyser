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

    @ColumnInfo(name="detection_value_x")
    private float detectionValueX;

    @ColumnInfo(name="detection_value_y")
    private float detectionValueY;

    @ColumnInfo(name="detection_value_z")
    private float detectionValueZ;

    @ColumnInfo(name="warnings")
    private int warnings;

    @ColumnInfo(name="latitude")
    private double latitude;

    @ColumnInfo(name="longitude")
    private double longitude;

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

    public float getDetectionValueX() {
        return this.detectionValueX;
    }

    public void setDetectionValueX(final float detectionValueX) {
        this.detectionValueX = detectionValueX;
    }

    public float getDetectionValueY() {
        return this.detectionValueY;
    }

    public void setDetectionValueY(final float detectionValueY) {
        this.detectionValueY = detectionValueY;
    }

    public float getDetectionValueZ() {
        return this.detectionValueZ;
    }

    public void setDetectionValueZ(final float detectionValueZ) {
        this.detectionValueZ = detectionValueZ;
    }

    public int getWarnings() {
        return this.warnings;
    }

    public void setWarnings(final int warnings) {
        this.warnings = warnings;
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
