package com.s3603441.roadqualityanalyser.ui.analytics_graph;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.snackbar.Snackbar;
import com.s3603441.roadqualityanalyser.R;
import com.s3603441.roadqualityanalyser.db.AppDatabase;
import com.s3603441.roadqualityanalyser.db.accelerometer.Accelerometer;
import com.s3603441.roadqualityanalyser.ui.recordings.RecordingsViewModel;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsGraph extends Fragment {

    private AnalyticsGraphViewModel analyticsGraphViewModel;

    public static AnalyticsGraph newInstance() {
        return new AnalyticsGraph();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View root =  inflater.inflate(R.layout.analytics_graph_fragment, container, false);

        initUI(root);
        loadData(root);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        analyticsGraphViewModel = new ViewModelProvider(this).get(AnalyticsGraphViewModel.class);
    }

    private void initUI(final View root) {
        analyticsGraphViewModel.setLineChart((LineChart) root.findViewById(R.id.linechart));
        analyticsGraphViewModel.getLineChart().getDescription().setEnabled(false);
        analyticsGraphViewModel.getLineChart().getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        analyticsGraphViewModel.getLineChart().getXAxis().setLabelRotationAngle(45f);
    }

    private void initLineChart(final List<Accelerometer> data) {
        if (analyticsGraphViewModel.getLineChart().getData() == null) {
            analyticsGraphViewModel.getLineChart().setData(new LineData());
        }

        analyticsGraphViewModel.setxAxisData(new ArrayList<String>());

        LineData lineData = analyticsGraphViewModel.getLineChart().getData();
        ILineDataSet dataSetX = lineData.getDataSetByIndex(0);
        ILineDataSet dataSetY = lineData.getDataSetByIndex(1);
        ILineDataSet dataSetZ = lineData.getDataSetByIndex(2);

        // If the line data sets do not exist, then create a new one.
        if (dataSetX == null) {
            dataSetX = analyticsGraphViewModel.createDataSet("X", Color.RED);
            lineData.addDataSet(dataSetX);
        }
        if (dataSetY == null) {
            dataSetY = analyticsGraphViewModel.createDataSet("Y", Color.BLUE);
            lineData.addDataSet(dataSetY);
        }
        if (dataSetZ == null) {
            dataSetZ = analyticsGraphViewModel.createDataSet("Z", Color.GREEN);
            lineData.addDataSet(dataSetZ);
        }

        for (int i = 0; i < data.size(); i++) {
            lineData.addEntry(new Entry(dataSetX.getEntryCount(),
                    analyticsGraphViewModel.getData().get(i).getX()), 0);
            lineData.addEntry(new Entry(dataSetY.getEntryCount(),
                    analyticsGraphViewModel.getData().get(i).getY()), 1);
            lineData.addEntry(new Entry(dataSetZ.getEntryCount(),
                    analyticsGraphViewModel.getData().get(i).getZ()), 2);
            lineData.notifyDataChanged();

            analyticsGraphViewModel.getxAxisData().add(data.get(i).getCurrentTime());
            analyticsGraphViewModel.getLineChart().notifyDataSetChanged();
        }

        // Source: https://stackoverflow.com/questions/45320457/how-to-set-string-value-of-xaxis-in-mpandroidchart
        analyticsGraphViewModel.getLineChart().getXAxis().setValueFormatter(new IndexAxisValueFormatter(analyticsGraphViewModel.getxAxisData()));
        analyticsGraphViewModel.getLineChart().notifyDataSetChanged();
    }


    private void loadData(final View root) {
        final Snackbar loadingSnackbar = Snackbar.make(root, "Loading data...", Snackbar.LENGTH_INDEFINITE);
        final GetDataTask getDataTask = new GetDataTask(root, getArguments().getString("dateTime"), loadingSnackbar);
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
            analyticsGraphViewModel.setData(data);
            initLineChart(analyticsGraphViewModel.getData());
            loadingSnackbar.dismiss();
        }
    }
}
