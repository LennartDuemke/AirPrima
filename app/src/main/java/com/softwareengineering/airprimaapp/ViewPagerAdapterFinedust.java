package com.softwareengineering.airprimaapp;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Adapter for the particulate matter ViewPager (Allows swiping between fragments).
 */
class ViewPagerAdapterFinedust extends FragmentStatePagerAdapter {

    private int fragmentCount = 9;
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
                return FragmentFinedustStartpage.newInstance(locationID);
            case 1:
                return FragmentEAQIClassification.newInstance(locationID);
            case 2:
                return FragmentDevelopment.newInstance(locationID, Sensor.FINEDUST, Date.DAY);
            case 3:
                return FragmentDevelopment.newInstance(locationID, Sensor.FINEDUST, Date.WEEK);
            case 4:
                return FragmentDevelopment.newInstance(locationID, Sensor.FINEDUST, Date.MONTH);
            case 5:
                return FragmentDevelopment.newInstance(locationID, Sensor.FINEDUST, Date.YEAR);
            case 6:
                return FragmentPieChart.newInstance(locationID, false);
            case 7:
                return FragmentPieChart.newInstance(locationID, true);
            case 8:
                return FragmentMinMax.newInstance(locationID, Sensor.FINEDUST);
            default:
                return FragmentFinedustStartpage.newInstance(locationID);
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
