package com.softwareengineering.airprimaapp.visualization.pageradapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.softwareengineering.airprimaapp.visualization.fragments.FragmentEAQIClassification;
import com.softwareengineering.airprimaapp.visualization.fragments.FragmentFinedustStartpage;

/**
 * Mobile measurement: Adapter for the particulate matter ViewPager (Allows swiping between fragments).
 */
public class ViewPagerAdapterFinedustMobile extends FragmentStatePagerAdapter {

    private int fragmentCount = 2;
    private long locationID;

    /**
     * Constructor
     */
    public ViewPagerAdapterFinedustMobile(FragmentManager fm, long locationID) {
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
                return FragmentFinedustStartpage.newInstance(locationID, true);
            case 1:
                return FragmentEAQIClassification.newInstance(locationID, true);
            default:
                return FragmentFinedustStartpage.newInstance(locationID, true);
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
