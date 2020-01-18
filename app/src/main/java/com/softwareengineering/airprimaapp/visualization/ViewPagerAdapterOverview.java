package com.softwareengineering.airprimaapp.visualization;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Adapter for the overview ViewPager (Allows swiping between fragments; In this case there is only one fragment).
 */
class ViewPagerAdapterOverview extends FragmentStatePagerAdapter {

    private int fragmentCount = 1;
    private long locationID;

    /**
     * Constructor
     */
    public ViewPagerAdapterOverview(FragmentManager fm, long locationID) {
        super(fm);
        this.locationID = locationID;
    }

    /**
     * Decides which fragment to return
     */
    @Override
    public Fragment getItem(int i) {
        return FragmentOverview.newInstance(locationID);
    }

    /**
     * Returns the number of fragments that are part of the ViewPager
     */
    @Override
    public int getCount() {
        return fragmentCount;
    }
}

