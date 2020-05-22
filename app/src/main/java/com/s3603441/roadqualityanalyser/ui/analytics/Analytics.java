package com.s3603441.roadqualityanalyser.ui.analytics;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;

import com.s3603441.roadqualityanalyser.R;

public class Analytics extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final AnalyticsPagerAdapter analyticsPagerAdapter = new AnalyticsPagerAdapter(this, getSupportFragmentManager(), getIntent().getExtras().getString("dateTime"));
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(analyticsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}