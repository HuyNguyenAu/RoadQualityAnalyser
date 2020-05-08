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

public class AnalyticsFragment extends Fragment {

    private AnalyticsViewModel analyticsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        analyticsViewModel =
                ViewModelProviders.of(this).get(AnalyticsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_analytics, container, false);
        return root;
    }
}
