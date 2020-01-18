package com.softwareengineering.airprimaapp.visualization.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.softwareengineering.airprimaapp.R;
import com.softwareengineering.airprimaapp.other.DatabaseHandler;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Fragment in VisualizationActivity: First page when the user presses the humidity button.
 */
public class FragmentHumidityStartpage extends Fragment {

    private static final String TAG = FragmentHumidityStartpage.class.getSimpleName();
    private static final String MEASUREMENT_HUM = "measurement_hum";

    private TextView viewTitle;
    private TextView viewHumid;
    private TextView viewNoData;

    private DatabaseHandler dbHandler;
    private long locationID;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> updaterHandle = null;

    /**
     * Method to initialize fragment
     */
    public static FragmentHumidityStartpage newInstance(long locationID, boolean isConnected) {
        Bundle bundle = new Bundle();
        bundle.putLong("id", locationID);
        bundle.putBoolean("connected", isConnected);

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
            boolean isConnected = arguments.getBoolean("connected");

            insertData();
            if(isConnected) {
                startScheduledExecutorService();
            }
        }
    }

    /**
     * Closes database handler on fragment destruction
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        dbHandler.close();
        if(updaterHandle != null) {
            updaterHandle.cancel(true);
        }
        scheduler.shutdown();
    }

    /**
     * Insert the data into the views
     */
    private void insertData() {

        Cursor newestM = dbHandler.queryNewestMeasurement(locationID);
        if(newestM != null) {
            if(newestM.getCount() > 0) {

                newestM.moveToFirst();

                float humid = newestM.getFloat(newestM.getColumnIndex(MEASUREMENT_HUM));

                String year = newestM.getString(newestM.getColumnIndex("year"));
                String month = newestM.getString(newestM.getColumnIndex("month"));
                String day = newestM.getString(newestM.getColumnIndex("day"));
                String date = day + "." + month + "." + year;

                viewTitle.setVisibility(View.VISIBLE);
                viewHumid.setVisibility(View.VISIBLE);
                viewNoData.setVisibility(View.GONE);

                viewTitle.setText(getString(R.string.humidity_startpage_title, date));

                DecimalFormat df = new DecimalFormat("#.#");
                df.setRoundingMode(RoundingMode.HALF_UP);
                viewHumid.setText(getString(R.string.humidity_value, df.format(humid)));

            } else {
                viewTitle.setVisibility(View.GONE);
                viewHumid.setVisibility(View.GONE);
                viewNoData.setVisibility(View.VISIBLE);
            }
            newestM.close();
        }
    }

    /**
     * Starts a scheduledExecutorService that updates the measurements
     */
    private void startScheduledExecutorService() {

        final Runnable updater = new Runnable() {
            public void run() {
                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Starting task of ScheduledExecutorService");
                            insertData();
                            Log.d(TAG, "Finished task of ScheduledExecutorService");
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "ERROR in ScheduledExecutorService", e);
                }
            }
        };

        updaterHandle = scheduler.scheduleAtFixedRate(updater, 5, 5, TimeUnit.SECONDS);
    }
}
