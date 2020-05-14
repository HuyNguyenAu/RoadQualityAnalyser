package com.s3603441.roadqualityanalyser.ui.analytics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AnalyticsViewModel extends ViewModel {

    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    private List<String> items;

    public AnalyticsViewModel() {
    }

    public RecyclerView getRecyclerView() {
        return this.recyclerView;
    }

    public void setRecyclerView(final RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public MyAdapter getMyAdapter() {
        return this.myAdapter;
    }

    public void setMyAdapter(final MyAdapter myAdapter) {
        this.myAdapter = myAdapter;
    }

    public List<String> getItems() {
        return this.items;
    }

    public void setItems(final List<String> items) {
        this.items = items;
    }
}