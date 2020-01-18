package com.softwareengineering.airprimaapp.visualization.pageradapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.softwareengineering.airprimaapp.visualization.fragments.FragmentTemperatureStartpage;

/**
 * Mobile measurement: Adapter for the temperature ViewPager (Allows swiping between fragments).
 */
public class ViewPagerAdapterTemperatureMobile extends FragmentStatePagerAdapter {

    private int fragmentCount = 1;
    private long locationID;

    /**
     * Constructor
     */
    public ViewPagerAdapterTemperatureMobile(FragmentManager fm, long locationID) {
        super(fm);
        this.locationID = locationID;
    }

    /**
     * Decides which fragment to return
     */
    @Override
    public Fragment getItem(int i) {
        switch(i) {
            case 0:
                return FragmentTemperatureStartpage.newInstance(locationID, true);
            default:
                return FragmentTemperatureStartpage.newInstance(locationID, true);
        }
    }

    /**
     * Returns the number of fragments that are part of the ViewPager
     */
    @Override
    public int getCount() {
        return fragmentCount;
    }
}
