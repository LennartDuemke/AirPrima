package com.softwareengineering.airprimaapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * Fragment in VisualizationActivity: Overview over all the four sensor readings.
 */
public class FragmentOverview extends Fragment {

    private int pm2_5 = 8;
    private int pm10 = 25;
    private int temp = 20;
    private int humid = 56;

    /**
     * Setup for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);

        TextView tvPM25 = view.findViewById(R.id.pm2_5);
        TextView tvPM10 = view.findViewById(R.id.pm10);
        TextView tvTemp = view.findViewById(R.id.temp);
        TextView tvHumid = view.findViewById(R.id.humid);

        tvPM25.setText(getString(R.string.overview_pm2_5, this.pm2_5));
        tvPM10.setText(getString(R.string.overview_pm10, this.pm10));
        tvTemp.setText(getString(R.string.overview_temp, this.temp));
        tvHumid.setText(getString(R.string.overview_humid, this.humid));

        return view;
    }
}
