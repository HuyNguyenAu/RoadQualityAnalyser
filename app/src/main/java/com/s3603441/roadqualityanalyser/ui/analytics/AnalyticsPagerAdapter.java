package com.s3603441.roadqualityanalyser.ui.analytics;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.s3603441.roadqualityanalyser.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class AnalyticsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_map, R.string.tab_text_graph};
    private final Context mContext;
    private String dateTime;

    public AnalyticsPagerAdapter(Context context, FragmentManager fm, final String dateTime) {
        super(fm);
        mContext = context;
        setDateTime(dateTime);
    }

    @Override
    public Fragment getItem(int position) {
       Fragment fragment = PlaceholderFragment.newInstanceMap(getDateTime());

       if (position == 1) {
           fragment = PlaceholderFragment.newInstanceGraph(getDateTime());
       }

        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }

    public String getDateTime() {
        return this.dateTime;
    }

    public void setDateTime(final String dateTime) {
        this.dateTime = dateTime;
    }
}