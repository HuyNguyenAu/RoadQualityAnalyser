package com.s3603441.roadqualityanalyser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
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
    private Button button_start_stop;

    // Accelerometer.
    private SensorManager sensorManager;
    private Sensor accelerometer;
    // Accelerometer data.
    private int d;
    private long lastUpdated;
    float smoothing;
    private List<Float> rawAccelX;
    private List<Float> filteredAccelX;
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
        button_start_stop = findViewById(R.id.button_start_stop);

        d = 2;
        lastUpdated = 0l;
        smoothing = 1;
        rawAccelX = new ArrayList<>();
        filteredAccelX = new ArrayList<>();
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
                textView_timer.setText(String.valueOf(smoothing));
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
    }

    private int getWindowSize(final int d) {
        return d + 2;
    }

    private boolean initialLimit(final int size, final int d) {
        final int windowSize = getWindowSize(d);
        boolean allow = false;

        if (size >= windowSize) {
            allow = true;
        }

        return allow;
    }

    private float windowFilter(final List<Float> data, final int d) {
        final int windowSize = getWindowSize(d);
        final int offset = data.size() - windowSize;
        float result = 0f;

        for (int j = offset; j < windowSize + offset; j++) {
            if (j % windowSize == 0) {
                continue;
            }

            result += (Math.abs(data.get(j) - data.get(j - 1))) / Float.valueOf(d);
        }

        return result;
    }

    private float lowPassFilter(final float oldValue, final float newValue, final float smoothing,
                                long delta) {
        return oldValue + (newValue - oldValue) / (smoothing / Float.valueOf(delta));
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
//                ILineDataSet dataSetY = lineData.getDataSetByIndex(1);
//                ILineDataSet dataSetZ = lineData.getDataSetByIndex(2);

                // If the line data sets do not exist, then create a new one.
                if (dataSetX == null) {
                    dataSetX = createDataSet("X", Color.RED);
                    lineData.addDataSet(dataSetX);
                }
//                if (dataSetY == null) {
//                    dataSetY = createDataSet("Y", Color.BLUE);
//                    lineData.addDataSet(dataSetY);
//                }
//                if (dataSetZ == null) {
//                    dataSetZ = createDataSet("Z", Color.GREEN);
//                    lineData.addDataSet(dataSetZ);
//                }

                rawAccelX.add(event.values[0]);

                // Low pass filter.
                if (rawAccelX.size() > 1) {
                    final float oldValue = rawAccelX.get(rawAccelX.size() - 2);
                    final float newValue = rawAccelX.get(rawAccelX.size() - 1);
                    long delta = System.currentTimeMillis() - lastUpdated;

                    if (rawAccelX.size() == 2) {
                        delta = 1;
                    }

                    filteredAccelX.add(lowPassFilter(oldValue, newValue, smoothing, delta));
                    lastUpdated = System.currentTimeMillis();
                }

                // Added a new entry into the data set.
//                lineData.addEntry(new Entry(dataSetX.getEntryCount(), event.values[0]), 0);
//                lineData.addEntry(new Entry(dataSetY.getEntryCount(), event.values[1]), 1);
//                lineData.addEntry(new Entry(dataSetZ.getEntryCount(), event.values[2]), 2);

                // Update the line chart and move the new data into view.
                if (initialLimit(filteredAccelX.size(), d)) {
                    lineData.addEntry(new Entry(dataSetX.getEntryCount(), windowFilter(filteredAccelX, d)), 0);
                    lineData.notifyDataChanged();
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
