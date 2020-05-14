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
import com.s3603441.roadqualityanalyser.R;
import com.s3603441.roadqualityanalyser.db.AppDatabase;
import com.s3603441.roadqualityanalyser.db.accelerometer.Accelerometer;
import com.s3603441.roadqualityanalyser.db.settings.Setting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements SensorEventListener {

    private HomeViewModel homeViewModel;

    // Tells the line chart to start or stop plotting.
    // This is used in another thread.
    private volatile boolean start;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_home, container, false);

        initUI(root);
        initPlotThread();
        initSettings(root, root.getContext().getApplicationContext());

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

        homeViewModel.setRawData(new ArrayList<Accelerometer>());
        homeViewModel.setFilteredData(new ArrayList<Accelerometer>());
        homeViewModel.setWindowFilteredData(new ArrayList<Accelerometer>());
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
                buttonStartStopClicked(root);
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

    public void initSettings(final View root, final Context context) {
        final InitSettingsTask initSettingsTask = new InitSettingsTask(root);
        initSettingsTask.execute(context);
    }

    public void buttonStartStopClicked(final View root) {
        if (!start) {
            // Load the smoothing factor, window size, and threshold in a separate thread.
            final StartTask startTask = new StartTask();
            startTask.execute(root.getContext());
        } else {
            final Snackbar savingSnackbar = Snackbar.make(root, "Saving data...", Snackbar.LENGTH_INDEFINITE);
            final StopTask stopTask = new StopTask(root, root.getContext(), savingSnackbar);
            savingSnackbar.show();
            start = false;
            stopTask.execute(homeViewModel.getWindowFilteredData());
        }
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

                Accelerometer rawData = new Accelerometer();
                rawData.setX(event.values[0]);
                rawData.setY(event.values[1]);
                rawData.setZ(event.values[2]);
                homeViewModel.getRawData().add(rawData);

                // Low pass filter.
                if (homeViewModel.getRawData().size() > 1) {
                    final Accelerometer oldValue = homeViewModel.getRawData()
                            .get(homeViewModel.getRawData().size() - 2);
                    final Accelerometer newValue = homeViewModel.getRawData()
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

                    Accelerometer filteredData = new Accelerometer();
                    filteredData.setX(filteredX);
                    filteredData.setY(filteredY);
                    filteredData.setZ(filteredZ);
                    homeViewModel.getFilteredData().add(filteredData);
                }

                // Update the line chart and move the new data into view.
                if (homeViewModel.initialLimit(homeViewModel.getFilteredData().size(), homeViewModel.getWindowSize())) {
                    final Accelerometer windowFiltedData = new Accelerometer();
                    windowFiltedData.setTimeCreated(System.currentTimeMillis());
                    windowFiltedData.setDatetime(homeViewModel.getDateTimeStartCreated());
                    windowFiltedData.setX(homeViewModel.windowFilter(homeViewModel.getFilteredData(), homeViewModel.getWindowSize(), 0));
                    windowFiltedData.setY(homeViewModel.windowFilter(homeViewModel.getFilteredData(), homeViewModel.getWindowSize(), 1));
                    windowFiltedData.setZ(homeViewModel.windowFilter(homeViewModel.getFilteredData(), homeViewModel.getWindowSize(), 2));

                    homeViewModel.getWindowFilteredData().add(windowFiltedData);

                    lineData.addEntry(new Entry(dataSetX.getEntryCount(),
                            windowFiltedData.getX()), 0);
                    lineData.addEntry(new Entry(dataSetY.getEntryCount(),
                            windowFiltedData.getY()), 1);
                    lineData.addEntry(new Entry(dataSetZ.getEntryCount(),
                            windowFiltedData.getZ()), 2);
                    lineData.notifyDataChanged();

                    // Threshold testing.
                    if (homeViewModel.getDetectionValue(homeViewModel.getWindowFilteredData(), homeViewModel.getWindowSize(), 1,
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

    private class InitSettingsTask extends AsyncTask<Context, Void, Void> {
        private View root;

        public InitSettingsTask(final View root) {
            this.root = root;
        }

        @Override
        protected Void doInBackground(Context... contexts) {
            final AppDatabase appDatabase = Room.databaseBuilder(contexts[0],
                    AppDatabase.class, "settings").build();

            List<Setting> settings = new ArrayList<>();
            settings.add(new Setting("smoothing", 2));
            settings.add(new Setting("window_size", 2));
            settings.add(new Setting("threshold", 600));
            settings.add(new Setting("sensitivity", 100));

            initSettings(settings, appDatabase);
            appDatabase.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        public void initSettings(final List<Setting> settings, final AppDatabase appDatabase) {
            for (int i = 0; i < settings.size(); i++) {
                Setting setting = settings.get(i);
                final Setting oldSetting = appDatabase.settingDao().getSetting(setting.getName());

                if (oldSetting == null) {
                    setting.setId(i);
                    appDatabase.settingDao().addSetting(setting);
                }
            }
        }
    }

    private class StartTask extends AsyncTask<Context, Void, List<Setting>> {
        @Override
        protected List<Setting> doInBackground(Context... contexts) {
            final AppDatabase appDatabase = Room.databaseBuilder(contexts[0],
                    AppDatabase.class, "settings").build();
            final List<Setting> settings = appDatabase.settingDao().getAll();
            appDatabase.close();

            return settings;
        }

        @Override
        protected void onPostExecute(final List<Setting> settings) {
            for (final Setting setting : settings) {
                final String name = setting.getName();
                final int value = setting.getValue();

                // Set the smoothing factor, window size, and threshold from saved settings.
                if (name.equalsIgnoreCase("smoothing")) {
                    homeViewModel.setSmoothing(value);
                } else if (name.equalsIgnoreCase("window_size")) {
                    homeViewModel.setWindowSize(value);
                } else if (name.equalsIgnoreCase("threshold")) {
                    homeViewModel.setThreshold(value);
                } else if (name.equalsIgnoreCase("sensitivity")) {
                    homeViewModel.setSensitivity(value);
                }
            }

            homeViewModel.getRawData().clear();
            homeViewModel.getFilteredData().clear();
            homeViewModel.getThresholdData().clear();
            homeViewModel.getLineChart().clear();

            // https://stackoverflow.com/questions/13515168/android-time-in-iso-8601
            SimpleDateFormat currentDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            homeViewModel.setDateTimeStartCreated(currentDateTime.format(new Date()));

            // Toggle start to be true or false.
            start = true;
            // Set the start_stop button text accordingly.
            homeViewModel.getButtonStartStop().setText("Stop");

            super.onPostExecute(settings);
        }
    }

    private class StopTask extends AsyncTask<List<Accelerometer>, Void, Void> {
        private View root;
        private Context context;
        private Snackbar savingSnackbar;

        public StopTask(final View root, final Context context, final Snackbar savingSnackbar) {
            this.root = root;
            this.context = context;
            this.savingSnackbar = savingSnackbar;
        }

        @Override
        protected Void doInBackground(List<Accelerometer>... lists) {
            final AppDatabase appDatabase = Room.databaseBuilder(context,
                    AppDatabase.class, "data").build();
            final List<Accelerometer> currentData = appDatabase.accelerometerDao().getAll();
            final List<Accelerometer> newData = lists[0];
            int initialIndex = 0;

            if (currentData.size() > 0) {
                initialIndex = currentData.size();
            }

            for (int i = 0; i < newData.size(); i++) {
                newData.get(i).setId(i + initialIndex);
                appDatabase.accelerometerDao().addData(newData.get(i));
            }

            appDatabase.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            savingSnackbar.dismiss();
            Snackbar.make(root, "Data saved!", Snackbar.LENGTH_SHORT).show();

            // Set the start_stop button text accordingly.
            homeViewModel.getButtonStartStop().setText("Start");
            super.onPostExecute(aVoid);
        }
    }
}
