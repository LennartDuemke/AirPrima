package com.softwareengineering.airprimaapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * Fragment in VisualizationActivity: First page when the user presses the particulate matter button.
 */
public class FragmentFinedustStartpage extends Fragment {

    private int pm2_5 = 8;
    private int pm10 = 25;

    /**
     * Setup for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finedust_startpage, container, false);

        TextView tvPM25 = view.findViewById(R.id.finedust_pm2_5);
        TextView tvPM10 = view.findViewById(R.id.finedust_pm10);

        tvPM25.setText(getString(R.string.overview_pm2_5, this.pm2_5));
        tvPM10.setText(getString(R.string.overview_pm10, this.pm10));

        return view;
    }
}

