package com.softwareengineering.airprimaapp.visualization.pageradapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.softwareengineering.airprimaapp.visualization.fragments.FragmentOverview;

/**
 * Adapter for the overview ViewPager (Allows swiping between fragments; In this case there is only one fragment).
 */
public class ViewPagerAdapterOverview extends FragmentStatePagerAdapter {

    private int fragmentCount = 1;
    private long locationID;
    private boolean isConnected;

    /**
     * Constructor
     */
    public ViewPagerAdapterOverview(FragmentManager fm, long locationID, boolean isConnected) {
        super(fm);
        this.locationID = locationID;
        this.isConnected = isConnected;
    }

    /**
     * Decides which fragment to return
     */
    @Override
    public Fragment getItem(int i) {
        return FragmentOverview.newInstance(locationID, isConnected);
    }

    /**
     * Returns the number of fragments that are part of the ViewPager
     */
    @Override
    public int getCount() {
        return fragmentCount;
    }
}

