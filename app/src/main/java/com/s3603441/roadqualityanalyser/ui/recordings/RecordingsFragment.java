package com.s3603441.roadqualityanalyser.ui.recordings;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.snackbar.Snackbar;
import com.s3603441.roadqualityanalyser.R;
import com.s3603441.roadqualityanalyser.db.AppDatabase;
import com.s3603441.roadqualityanalyser.db.accelerometer.Accelerometer;
import com.s3603441.roadqualityanalyser.ui.analytics.Analytics;

import java.util.List;

public class RecordingsFragment extends Fragment implements MyAdapter.ItemClickListener{

    private RecordingsViewModel recordingsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        recordingsViewModel =
                ViewModelProviders.of(this).get(RecordingsViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_recordings, container, false);

        initUI(root);

        return root;
    }

    public void initUI(final View root) {
        final GetDateTimesTask getDateTimesTask = new GetDateTimesTask(root);
        getDateTimesTask.execute(root.getContext().getApplicationContext());
    }

    public void initRecyclerView(final View root) {
        recordingsViewModel.setRecyclerView((RecyclerView) root.findViewById(R.id.recyclerview));
        recordingsViewModel.getRecyclerView().setHasFixedSize(true);
        recordingsViewModel.getRecyclerView().setLayoutManager(new LinearLayoutManager(root.getContext()));
        recordingsViewModel.setMyAdapter(new MyAdapter(root.getContext(), recordingsViewModel.getItems()));
        recordingsViewModel.getMyAdapter().setClickListener(this);
        recordingsViewModel.getRecyclerView().setAdapter(recordingsViewModel.getMyAdapter());
        // Source: https://stackoverflow.com/questions/28713231/android-add-divider-between-items-in-recyclerview
        recordingsViewModel.getRecyclerView().addItemDecoration(new DividerItemDecoration(root.getContext().getApplicationContext(), LinearLayout.VERTICAL));
    }

    public void showAnalytics(final Context context) {
        final Intent intent = new Intent(context, Analytics.class);
        startActivity(intent);
    }

    @Override
    public void onItemClick(final View root, final int position) {
        final Snackbar loadingSnackbar = Snackbar.make(root, "Loading data...", Snackbar.LENGTH_INDEFINITE);
        final GetDataTask getDataTask = new GetDataTask(root, recordingsViewModel.getMyAdapter().getItem(position), loadingSnackbar);
        loadingSnackbar.show();
        getDataTask.execute(root.getContext().getApplicationContext());
    }

    private class GetDateTimesTask extends AsyncTask <Context, Void, List<String>> {
        private View root;

        public GetDateTimesTask(final View root) {
            this.root = root;
        }

        @Override
        protected List<String> doInBackground(Context... contexts) {
            final AppDatabase appDatabase = Room.databaseBuilder(contexts[0],
                    AppDatabase.class, "data").build();

            return appDatabase.accelerometerDao().getDateTimes();
        }

        @Override
        protected void onPostExecute(final List<String> result) {
            recordingsViewModel.setItems(result);
            initRecyclerView(root);
        }
    }

    private class GetDataTask extends AsyncTask <Context, Void, List<Accelerometer>> {
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
            loadingSnackbar.dismiss();
            showAnalytics(root.getContext().getApplicationContext());
        }
    }
}


