package com.s3603441.roadqualityanalyser.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.room.Room;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.snackbar.Snackbar;
import com.s3603441.roadqualityanalyser.AccelerometerData;
import com.s3603441.roadqualityanalyser.R;
import com.s3603441.roadqualityanalyser.db.AppDatabase;
import com.s3603441.roadqualityanalyser.db.settings.Setting;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment implements SensorEventListener {

    private HomeViewModel homeViewModel;

    // Tells the line chart to start or stop plotting.
    // This is used in another thread.
    private volatile boolean start;
    private volatile boolean done;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
       final View root = inflater.inflate(R.layout.fragment_home, container, false);

        initUI(root);
        initPlotThread();
        initSettings(root.getContext().getApplicationContext());

        return root;
    }

    public void initUI(final View root) {
        // Register controls.
        homeViewModel.setTextViewTimer((TextView) root.findViewById(R.id.textView_timer));
        homeViewModel.setLineChart((LineChart) root.findViewById(R.id.linechart));
        homeViewModel.setButtonStartStop((Button) root.findViewById(R.id.button_start_stop));

        homeViewModel.setWindowSize(2);
        homeViewModel.setSmoothing(2);
        homeViewModel.setThreshold(600);

        homeViewModel.setRawData(new ArrayList<AccelerometerData>());
        homeViewModel.setFilteredData(new ArrayList<AccelerometerData>());
        homeViewModel.setThresholdData(new ArrayList<Float>());

        // Setup line chart visuals and interactions.
        homeViewModel.getLineChart().setTouchEnabled(false);
        homeViewModel.getLineChart().setDragEnabled(false);
        homeViewModel.getLineChart().setPinchZoom(false);
        homeViewModel.getLineChart().setScaleXEnabled(false);
        homeViewModel.getLineChart().setScaleYEnabled(false);
        homeViewModel.getLineChart().getDescription().setEnabled(false);
        homeViewModel.getLineChart().setDrawGridBackground(false);
        homeViewModel.getLineChart().getXAxis().setDrawGridLines(false);
        homeViewModel.getLineChart().getXAxis().setDrawLabels(false);
        homeViewModel.getLineChart().setData(new LineData());

        // By default, the app does not plot data.
        homeViewModel.setPlot(false);
        start = false;

        // Setup accelerometer.
        homeViewModel.setSensorManager((SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE));
        homeViewModel.setAccelerometer(homeViewModel.getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        homeViewModel.getSensorManager().registerListener(this, homeViewModel.getAccelerometer(), SensorManager.SENSOR_DELAY_GAME);

        homeViewModel.getButtonStartStop().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonStartStopClicked(root.getContext());
            }
        });
    }

    public void initPlotThread() {
        // Interrupt the thread if it exists.
        if (homeViewModel.getThread() != null) {
            homeViewModel.getThread().interrupt();
        }

        // Create a new thread to control when to plot the accelerometer data.
        homeViewModel.setThread(new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // Only plot when start is true.
                    if (start) {
                        doTask();
                    }
                }
            }

            // Set plot to true, then delay. Plot is set to false later.
            // It can only be set to true here.
            public void doTask() {
                homeViewModel.setPlot(true);

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
        homeViewModel.getThread().start();
    }

    public void initSettings(final Context context) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                final AppDatabase appDatabase = Room.databaseBuilder(context,
                        AppDatabase.class, "data").build();

                insertSetting("smoothing", 2, appDatabase);
                insertSetting("window_size", 2, appDatabase);
                insertSetting("threshold", 600, appDatabase);
                insertSetting("sensitivity", 25, appDatabase);

                appDatabase.close();
            }
        });
    }

    public void insertSetting(final String name, final int value,
                            final AppDatabase appDatabase) {
        final boolean exists = appDatabase.settingDao().getSetting(name) != null;
        final int count =  appDatabase.settingDao().getAll().size();

        if (exists) {
            return;
        }

        Setting setting = new Setting();
        setting.setId(count);
        setting.setSetting(name);
        setting.setValue(value);

        appDatabase.settingDao().addSetting(setting);
    }

    public void buttonStartStopClicked(final Context context) {
        // Load the smoothing factor, window size, and threshold in a separate thread.
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                final AppDatabase appDatabase = Room.databaseBuilder(context,
                        AppDatabase.class, "data").build();

                done = false;

                // Set the smoothing factor, window size, and threshold from saved settings.
                homeViewModel.setSmoothing(
                        appDatabase.settingDao().getSetting("smoothing").getValue());
                homeViewModel.setWindowSize(
                        appDatabase.settingDao().getSetting("window_size").getValue());
                homeViewModel.setThreshold(
                        appDatabase.settingDao().getSetting("threshold").getValue());
                homeViewModel.setSensitivity(
                        appDatabase.settingDao().getSetting("sensitivity").getValue());

                appDatabase.close();
                done = true;
            }
        };

        // Only load the settings when the user starts the recording.
       if (!start) {
           synchronized (task) {
               AsyncTask.execute(task);
           }

           homeViewModel.getRawData().clear();
           homeViewModel.getFilteredData().clear();
           homeViewModel.getThresholdData().clear();
           homeViewModel.getLineChart().clear();

           // https://stackoverflow.com/questions/13515168/android-time-in-iso-8601
           SimpleDateFormat currentDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
           homeViewModel.setDateTimeStart(currentDateTime.format(new Date()));
       }

       // Wait until the task is done.
        while (!done) {
        }

        // Toggle start to be true or false.
        start = start ? false : true;

        // Set the start_stop button text accordingly.
        homeViewModel.getButtonStartStop().setText(start ? "Stop" : "Start");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Plot the data only once.
            if (homeViewModel.getPlot()) {
                if (homeViewModel.getLineChart().getData() == null) {
                    homeViewModel.getLineChart().setData(new LineData());
                }

                LineData lineData = homeViewModel.getLineChart().getData();

                // Try to get the data sets.
                ILineDataSet dataSetX = lineData.getDataSetByIndex(0);
                ILineDataSet dataSetY = lineData.getDataSetByIndex(1);
                ILineDataSet dataSetZ = lineData.getDataSetByIndex(2);

                // If the line data sets do not exist, then create a new one.
                if (dataSetX == null) {
                    dataSetX = homeViewModel.createDataSet("X", Color.RED);
                    lineData.addDataSet(dataSetX);
                }
                if (dataSetY == null) {
                    dataSetY = homeViewModel.createDataSet("Y", Color.BLUE);
                    lineData.addDataSet(dataSetY);
                }
                if (dataSetZ == null) {
                    dataSetZ = homeViewModel.createDataSet("Z", Color.GREEN);
                    lineData.addDataSet(dataSetZ);
                }

                homeViewModel.getRawData().add(new AccelerometerData(event.values[0], event.values[1],
                        event.values[2], System.currentTimeMillis()));

                // Low pass filter.
                if (homeViewModel.getRawData().size() > 1) {
                    final AccelerometerData oldValue = homeViewModel.getRawData()
                            .get(homeViewModel.getRawData().size() - 2);
                    final AccelerometerData newValue = homeViewModel.getRawData()
                            .get(homeViewModel.getRawData().size() - 1);
                    long delta = newValue.getTimeCreated() - oldValue.getTimeCreated();

                    if (homeViewModel.getRawData().size() == 2) {
                        delta = 1;
                    }

                    final float filteredX = homeViewModel.lowPassFilter(oldValue.getX(), newValue.getX(),
                            homeViewModel.getSmoothing(), delta);
                    final float filteredY = homeViewModel.lowPassFilter(oldValue.getY(), newValue.getY(),
                            homeViewModel.getSmoothing(), delta);
                    final float filteredZ = homeViewModel.lowPassFilter(oldValue.getZ(), newValue.getZ(),
                            homeViewModel.getSmoothing(), delta);

                    homeViewModel.getFilteredData().add(new AccelerometerData(filteredX, filteredY, filteredZ,
                            System.currentTimeMillis()));
                }

                // Update the line chart and move the new data into view.
                if (homeViewModel.initialLimit(homeViewModel.getFilteredData().size(), homeViewModel.getWindowSize())) {
                    lineData.addEntry(new Entry(dataSetX.getEntryCount(),
                            homeViewModel.windowFilter(homeViewModel.getFilteredData(), homeViewModel.getWindowSize(), 0)), 0);
                    lineData.addEntry(new Entry(dataSetY.getEntryCount(),
                            homeViewModel.windowFilter(homeViewModel.getFilteredData(), homeViewModel.getWindowSize(), 1)), 1);
                    lineData.addEntry(new Entry(dataSetZ.getEntryCount(),
                            homeViewModel.windowFilter(homeViewModel.getFilteredData(), homeViewModel.getWindowSize(), 2)), 2);
                    lineData.notifyDataChanged();

                    // Threshold testing.
                    if (homeViewModel.getDetectionValue(homeViewModel.getFilteredData(), homeViewModel.getWindowSize(), 0,
                            homeViewModel.getSensitivity() / 100.0f) >= homeViewModel.getThreshold()) {
                        Snackbar.make(getView(), "Danger!", Snackbar.LENGTH_SHORT).show();
                    }
                    homeViewModel.getLineChart().notifyDataSetChanged();
                    homeViewModel.getLineChart().setVisibleXRangeMaximum(150);
                    homeViewModel.getLineChart().moveViewToX(lineData.getEntryCount());
                }

                // Stop plotting and wait until plotting is allowed.
                homeViewModel.setPlot(false);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onPause() {
        super.onPause();
        // Mark the thread for garbage collection.
        if (homeViewModel.getThread() != null) {
            homeViewModel.getThread().interrupt();
        }
        // Disable the sensor when the app is in paused state.
        homeViewModel.getSensorManager().unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // When the app has paused or is destroyed, we need to re-register the accelerometer.
        homeViewModel.getSensorManager().registerListener(this, homeViewModel.getAccelerometer(), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onDetach() {
        // Disable the sensor when the app is in destroyed state.
        homeViewModel.getSensorManager().unregisterListener(this);
        // Mark the thread for garbage collection.
        homeViewModel.getThread().interrupt();
        super.onDetach();
    }
}
