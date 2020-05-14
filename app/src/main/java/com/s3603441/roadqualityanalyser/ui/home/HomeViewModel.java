package com.s3603441.roadqualityanalyser.ui.home;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineDataSet;
import com.s3603441.roadqualityanalyser.db.accelerometer.Accelerometer;

import java.util.List;

public class HomeViewModel extends ViewModel {
    // Controls.
    private TextView textView_timer;
    private LineChart lineChart;
    private Button button_start_stop;

    // Accelerometer.
    private SensorManager sensorManager;
    private Sensor accelerometer;

    // Accelerometer data.
    private String dateTimeCreated;
    private int windowSize;
    private float smoothing;
    private int threshold;
    private float sensitivity;
    private List<Accelerometer> rawData;
    private List<Accelerometer> filteredData;
    private List<Float> thresholdData;

    // Line chart control.
    private Thread thread;
    private boolean plot;

    public HomeViewModel() {
    }

    // Calculate the number of elements to process from the window size d.
    public int getNumberOfElements(final int windowSize) {
        return windowSize + 2;
    }

    // Determine when the window filter can be applied based on the window size.
    public boolean initialLimit(final int size, final int windowSize) {
        final int numberOfElements = getNumberOfElements(windowSize);
        boolean allow = false;

        if (size >= numberOfElements) {
            allow = true;
        }

        return allow;
    }

    // Amplify the changes in acceleration larger.
    public float windowFilter(final List<Accelerometer> data, final int windowSize, final int index) {
        // The number of data points to process.
        final int numberOfElements = getNumberOfElements(windowSize);
        // Calculate the position of the start of the window.
        // This is where the first data point of the window is.
        final int offset = data.size() - numberOfElements;
        // The window filtered value.
        float result = 0f;

        // Start from the first data point of the window and loop to the final data point of the
        // window to determine the window filtered value.
        for (int j = offset; j < numberOfElements + offset; j++) {
            // Since the window filter depends on two data points, the current value and the previous
            // value. That means we need to skip the first value.
            if (j == offset) {
                continue;
            }

            // The current data point.
            float k = 0f;
            // The previous data point.
            float k1 = 0f;

            // X data point.
            if (index == 0) {
                k = data.get(j).getX();
                k1 = data.get(j - 1).getX();
            }
            // Y data point.
            else if (index == 1) {
                k = data.get(j).getY();
                k1 = data.get(j - 1).getY();
            }
            // Z data point.
            else if (index == 2) {
                k = data.get(j).getZ();
                k1 = data.get(j - 1).getZ();
            }

            // Calculate the window filtered value.
            result += (Math.abs(k - k1)) / Float.valueOf(windowSize);
        }

        return result;
    }

    // A simple low pass filter.
    // Source: http://phrogz.net/js/framerate-independent-low-pass-filter.html
    public float lowPassFilter(final float oldValue, final float newValue, final float smoothing,
                               long delta) {
        return oldValue + (newValue - oldValue) / (smoothing / Float.valueOf(delta));
    }

    // TODO
    public float getDetectionValue(final List<Accelerometer> data, final int d, final int index,
                                   final float threshold) {
        final int numberOfElements = getNumberOfElements(d);
        final int offset = data.size() - numberOfElements;

        float maxValue = 0;
        float minValue = 0;
        float average = 0;

        for (int j = offset; j < numberOfElements + offset; j++) {
            // The current data point.
            float k = 0f;

            // Z data point.
            if (index == 0) {
                k = data.get(j).getX();
            }
            // Z data point.
            else if (index == 1) {
                k = data.get(j).getY();
            }
            // Z data point.
            else if (index == 2) {
                k = data.get(j).getZ();
            }

            if (maxValue < k) {
                maxValue = k;
            }

            if (minValue > k) {
                minValue = k;
            }

            average += k;
        }

        average /= Float.valueOf(numberOfElements);

        return ((maxValue - minValue) * threshold) + average;
    }

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

    public TextView getTextViewTimer() {
        return this.textView_timer;
    }

    public void setTextViewTimer(final TextView textView_timer) {
        this.textView_timer = textView_timer;
    }

    public LineChart getLineChart() {
        return this.lineChart;
    }

    public void setLineChart(final LineChart lineChart) {
        this.lineChart = lineChart;
    }

    public Button getButtonStartStop() {
        return this.button_start_stop;
    }

    public void setButtonStartStop(final Button button_start_stop) {
        this.button_start_stop = button_start_stop;
    }

    public SensorManager getSensorManager() {
        return this.sensorManager;
    }

    public void setSensorManager(final SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }

    public Sensor getAccelerometer() {
        return this.accelerometer;
    }

    public void setAccelerometer(final Sensor accelerometer) {
        this.accelerometer = accelerometer;
    }

    public String getDateTimeStartCreated() {
        return this.dateTimeCreated;
    }

    public void setDateTimeStartCreated(final String dateTimeCreated) {
        this.dateTimeCreated = dateTimeCreated;
    }

    public int getWindowSize() {
        return this.windowSize;
    }

    public void setWindowSize(final int windowSize) {
        this.windowSize = windowSize;
    }

    public float getSmoothing() {
        return this.smoothing;
    }

    public void setSmoothing(final float smoothing) {
        this.smoothing = smoothing;
    }

    public int getThreshold() {
        return this.threshold;
    }

    public void setThreshold(final int threshold) {
        this.threshold = threshold;
    }

    public float getSensitivity() {
        return this.sensitivity;
    }

    public void setSensitivity(final float sensitivity) {
        this.sensitivity = sensitivity;
    }

    public List<Accelerometer> getRawData() {
        return this.rawData;
    }

    public void setRawData(final List<Accelerometer> rawData) {
        this.rawData = rawData;
    }

    public List<Accelerometer> getFilteredData() {
        return this.filteredData;
    }

    public void setFilteredData(final List<Accelerometer> filteredData) {
        this.filteredData = filteredData;
    }

    public List<Float> getThresholdData() {
        return this.thresholdData;
    }

    public void setThresholdData(final List<Float> thresholdData) {
        this.thresholdData = thresholdData;
    }

    public Thread getThread() {
        return this.thread;
    }

    public void setThread(final Thread thread) {
        this.thread = thread;
    }

    public boolean getPlot() {
        return this.plot;
    }

    public void setPlot(final boolean plot) {
        this.plot = plot;
    }
}