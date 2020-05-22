package com.s3603441.roadqualityanalyser.ui.analytics_map;

import androidx.lifecycle.ViewModel;

import com.s3603441.roadqualityanalyser.db.accelerometer.Accelerometer;

import java.util.List;

public class AnalyticsMapViewModel extends ViewModel {
    private List<Accelerometer> data;

    public List<Accelerometer> getData() {
        return this.data;
    }

    public void setData(final List<Accelerometer> data) {
        this.data = data;
    }
}
