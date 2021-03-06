package com.s3603441.roadqualityanalyser.ui.analytics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.s3603441.roadqualityanalyser.R;
import com.s3603441.roadqualityanalyser.ui.analytics_graph.AnalyticsGraph;
import com.s3603441.roadqualityanalyser.ui.analytics_map.AnalyticsMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    public static AnalyticsMap newInstanceMap(final String dateTime) {
        AnalyticsMap analyticsMap = new AnalyticsMap();
        Bundle bundle = new Bundle();
        bundle.putString("dateTime", dateTime);
        analyticsMap.setArguments(bundle);
        return analyticsMap;
    }

    public static AnalyticsGraph newInstanceGraph(final String dateTime) {
        AnalyticsGraph analyticsGraph = new AnalyticsGraph();
        Bundle bundle = new Bundle();
        bundle.putString("dateTime", dateTime);
        analyticsGraph.setArguments(bundle);
        return analyticsGraph;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_analytics, container, false);
        final TextView textView = root.findViewById(R.id.section_label);
        pageViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}