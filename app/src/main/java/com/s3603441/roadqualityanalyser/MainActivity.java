package com.s3603441.roadqualityanalyser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    // Controls.
    private TextView textView_timer;
    private LineChart lineChart;
    private SeekBar seekBar_smoothing;
    private SeekBar seekBar_windowSize;
    private SeekBar seekBar_threshold;
    private Button button_start_stop;

    // DEBUG
    private TextView textView_smoothing;
    private TextView textView_windowSize;
    private TextView textView_threshold;

    // Accelerometer.
    private SensorManager sensorManager;
    private Sensor accelerometer;
    // Accelerometer data.
    private int d;
    private float smoothing;
    private int threshold;
    private List<AccelerometerData> rawAccelData;
    private List<AccelerometerData> filteredAccelData;
    // Line chart control.
    private Thread thread;
    private boolean plot;
    // Tells the line chart to start or stop plotting.
    // This is used in another thread.
    private volatile boolean start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register controls.
        textView_timer = findViewById(R.id.textView_timer);
        lineChart = findViewById(R.id.linechart);
        seekBar_smoothing = findViewById(R.id.seekBar_smoothing);
        seekBar_windowSize = findViewById(R.id.seekBar_windowSize);
        seekBar_threshold = findViewById(R.id.seekBar_threshold);
        button_start_stop = findViewById(R.id.button_start_stop);
        // DEBUG
        textView_smoothing = findViewById(R.id.textView_smoothing);
        textView_windowSize = findViewById(R.id.textView_windowSize);
        textView_threshold = findViewById(R.id.textView_threshold);

        d = 2;
        smoothing = 1;
        rawAccelData = new ArrayList<>();
        filteredAccelData = new ArrayList<>();
        // By default, the app does not plot data.
        plot = false;
        start = false;

        // Setup accelerometer.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        // Setup line chart visuals and interactions.
        lineChart.setTouchEnabled(false);
        lineChart.setDragEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setScaleXEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setData(new LineData());

        button_start_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonStartStopClicked(view);
            }
        });

        // Interrupt the thread if it exists.
        if (thread != null){
            thread.interrupt();
        }

        // Source: https://stackoverflow.com/questions/33349424/android-seekbar-changing-the-value
        seekBar_smoothing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress <= 0) {
                    progress = 1;
                }

                smoothing = Float.valueOf(progress);
                // DEBUG
                textView_smoothing.setText("Smoothing: " + smoothing);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        seekBar_windowSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 2) {
                    progress = 2;
                }

                if (progress % 2 != 0) {
                    progress = progress - 1;
                }

                d = progress;
                // DEBUG
                textView_windowSize.setText("Window Size: " + d);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        seekBar_threshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress <= 0) {
                    progress = 1;
                }

                threshold = progress;
                // DEBUG
                textView_threshold.setText("Threshold: " + threshold);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // Create a new thread to control when to plot the accelerometer data.
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    // Only plot when start is true.
                    if (start) {
                        doTask();
                    }
                }
            }

            // Set plot to true, then delay. Plot is set to false later.
            // It can only be set to true here.
            public void doTask() {
                plot = true;

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        // DEBUG
        textView_smoothing.setText("Smoothing: " + smoothing);
        textView_windowSize.setText("Window Size: " + d);
        textView_threshold.setText("Threshold: " + threshold);
        textView_timer.setText("Noice!");
    }

    // Calculate the number of elements to process from the window size d.
    private int getNumberOfElements(final int d) {
        return d + 2;
    }

    // Determine when the window filter can be applied based on the window size.
    private boolean initialLimit(final int size, final int d) {
        final int numberOfElements = getNumberOfElements(d);
        boolean allow = false;

        if (size >= numberOfElements) {
            allow = true;
        }

        return allow;
    }

    // Amplify the changes in acceleration larger.
    private float windowFilter(final List<AccelerometerData> data, final int d, final int index) {
        // The number of data points to process.
        final int numberOfElements = getNumberOfElements(d);
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
            else  if (index == 1) {
                k = data.get(j).getY();
                k1 = data.get(j - 1).getY();
            }
            // Z data point.
            else  if (index == 2) {
                k = data.get(j).getZ();
                k1 = data.get(j - 1).getZ();
            }

            // Calculate the window filtered value.
            result += (Math.abs(k - k1)) / Float.valueOf(d);
        }

        return result;
    }

    // A simple low pass filter.
    // Source: http://phrogz.net/js/framerate-independent-low-pass-filter.html
    private float lowPassFilter(final float oldValue, final float newValue, final float smoothing,
                                long delta) {
        return oldValue + (newValue - oldValue) / (smoothing / Float.valueOf(delta));
    }

    //TODO TESTING: Confirm if this works...
    private float getDetectionValue(final List<AccelerometerData> data, final int d, final int index,
                            final float threshold) {
        final int numberOfElements = getNumberOfElements(d);
        final int offset = data.size() - numberOfElements;

        float maxValue = 0;
        float minValue = 0;
        float average = 0;

        for (int j = offset; j < numberOfElements + offset; j++) {
            // The current data point.
            float k = 0f;

            // X data point.
            if (index == 0) {
                k = data.get(j).getX();
            }
            // Y data point.
            else  if (index == 1) {
                k = data.get(j).getY();
            }
            // Z data point.
            else  if (index == 2) {
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
    private LineDataSet createDataSet(final String label, final int color) {
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

    public void buttonStartStopClicked(View view) {
        // Toggle start to be true or false.
        start = start ? false : true;
        // Set the start_stop button text accordingly.
        button_start_stop.setText(start ? "Stop" : "Start");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Plot the data only once.
            if(plot){
                LineData lineData = lineChart.getData();

                // Try to get the data sets.
                ILineDataSet dataSetX = lineData.getDataSetByIndex(0);
                ILineDataSet dataSetY = lineData.getDataSetByIndex(1);
                ILineDataSet dataSetZ = lineData.getDataSetByIndex(2);

                // If the line data sets do not exist, then create a new one.
                if (dataSetX == null) {
                    dataSetX = createDataSet("X", Color.RED);
                    lineData.addDataSet(dataSetX);
                }
                if (dataSetY == null) {
                    dataSetY = createDataSet("Y", Color.BLUE);
                    lineData.addDataSet(dataSetY);
                }
                if (dataSetZ == null) {
                    dataSetZ = createDataSet("Z", Color.GREEN);
                    lineData.addDataSet(dataSetZ);
                }

                rawAccelData.add(new AccelerometerData(event.values[0], event.values[1],
                        event.values[2], System.currentTimeMillis()));

                // Low pass filter.
                if (rawAccelData.size() > 1) {
                    final AccelerometerData oldValue = rawAccelData.get(rawAccelData.size() - 2);
                    final AccelerometerData newValue = rawAccelData.get(rawAccelData.size() - 1);
                    long delta = newValue.getTimeCreated() - oldValue.getTimeCreated();

                    if (rawAccelData.size() == 2) {
                        delta = 1;
                    }

                    final float filteredX = lowPassFilter(oldValue.getX(), newValue.getX(),
                            smoothing, delta);
                    final float filteredY = lowPassFilter(oldValue.getY(), newValue.getY(),
                            smoothing, delta);
                    final float filteredZ = lowPassFilter(oldValue.getZ(), newValue.getZ(),
                            smoothing, delta);

                    filteredAccelData.add(new AccelerometerData(filteredX, filteredY, filteredZ,
                            System.currentTimeMillis()));
                }

                // Update the line chart and move the new data into view.
                if (initialLimit(filteredAccelData.size(), d)) {
                    lineData.addEntry(new Entry(dataSetX.getEntryCount(),
                            windowFilter(filteredAccelData, d, 0)), 0);
                    lineData.addEntry(new Entry(dataSetY.getEntryCount(),
                            windowFilter(filteredAccelData, d, 1)), 1);
                    lineData.addEntry(new Entry(dataSetZ.getEntryCount(),
                            windowFilter(filteredAccelData, d, 2)), 2);
                    lineData.notifyDataChanged();

                    // Threshold testing.
                    // DEBUG
//                    textView_timer.setText(String.valueOf(getDetectionValue(filteredAccelData, d,
//                            0, Float.valueOf(threshold) / 100)));
                    if (getDetectionValue(filteredAccelData, d, 0,
                            Float.valueOf(25) / 100) >= threshold) {
                        // DEBUG
                        textView_timer.setText("Warning!");
                    }

                    lineChart.notifyDataSetChanged();
                    lineChart.setVisibleXRangeMaximum(150);
                    lineChart.moveViewToX(lineData.getEntryCount());
                }

                // Stop plotting and wait until plotting is allowed.
                plot = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        // Mark the thread for garbage collection.
        if (thread != null) {
            thread.interrupt();
        }
        // Disable the sensor when the app is in paused state.
        sensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        // When the app has paused or is destroyed, we need to re-register the accelerometer.
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onDestroy() {
        // Disable the sensor when the app is in destroyed state.
        sensorManager.unregisterListener(this);
        // Mark the thread for garbage collection.
        thread.interrupt();
        super.onDestroy();
    }
}
