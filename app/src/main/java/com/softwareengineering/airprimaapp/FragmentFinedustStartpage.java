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
 * Fragment in VisualizationActivity: First page when the user presses the particulate matter button.
 */
public class FragmentFinedustStartpage extends Fragment {

    private static final String MEASUREMENT_PM_2_5 = "measurement_pm_2_5";
    private static final String MEASUREMENT_PM_10 = "measurement_pm_10";

    private TextView viewTitle;
    private TextView viewPM2_5;
    private TextView viewPM10;
    private TextView viewNoData;

    private DatabaseHandler dbHandler;
    private long locationID;

    /**
     * Method to initialize fragment
     */
    static FragmentFinedustStartpage newInstance(long locationID) {
        Bundle bundle = new Bundle();
        bundle.putLong("id", locationID);

        FragmentFinedustStartpage fragment = new FragmentFinedustStartpage();
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     * Setup for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_finedust_startpage, container, false);
    }

    /**
     * Triggered after onCreateView(). It is for view setup.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewTitle = view.findViewById(R.id.finedust_startpage_title);
        viewPM2_5 = view.findViewById(R.id.finedust_startpage_2_5);
        viewPM10 = view.findViewById(R.id.finedust_startpage_10);
        viewNoData = view.findViewById(R.id.finedust_startpage_no_data);
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

                double value2_5 = newestM.getDouble(newestM.getColumnIndex(MEASUREMENT_PM_2_5));
                double value10 = newestM.getDouble(newestM.getColumnIndex(MEASUREMENT_PM_10));

                String year = newestM.getString(newestM.getColumnIndex("year"));
                String month = newestM.getString(newestM.getColumnIndex("month"));
                String day = newestM.getString(newestM.getColumnIndex("day"));
                String date = day + "." + month + "." + year;

                viewTitle.setText(getString(R.string.finedust_startpage_title, date));
                viewPM2_5.setText(getString(R.string.finedust_startpage_2_5, (int)value2_5));
                viewPM10.setText(getString(R.string.finedust_startpage_10, (int)value10));

            } else {
                viewTitle.setVisibility(View.GONE);
                viewPM2_5.setVisibility(View.GONE);
                viewPM10.setVisibility(View.GONE);
                viewNoData.setVisibility(View.VISIBLE);
            }
            newestM.close();
        }
    }
}

