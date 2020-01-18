package com.softwareengineering.airprimaapp.visualization.fragments;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
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
 * Fragment in VisualizationActivity: Classifies the newest measurement according to the EAQI
 */
public class FragmentEAQIClassification extends Fragment {

    private static final String TAG = FragmentEAQIClassification.class.getSimpleName();
    private static final String MEASUREMENT_PM_2_5 = "measurement_pm_2_5";
    private static final String MEASUREMENT_PM_10 = "measurement_pm_10";

    private TextView viewTitle;
    private TextView viewPM2_5Title;
    private TextView viewPM2_5;
    private TextView viewPM10Title;
    private TextView viewPM10;
    private TableLayout viewTable;
    private TextView viewNoData;

    private DatabaseHandler dbHandler;
    private long locationID;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> updaterHandle = null;

    /**
     * Method to initialize fragment
     */
    public static FragmentEAQIClassification newInstance(long locationID, boolean isConnected) {
        Bundle bundle = new Bundle();
        bundle.putLong("id", locationID);
        bundle.putBoolean("connected", isConnected);

        FragmentEAQIClassification fragment = new FragmentEAQIClassification();
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     * Setup for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_eaqi_classification, container, false);
    }

    /**
     * Triggered after onCreateView(). It is for view setup.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewTitle = view.findViewById(R.id.eaqi_classification_title);
        viewPM2_5Title = view.findViewById(R.id.eaqi_classification_2_5_title);
        viewPM2_5 = view.findViewById(R.id.eaqi_classification_2_5);
        viewPM10Title = view.findViewById(R.id.eaqi_classification_10_title);
        viewPM10 = view.findViewById(R.id.eaqi_classification_10);
        viewTable = view.findViewById(R.id.eaqi_classification_table);

        viewNoData = view.findViewById(R.id.eaqi_classification_no_data);
        viewNoData.setVisibility(View.GONE);

        dbHandler = new DatabaseHandler(view.getContext());

        // Read the bundle data
        Bundle arguments = getArguments();
        if (arguments != null) {
            locationID = arguments.getLong("id");
            boolean isConnected = arguments.getBoolean("connected");

            insertData();
            if (isConnected) {
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
        if (updaterHandle != null) {
            updaterHandle.cancel(true);
        }
        scheduler.shutdown();
    }

    /**
     * Insert the data into the views
     */
    private void insertData() {

        Cursor newestM = dbHandler.queryNewestMeasurement(locationID);
        if (newestM != null) {
            if (newestM.getCount() > 0) {

                newestM.moveToFirst();

                float value2_5 = newestM.getFloat(newestM.getColumnIndex(MEASUREMENT_PM_2_5));
                float value10 = newestM.getFloat(newestM.getColumnIndex(MEASUREMENT_PM_10));

                String year = newestM.getString(newestM.getColumnIndex("year"));
                String month = newestM.getString(newestM.getColumnIndex("month"));
                String day = newestM.getString(newestM.getColumnIndex("day"));
                String date = day + "." + month + "." + year;

                viewPM2_5Title.setVisibility(View.VISIBLE);
                viewPM2_5.setVisibility(View.VISIBLE);
                viewPM10Title.setVisibility(View.VISIBLE);
                viewPM10.setVisibility(View.VISIBLE);
                viewTable.setVisibility(View.VISIBLE);
                viewNoData.setVisibility(View.GONE);

                viewTitle.setText(getString(R.string.eaqi_classification_title, date));

                DecimalFormat df = new DecimalFormat("#.#");
                df.setRoundingMode(RoundingMode.HALF_UP);
                viewPM2_5.setText(df.format(value2_5));
                viewPM10.setText(df.format(value10));

                analyseValues(value2_5, value10);

            } else {
                viewTitle.setText(getString(R.string.eaqi_classification_title, "-"));
                viewPM2_5Title.setVisibility(View.GONE);
                viewPM2_5.setVisibility(View.GONE);
                viewPM10Title.setVisibility(View.GONE);
                viewPM10.setVisibility(View.GONE);
                viewTable.setVisibility(View.GONE);
                viewNoData.setVisibility(View.VISIBLE);
            }
            newestM.close();
        }
    }

    /**
     * Checks the PM values and classifies them according the EAQI
     */
    private void analyseValues(float pm2_5, float pm10) {

        // Check PM2.5
        if (pm2_5 >= 0 && pm2_5 < 10) {
            viewPM2_5Title.setText(getString(R.string.eaqi_classification_pm_2_5, getString(R.string.good)));
            viewPM2_5.setBackgroundColor(Color.parseColor("#50f0e6"));
        } else if (pm2_5 >= 10 && pm2_5 < 20) {
            viewPM2_5Title.setText(getString(R.string.eaqi_classification_pm_2_5, getString(R.string.fair)));
            viewPM2_5.setBackgroundColor(Color.parseColor("#50ccaa"));
        } else if (pm2_5 >= 20 && pm2_5 < 25) {
            viewPM2_5Title.setText(getString(R.string.eaqi_classification_pm_2_5, getString(R.string.moderate)));
            viewPM2_5.setBackgroundColor(Color.parseColor("#f3eb6a"));
        } else if (pm2_5 >= 25 && pm2_5 < 50) {
            viewPM2_5Title.setText(getString(R.string.eaqi_classification_pm_2_5, getString(R.string.poor)));
            viewPM2_5.setBackgroundColor(Color.parseColor("#ff5050"));
        } else if (pm2_5 >= 50 && pm2_5 < 75) {
            viewPM2_5Title.setText(getString(R.string.eaqi_classification_pm_2_5, getString(R.string.very_poor)));
            viewPM2_5.setBackgroundColor(Color.parseColor("#960032"));
        } else if (pm2_5 >= 75 && pm2_5 <= 800) {
            viewPM2_5Title.setText(getString(R.string.eaqi_classification_pm_2_5, getString(R.string.extremely_poor)));
            viewPM2_5.setBackgroundColor(Color.parseColor("#7d2181"));
        }

        // Check PM10
        if (pm10 >= 0 && pm10 < 20) {
            viewPM10Title.setText(getString(R.string.eaqi_classification_pm_10, getString(R.string.good)));
            viewPM10.setBackgroundColor(Color.parseColor("#50f0e6"));
        } else if (pm10 >= 20 && pm10 < 40) {
            viewPM10Title.setText(getString(R.string.eaqi_classification_pm_10, getString(R.string.fair)));
            viewPM10.setBackgroundColor(Color.parseColor("#50ccaa"));
        } else if (pm10 >= 40 && pm10 < 50) {
            viewPM10Title.setText(getString(R.string.eaqi_classification_pm_10, getString(R.string.moderate)));
            viewPM10.setBackgroundColor(Color.parseColor("#f3eb6a"));
        } else if (pm10 >= 50 && pm10 < 100) {
            viewPM10Title.setText(getString(R.string.eaqi_classification_pm_10, getString(R.string.poor)));
            viewPM10.setBackgroundColor(Color.parseColor("#ff5050"));
        } else if (pm10 >= 100 && pm10 < 150) {
            viewPM10Title.setText(getString(R.string.eaqi_classification_pm_10, getString(R.string.very_poor)));
            viewPM10.setBackgroundColor(Color.parseColor("#960032"));
        } else if (pm10 >= 150 && pm10 <= 1200) {
            viewPM10Title.setText(getString(R.string.eaqi_classification_pm_10, getString(R.string.extremely_poor)));
            viewPM10.setBackgroundColor(Color.parseColor("#7d2181"));
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

