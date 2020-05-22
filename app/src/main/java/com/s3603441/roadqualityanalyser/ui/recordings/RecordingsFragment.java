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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.s3603441.roadqualityanalyser.R;
import com.s3603441.roadqualityanalyser.db.AppDatabase;
import com.s3603441.roadqualityanalyser.db.accelerometer.Accelerometer;
import com.s3603441.roadqualityanalyser.ui.analytics.Analytics;

import java.util.ArrayList;
import java.util.List;

public class RecordingsFragment extends Fragment implements MyAdapter.ItemClickListener {

    private RecordingsViewModel recordingsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        recordingsViewModel = new ViewModelProvider(this).get(RecordingsViewModel.class);
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

    public void showAnalytics(final Context context, final String dateTime) {
        final Intent intent = new Intent(context, Analytics.class);
        intent.putExtra("dateTime", dateTime);
        startActivity(intent);
    }

    @Override
    public void onItemClick(final View root, final int position) {
        showAnalytics(root.getContext().getApplicationContext(), recordingsViewModel.getMyAdapter().getItem(position));
    }

    private class GetDateTimesTask extends AsyncTask<Context, Void, List<Item>> {
        private View root;

        public GetDateTimesTask(final View root) {
            this.root = root;
        }

        @Override
        protected List<Item> doInBackground(Context... contexts) {
            final AppDatabase appDatabase = Room.databaseBuilder(contexts[0],
                    AppDatabase.class, "data").build();
            final List<String> dateTimes = appDatabase.accelerometerDao().getDateTimes();

            List<Item> items = new ArrayList<>();

            for (int i = 0; i < dateTimes.size(); i++) {
                final String dateTime = dateTimes.get(i);
                final int warnings = appDatabase.accelerometerDao().getWarnings(dateTime);
                final Accelerometer firstItem = appDatabase.accelerometerDao().getData(dateTime).get(0);

                items.add(new Item(dateTime, warnings, firstItem.getLatitude(), firstItem.getLongitude()));
            }

            return items;
        }

        @Override
        protected void onPostExecute(final List<Item> result) {
            recordingsViewModel.setItems(result);
            initRecyclerView(root);
        }
    }
}


