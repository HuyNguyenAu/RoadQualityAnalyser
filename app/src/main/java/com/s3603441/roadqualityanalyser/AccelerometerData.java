package com.s3603441.roadqualityanalyser;

public class AccelerometerData {
    private float x;
    private float y;
    private float z;
    private long timeCreated;

    public AccelerometerData(final float x, final float y, final float z, final long timeCreated) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timeCreated = timeCreated;
    }

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public long getTimeCreated() {
        return timeCreated;
    }
}
