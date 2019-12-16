package com.softwareengineering.airprimaapp;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Adapter for the particulate matter ViewPager (Allows swiping between fragments).
 */
class ViewPagerAdapterFinedust extends FragmentStatePagerAdapter {

    private int fragmentCount = 5;
    private long locationID;

    /**
     * Constructor
     */
    public ViewPagerAdapterFinedust(FragmentManager fm, long locationID) {
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
                return new FragmentFinedustStartpage();
            case 1:
                return FragmentDevelopment.newInstance(locationID, Sensor.FINEDUST, Date.DAY);
            case 2:
                return FragmentDevelopment.newInstance(locationID, Sensor.FINEDUST, Date.WEEK);
            case 3:
                return FragmentDevelopment.newInstance(locationID, Sensor.FINEDUST, Date.MONTH);
            case 4:
                return FragmentDevelopment.newInstance(locationID, Sensor.FINEDUST, Date.YEAR);
            default:
                return new FragmentFinedustStartpage();
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
