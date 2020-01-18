package com.softwareengineering.airprimaapp.visualization.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
 * Fragment in VisualizationActivity: Overview over all the four sensor readings.
 */
public class FragmentOverview extends Fragment {

    private static final String TAG = FragmentOverview.class.getSimpleName();

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

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> updaterHandle = null;

    /**
     * Method to initialize fragment
     */
    public static FragmentOverview newInstance(long locationID, boolean isConnected) {
        Bundle bundle = new Bundle();
        bundle.putLong("id", locationID);
        bundle.putBoolean("connected", isConnected);

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

                float pm2_5 = newestM.getFloat(newestM.getColumnIndex(MEASUREMENT_PM_2_5));
                float pm10 = newestM.getFloat(newestM.getColumnIndex(MEASUREMENT_PM_10));
                float temp = newestM.getFloat(newestM.getColumnIndex(MEASUREMENT_TEMP));
                float humid = newestM.getFloat(newestM.getColumnIndex(MEASUREMENT_HUM));

                String year = newestM.getString(newestM.getColumnIndex("year"));
                String month = newestM.getString(newestM.getColumnIndex("month"));
                String day = newestM.getString(newestM.getColumnIndex("day"));
                String date = day + "." + month + "." + year;

                viewTitle.setVisibility(View.VISIBLE);
                upperPart.setVisibility(View.VISIBLE);
                lowerPart.setVisibility(View.VISIBLE);
                viewNoData.setVisibility(View.GONE);

                viewTitle.setText(getString(R.string.overview_title, date));

                DecimalFormat df = new DecimalFormat("#.#");
                df.setRoundingMode(RoundingMode.HALF_UP);
                viewPM2_5.setText(getString(R.string.overview_pm2_5, df.format(pm2_5)));
                viewPM10.setText(getString(R.string.overview_pm10, df.format(pm10)));
                viewTemp.setText(getString(R.string.overview_temp, df.format(temp)));
                viewHumid.setText(getString(R.string.overview_humid, df.format(humid)));

            } else {
                viewTitle.setVisibility(View.GONE);
                upperPart.setVisibility(View.GONE);
                lowerPart.setVisibility(View.GONE);
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
