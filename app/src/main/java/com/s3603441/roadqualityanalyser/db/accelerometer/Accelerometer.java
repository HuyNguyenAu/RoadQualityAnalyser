package com.s3603441.roadqualityanalyser.db.accelerometer;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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
}
