package com.softwareengineering.airprimaapp.visualization;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.softwareengineering.airprimaapp.R;
import com.softwareengineering.airprimaapp.other.DatabaseHandler;

/**
 * Fragment in VisualizationActivity: First page when the user presses the humidity button.
 */
public class FragmentHumidityStartpage extends Fragment {

    private static final String MEASUREMENT_HUM = "measurement_hum";

    private TextView viewTitle;
    private TextView viewHumid;
    private TextView viewNoData;

    private DatabaseHandler dbHandler;
    private long locationID;

    /**
     * Method to initialize fragment
     */
    static FragmentHumidityStartpage newInstance(long locationID) {
        Bundle bundle = new Bundle();
        bundle.putLong("id", locationID);

        FragmentHumidityStartpage fragment = new FragmentHumidityStartpage();
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     * Setup for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_humidity_startpage, container, false);
    }

    /**
     * Triggered after onCreateView(). It is for view setup.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewTitle = view.findViewById(R.id.humidity_startpage_title);
        viewHumid = view.findViewById(R.id.humidity_startpage_value);
        viewNoData = view.findViewById(R.id.humidity_startpage_no_data);
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

                int humid = (int) newestM.getDouble(newestM.getColumnIndex(MEASUREMENT_HUM));

                String year = newestM.getString(newestM.getColumnIndex("year"));
                String month = newestM.getString(newestM.getColumnIndex("month"));
                String day = newestM.getString(newestM.getColumnIndex("day"));
                String date = day + "." + month + "." + year;

                viewTitle.setText(getString(R.string.humidity_startpage_title, date));
                viewHumid.setText(getString(R.string.humidity_value, humid));

            } else {
                viewTitle.setVisibility(View.GONE);
                viewHumid.setVisibility(View.GONE);
                viewNoData.setVisibility(View.VISIBLE);
            }
            newestM.close();
        }
    }
}
