package com.s3603441.roadqualityanalyser.ui.recordings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.s3603441.roadqualityanalyser.R;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<Item> items;
    private LayoutInflater layoutInflater;
    private ItemClickListener itemClickListener;

    MyAdapter(Context context, List<Item> data) {
        this.layoutInflater = LayoutInflater.from(context);
        this.items = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View view = layoutInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.textView_datetime.setText("Datetime: " + items.get(position).getDateTime());
        holder.textView_warnings.setText("Total Warnings: " + items.get(position).getWarnings());
        holder.textView_coordinates.setText("Starting Coordinates: " + items.get(position).getLatitude() + ", "
                + items.get(position).getLongitude());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textView_datetime;
        TextView textView_warnings;
        TextView textView_coordinates;

        ViewHolder(final View itemView) {
            super(itemView);
            textView_datetime = itemView.findViewById(R.id.datetime);
            textView_warnings = itemView.findViewById(R.id.warnings);
            textView_coordinates = itemView.findViewById(R.id.coordinates);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(final View view) {
            if (itemClickListener != null) itemClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    String getItem(final int id) {
        return items.get(id).getDateTime();
    }

    void setClickListener(final ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}