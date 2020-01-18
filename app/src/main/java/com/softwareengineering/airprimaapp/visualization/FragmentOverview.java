package com.softwareengineering.airprimaapp.visualization;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.softwareengineering.airprimaapp.R;
import com.softwareengineering.airprimaapp.other.DatabaseHandler;

/**
 * Fragment in VisualizationActivity: Overview over all the four sensor readings.
 */
public class FragmentOverview extends Fragment {

    private static final String MEASUREMENT_PM_2_5 = "measurement_pm_2_5";
    private static final String MEASUREMENT_PM_10 = "measurement_pm_10";
    private static final String MEASUREMENT_TEMP = "measurement_temp";
    private static final String MEASUREMENT_HUM = "measurement_hum";

    private LinearLayout upperPart;
    private LinearLayout lowerPart;
    private TextView viewPM2_5;
    private TextView viewPM10;
    private TextView viewTemp;
    private TextView viewHumid;
    private TextView viewTitle;
    private TextView viewNoData;

    private DatabaseHandler dbHandler;
    private long locationID;

    /**
     * Method to initialize fragment
     */
    static FragmentOverview newInstance(long locationID) {
        Bundle bundle = new Bundle();
        bundle.putLong("id", locationID);

        FragmentOverview fragment = new FragmentOverview();
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     * Setup for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    /**
     * Triggered after onCreateView(). It is for view setup.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        upperPart = view.findViewById(R.id.overview_upper_part);
        lowerPart = view.findViewById(R.id.overview_lower_part);
        viewPM2_5 = view.findViewById(R.id.overview_pm2_5);
        viewPM10 = view.findViewById(R.id.overview_pm10);
        viewTemp = view.findViewById(R.id.overview_temp);
        viewHumid = view.findViewById(R.id.overview_humid);
        viewTitle = view.findViewById(R.id.overview_title);

        viewNoData = view.findViewById(R.id.overview_no_data);
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

                int pm2_5 = (int) newestM.getDouble(newestM.getColumnIndex(MEASUREMENT_PM_2_5));
                int pm10 = (int) newestM.getDouble(newestM.getColumnIndex(MEASUREMENT_PM_10));
                int temp = (int) newestM.getDouble(newestM.getColumnIndex(MEASUREMENT_TEMP));
                int humid = (int) newestM.getDouble(newestM.getColumnIndex(MEASUREMENT_HUM));

                String year = newestM.getString(newestM.getColumnIndex("year"));
                String month = newestM.getString(newestM.getColumnIndex("month"));
                String day = newestM.getString(newestM.getColumnIndex("day"));
                String date = day + "." + month + "." + year;

                viewPM2_5.setText(getString(R.string.overview_pm2_5, pm2_5));
                viewPM10.setText(getString(R.string.overview_pm10, pm10));
                viewTemp.setText(getString(R.string.overview_temp, temp));
                viewHumid.setText(getString(R.string.overview_humid, humid));
                viewTitle.setText(getString(R.string.overview_title, date));

            } else {
                viewTitle.setVisibility(View.GONE);
                upperPart.setVisibility(View.GONE);
                lowerPart.setVisibility(View.GONE);
                viewNoData.setVisibility(View.VISIBLE);
            }
            newestM.close();
        }
    }
}
