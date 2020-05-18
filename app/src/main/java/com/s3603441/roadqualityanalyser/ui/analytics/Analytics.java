package com.s3603441.roadqualityanalyser.ui.analytics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.room.Room;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.snackbar.Snackbar;
import com.s3603441.roadqualityanalyser.R;
import com.s3603441.roadqualityanalyser.db.AppDatabase;
import com.s3603441.roadqualityanalyser.db.accelerometer.Accelerometer;
import com.s3603441.roadqualityanalyser.ui.home.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class Analytics extends AppCompatActivity {
    private AnalyticsViewModel analyticsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        analyticsViewModel =
                ViewModelProviders.of(this).get(AnalyticsViewModel.class);

        initUI();
        loadData(getWindow().getDecorView());
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initUI() {
        analyticsViewModel.setLineChart((LineChart) findViewById(R.id.linechart));
        analyticsViewModel.getLineChart().getDescription().setEnabled(false);
        analyticsViewModel.getLineChart().getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        analyticsViewModel.getLineChart().getXAxis().setLabelRotationAngle(45f);
    }

    private void initLineChart(final List<Accelerometer> data) {
        if (analyticsViewModel.getLineChart().getData() == null) {
            analyticsViewModel.getLineChart().setData(new LineData());
        }

        analyticsViewModel.setxAxisData(new ArrayList<String>());

        LineData lineData = analyticsViewModel.getLineChart().getData();
        ILineDataSet dataSetX = lineData.getDataSetByIndex(0);
        ILineDataSet dataSetY = lineData.getDataSetByIndex(1);
        ILineDataSet dataSetZ = lineData.getDataSetByIndex(2);

        // If the line data sets do not exist, then create a new one.
        if (dataSetX == null) {
            dataSetX = analyticsViewModel.createDataSet("X", Color.RED);
            lineData.addDataSet(dataSetX);
        }
        if (dataSetY == null) {
            dataSetY = analyticsViewModel.createDataSet("Y", Color.BLUE);
            lineData.addDataSet(dataSetY);
        }
        if (dataSetZ == null) {
            dataSetZ = analyticsViewModel.createDataSet("Z", Color.GREEN);
            lineData.addDataSet(dataSetZ);
        }

       for (int i = 0; i < data.size(); i++) {
           lineData.addEntry(new Entry(dataSetX.getEntryCount(),
                   analyticsViewModel.getData().get(i).getX()), 0);
           lineData.addEntry(new Entry(dataSetY.getEntryCount(),
                   analyticsViewModel.getData().get(i).getY()), 1);
           lineData.addEntry(new Entry(dataSetZ.getEntryCount(),
                   analyticsViewModel.getData().get(i).getZ()), 2);
           lineData.notifyDataChanged();

           analyticsViewModel.getxAxisData().add(data.get(i).getCurrentTime());
           analyticsViewModel.getLineChart().notifyDataSetChanged();
       }

       // Source: https://stackoverflow.com/questions/45320457/how-to-set-string-value-of-xaxis-in-mpandroidchart
        analyticsViewModel.getLineChart().getXAxis().setValueFormatter(new IndexAxisValueFormatter(analyticsViewModel.getxAxisData()));
        analyticsViewModel.getLineChart().notifyDataSetChanged();
    }


    private void loadData(final View root) {
        final Snackbar loadingSnackbar = Snackbar.make(root, "Loading data...", Snackbar.LENGTH_INDEFINITE);
        final GetDataTask getDataTask = new GetDataTask(root, getIntent().getExtras().getString("dateTime"), loadingSnackbar);
        loadingSnackbar.show();
        getDataTask.execute(root.getContext().getApplicationContext());
    }

    private class GetDataTask extends AsyncTask<Context, Void, List<Accelerometer>> {
        private View root;
        private String dateTime;
        private Snackbar loadingSnackbar;

        public GetDataTask(final View root, final String dateTime, final Snackbar loadingSnackbar) {
            this.root = root;
            this.dateTime = dateTime;
            this.loadingSnackbar = loadingSnackbar;
        }

        @Override
        protected List<Accelerometer> doInBackground(Context... contexts) {
            final AppDatabase appDatabase = Room.databaseBuilder(contexts[0],
                    AppDatabase.class, "data").build();

            return appDatabase.accelerometerDao().getData(dateTime);
        }

        @Override
        protected void onPostExecute(final List<Accelerometer> data) {
            analyticsViewModel.setData(data);
            initLineChart(analyticsViewModel.getData());
            loadingSnackbar.dismiss();
        }
    }
}
