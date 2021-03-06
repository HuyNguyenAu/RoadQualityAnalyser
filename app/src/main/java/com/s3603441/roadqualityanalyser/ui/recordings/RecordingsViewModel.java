package com.s3603441.roadqualityanalyser.ui.recordings;

import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecordingsViewModel extends ViewModel {

    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    private List<Item> items;

    public RecordingsViewModel() {
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

    public List<Item> getItems() {
        return this.items;
    }

    public void setItems(final List<Item> items) {
        this.items = items;
    }
}