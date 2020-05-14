package com.s3603441.roadqualityanalyser.ui.analytics;

import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineDataSet;
import com.s3603441.roadqualityanalyser.db.accelerometer.Accelerometer;

import java.util.List;

public class AnalyticsViewModel extends ViewModel {
    private LineChart lineChart;
    private List<Accelerometer> data;

    // Create a new line data set.
    public LineDataSet createDataSet(final String label, final int color) {
        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(color);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    public LineChart getLineChart() {
        return this.lineChart;
    }

    public void setLineChart(final LineChart lineChart) {
        this.lineChart = lineChart;
    }

    public List<Accelerometer> getData() {
        return this.data;
    }

    public void setData(final List<Accelerometer> data) {
        this.data = data;
    }
}
