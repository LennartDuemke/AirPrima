package com.softwareengineering.airprimaapp.visualization.pageradapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.softwareengineering.airprimaapp.visualization.Date;
import com.softwareengineering.airprimaapp.visualization.Sensor;
import com.softwareengineering.airprimaapp.visualization.fragments.FragmentDevelopment;
import com.softwareengineering.airprimaapp.visualization.fragments.FragmentEAQIClassification;
import com.softwareengineering.airprimaapp.visualization.fragments.FragmentFinedustStartpage;
import com.softwareengineering.airprimaapp.visualization.fragments.FragmentMinMax;
import com.softwareengineering.airprimaapp.visualization.fragments.FragmentPieChart;

/**
 * Adapter for the particulate matter ViewPager (Allows swiping between fragments).
 */
public class ViewPagerAdapterFinedust extends FragmentStatePagerAdapter {

    private int fragmentCount = 9;
    private long locationID;
    private boolean isConnected;

    /**
     * Constructor
     */
    public ViewPagerAdapterFinedust(FragmentManager fm, long locationID, boolean isConnected) {
        super(fm);
        this.locationID = locationID;
        this.isConnected = isConnected;
    }

    /**
     * Decides which fragment to return
     */
    @Override
    public Fragment getItem(int i) {
        switch(i) {
            case 0:
                return FragmentFinedustStartpage.newInstance(locationID, isConnected);
            case 1:
                return FragmentEAQIClassification.newInstance(locationID, isConnected);
            case 2:
                return FragmentDevelopment.newInstance(locationID, Sensor.FINEDUST, Date.DAY, isConnected);
            case 3:
                return FragmentDevelopment.newInstance(locationID, Sensor.FINEDUST, Date.WEEK, isConnected);
            case 4:
                return FragmentDevelopment.newInstance(locationID, Sensor.FINEDUST, Date.MONTH, isConnected);
            case 5:
                return FragmentDevelopment.newInstance(locationID, Sensor.FINEDUST, Date.YEAR, isConnected);
            case 6:
                return FragmentPieChart.newInstance(locationID, false, isConnected);
            case 7:
                return FragmentPieChart.newInstance(locationID, true, isConnected);
            case 8:
                return FragmentMinMax.newInstance(locationID, Sensor.FINEDUST, isConnected);
            default:
                return FragmentFinedustStartpage.newInstance(locationID, isConnected);
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
