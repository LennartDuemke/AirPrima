package com.softwareengineering.airprimaapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * Fragment in VisualizationActivity: First page when the user presses the temperature button.
 */
public class FragmentTemperatureStartpage extends Fragment {

    private int temp = 20;

    /**
     * Setup for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_temperature_startpage, container, false);

        TextView tvTemp = view.findViewById(R.id.temp_celsius);

        tvTemp.setText(getString(R.string.overview_temp, this.temp));

        return view;
    }
}

