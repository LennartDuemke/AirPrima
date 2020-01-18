package com.softwareengineering.airprimaapp.visualization.pageradapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.softwareengineering.airprimaapp.visualization.Date;
import com.softwareengineering.airprimaapp.visualization.Sensor;
import com.softwareengineering.airprimaapp.visualization.fragments.FragmentDevelopment;
import com.softwareengineering.airprimaapp.visualization.fragments.FragmentMinMax;
import com.softwareengineering.airprimaapp.visualization.fragments.FragmentTemperatureStartpage;

/**
 * Adapter for the temperature ViewPager (Allows swiping between fragments).
 */
public class ViewPagerAdapterTemperature extends FragmentStatePagerAdapter {

    private int fragmentCount = 6;
    private long locationID;
    private boolean isConnected;

    /**
     * Constructor
     */
    public ViewPagerAdapterTemperature(FragmentManager fm, long locationID, boolean isConnected) {
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
                return FragmentTemperatureStartpage.newInstance(locationID, isConnected);
            case 1:
                return FragmentDevelopment.newInstance(locationID, Sensor.TEMPERATURE, Date.DAY, isConnected);
            case 2:
                return FragmentDevelopment.newInstance(locationID, Sensor.TEMPERATURE, Date.WEEK, isConnected);
            case 3:
                return FragmentDevelopment.newInstance(locationID, Sensor.TEMPERATURE, Date.MONTH, isConnected);
            case 4:
                return FragmentDevelopment.newInstance(locationID, Sensor.TEMPERATURE, Date.YEAR, isConnected);
            case 5:
                return FragmentMinMax.newInstance(locationID, Sensor.TEMPERATURE, isConnected);
            default:
                return FragmentTemperatureStartpage.newInstance(locationID, isConnected);
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
