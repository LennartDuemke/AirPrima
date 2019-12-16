package com.softwareengineering.airprimaapp;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Adapter for the humidity ViewPager (Allows swiping between fragments).
 */
class ViewPagerAdapterHumidity extends FragmentStatePagerAdapter {

    private int fragmentCount = 5;
    private long locationID;

    /**
     * Constructor
     */
    public ViewPagerAdapterHumidity(FragmentManager fm, long locationID) {
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
                return new FragmentHumidityStartpage();
            case 1:
                return FragmentDevelopment.newInstance(locationID, Sensor.HUMIDITY, Date.DAY);
            case 2:
                return FragmentDevelopment.newInstance(locationID, Sensor.HUMIDITY, Date.WEEK);
            case 3:
                return FragmentDevelopment.newInstance(locationID, Sensor.HUMIDITY, Date.MONTH);
            case 4:
                return FragmentDevelopment.newInstance(locationID, Sensor.HUMIDITY, Date.YEAR);
            default:
                return new FragmentHumidityStartpage();
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
