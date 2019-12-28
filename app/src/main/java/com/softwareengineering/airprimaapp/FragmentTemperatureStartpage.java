package com.softwareengineering.airprimaapp;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * Fragment in VisualizationActivity: First page when the user presses the temperature button.
 */
public class FragmentTemperatureStartpage extends Fragment {

    private static final String MEASUREMENT_TEMP = "measurement_temp";

    private TextView viewTitle;
    private TextView viewTempCelsius;
    private TextView viewTempFahrenheit;
    private TextView viewNoData;

    private DatabaseHandler dbHandler;
    private long locationID;

    /**
     * Method to initialize fragment
     */
    static FragmentTemperatureStartpage newInstance(long locationID) {
        Bundle bundle = new Bundle();
        bundle.putLong("id", locationID);

        FragmentTemperatureStartpage fragment = new FragmentTemperatureStartpage();
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     * Setup for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_temperature_startpage, container, false);
    }

    /**
     * Triggered after onCreateView(). It is for view setup.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewTitle = view.findViewById(R.id.temperature_startpage_title);
        viewTempCelsius = view.findViewById(R.id.temperature_startpage_celsius);
        viewTempFahrenheit = view.findViewById(R.id.temperature_startpage_fahrenheit);
        viewNoData = view.findViewById(R.id.temperature_startpage_no_data);
        viewNoData.setVisibility(View.GONE);

        dbHandler = new DatabaseHandler(view.getContext());

        // Read the bundle data
        Bundle arguments = getArguments();
        if (arguments != null) {
            locationID = arguments.getLong("id");

            insertData();
        }
    }

    /**
     * Closes database handler on fragment destruction
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHandler.close();
    }

    /**
     * Insert the data into the views
     */
    private void insertData() {

        Cursor newestM = dbHandler.queryNewestMeasurement(locationID);
        if(newestM != null) {
            if(newestM.getCount() > 0) {

                newestM.moveToFirst();

                double tempCelsius = newestM.getDouble(newestM.getColumnIndex(MEASUREMENT_TEMP));

                String year = newestM.getString(newestM.getColumnIndex("year"));
                String month = newestM.getString(newestM.getColumnIndex("month"));
                String day = newestM.getString(newestM.getColumnIndex("day"));
                String date = day + "." + month + "." + year;

                viewTitle.setText(getString(R.string.temperature_startpage_title, date));
                viewTempCelsius.setText(getString(R.string.temperature_value_celsius, (int)tempCelsius));
                double tempFahrenheit = (tempCelsius * 9/5) + 32;
                viewTempFahrenheit.setText(getString(R.string.temperature_startpage_value_fahrenheit, (int)tempFahrenheit));

            } else {
                viewTitle.setVisibility(View.GONE);
                viewTempCelsius.setVisibility(View.GONE);
                viewTempFahrenheit.setVisibility(View.GONE);
                viewNoData.setVisibility(View.VISIBLE);
            }
            newestM.close();
        }
    }
}

