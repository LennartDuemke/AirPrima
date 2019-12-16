package com.softwareengineering.airprimaapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * Fragment in VisualizationActivity: First page when the user presses the humidity button.
 */
public class FragmentHumidityStartpage extends Fragment {

    private int humid = 56;

    /**
     * Setup for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_humidity_startpage, container, false);

        TextView tvHumid = view.findViewById(R.id.humidity);

        tvHumid.setText(getString(R.string.overview_humid, this.humid));

        return view;
    }
}
