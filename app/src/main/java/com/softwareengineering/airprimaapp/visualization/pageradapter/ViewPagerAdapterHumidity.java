package com.softwareengineering.airprimaapp.visualization.pageradapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.softwareengineering.airprimaapp.visualization.Date;
import com.softwareengineering.airprimaapp.visualization.Sensor;
import com.softwareengineering.airprimaapp.visualization.fragments.FragmentDevelopment;
import com.softwareengineering.airprimaapp.visualization.fragments.FragmentHumidityStartpage;
import com.softwareengineering.airprimaapp.visualization.fragments.FragmentMinMax;

/**
 * Adapter for the humidity ViewPager (Allows swiping between fragments).
 */
public class ViewPagerAdapterHumidity extends FragmentStatePagerAdapter {

    private int fragmentCount = 6;
    private long locationID;
    private boolean isConnected;

    /**
     * Constructor
     */
    public ViewPagerAdapterHumidity(FragmentManager fm, long locationID, boolean isConnected) {
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
                return FragmentHumidityStartpage.newInstance(locationID, isConnected);
            case 1:
                return FragmentDevelopment.newInstance(locationID, Sensor.HUMIDITY, Date.DAY, isConnected);
            case 2:
                return FragmentDevelopment.newInstance(locationID, Sensor.HUMIDITY, Date.WEEK, isConnected);
            case 3:
                return FragmentDevelopment.newInstance(locationID, Sensor.HUMIDITY, Date.MONTH, isConnected);
            case 4:
                return FragmentDevelopment.newInstance(locationID, Sensor.HUMIDITY, Date.YEAR, isConnected);
            case 5:
                return FragmentMinMax.newInstance(locationID, Sensor.HUMIDITY, isConnected);
            default:
                return FragmentHumidityStartpage.newInstance(locationID, isConnected);
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

