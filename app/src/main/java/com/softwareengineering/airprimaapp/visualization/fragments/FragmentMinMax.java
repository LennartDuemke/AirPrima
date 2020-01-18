package com.softwareengineering.airprimaapp.visualization.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.softwareengineering.airprimaapp.R;
import com.softwareengineering.airprimaapp.other.DatabaseHandler;
import com.softwareengineering.airprimaapp.visualization.Date;
import com.softwareengineering.airprimaapp.visualization.DatepickerDialog;
import com.softwareengineering.airprimaapp.visualization.DatepickerListener;
import com.softwareengineering.airprimaapp.visualization.Sensor;
import com.softwareengineering.airprimaapp.visualization.TimePeriodPickerDialog;
import com.softwareengineering.airprimaapp.visualization.TimePeriodPickerListener;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment in VisualizationActivity: Shows the minimal and maximal values of a specific time period.
 */
public class FragmentMinMax extends Fragment {

    private TextView fragmentTitle;
    private Button timePeriodButton;
    private TextView upperTitle;
    private TextView upperValue;
    private TextView lowerTitle;
    private TextView lowerValue;
    private TextView noDataText;

    private DatabaseHandler dbHandler;
    private float minValue;
    private float minValue2;
    private float maxValue;
    private float maxValue2;

    private long locationID;
    private boolean isConnected;
    private Sensor sensorType;
    private Date dateType;

    private List<String> shortMonthNames = new ArrayList<>();

    /**
     * Method to initialize fragment
     */
    public static FragmentMinMax newInstance(long locationID, Sensor sensorType, boolean isConnected) {
        Bundle bundle = new Bundle();
        bundle.putLong("id", locationID);
        bundle.putSerializable("sensor", sensorType);
        bundle.putBoolean("connected", isConnected);

        FragmentMinMax fragment = new FragmentMinMax();
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     * Setup for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_min_max, container, false);
    }

    /**
     * Triggered after onCreateView(). It is for view setup.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentTitle = view.findViewById(R.id.min_max_title);
        timePeriodButton = view.findViewById(R.id.min_max_time_period_button);
        upperTitle = view.findViewById(R.id.min_max_upper_title);
        upperValue = view.findViewById(R.id.min_max_upper_value);
        lowerTitle = view.findViewById(R.id.min_max_lower_title);
        lowerValue = view.findViewById(R.id.min_max_lower_value);

        noDataText = view.findViewById(R.id.min_max_no_data);
        noDataText.setVisibility(View.GONE);

        shortMonthNames.add(getString(R.string.january_short));
        shortMonthNames.add(getString(R.string.february_short));
        shortMonthNames.add(getString(R.string.march_short));
        shortMonthNames.add(getString(R.string.april_short));
        shortMonthNames.add(getString(R.string.may_short));
        shortMonthNames.add(getString(R.string.june_short));
        shortMonthNames.add(getString(R.string.july_short));
        shortMonthNames.add(getString(R.string.august_short));
        shortMonthNames.add(getString(R.string.september_short));
        shortMonthNames.add(getString(R.string.october_short));
        shortMonthNames.add(getString(R.string.november_short));
        shortMonthNames.add(getString(R.string.december_short));

        dbHandler = new DatabaseHandler(view.getContext());
        dateType = Date.DAY;

        // Read the bundle data
        Bundle arguments = getArguments();
        if (arguments != null) {
            locationID = arguments.getLong("id");
            sensorType = (Sensor) arguments.getSerializable("sensor");
            isConnected = arguments.getBoolean("connected");

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

        // Set time period picker button text
        setTimePeriodButton(true, null, null, null, null);

        // Set fragment title text and lower/ upper titles
        switch (sensorType) {
            case FINEDUST:
                fragmentTitle.setText(getString(R.string.min_max_title, getString(R.string.finedust)));
                lowerTitle.setText(getString(R.string.min_max_lower_title, getString(R.string.finedust)));
                upperTitle.setText(getString(R.string.min_max_upper_title, getString(R.string.finedust)));
                break;
            case TEMPERATURE:
                fragmentTitle.setText(getString(R.string.min_max_title, getString(R.string.temperature)));
                lowerTitle.setText(getString(R.string.min_max_lower_title, getString(R.string.temperature)));
                upperTitle.setText(getString(R.string.min_max_upper_title, getString(R.string.temperature)));
                break;
            case HUMIDITY:
                fragmentTitle.setText(getString(R.string.min_max_title, getString(R.string.humidity)));
                lowerTitle.setText(getString(R.string.min_max_lower_title, getString(R.string.humidity)));
                upperTitle.setText(getString(R.string.min_max_upper_title, getString(R.string.humidity)));
                break;
        }

        setupButtonListener();
        lookUpDateAndGetValues(true, null, null, null, null);   // Newest measurements
    }

    /**
     * Set the text of the time period picker button
     */
    private void setTimePeriodButton(boolean empty, String year, String week, String month, String day) {

        String tmpDate = "";

        if(empty) {
            tmpDate = getString(R.string.min_max_time_period_button);

        } else {
            switch (dateType) {
                case DAY:
                    tmpDate = day + "." + month + "." + year;
                    break;
                case WEEK:
                    int weekInt = Integer.parseInt(week);
                    weekInt++;
                    if (weekInt < 10) {
                        week = "0" + weekInt;
                    } else {
                        week = String.valueOf(weekInt);
                    }
                    tmpDate = getString(R.string.calendar_week_shortform) + " " + week + " " + year;
                    break;
                case MONTH:
                    tmpDate = shortMonthNames.get(Integer.parseInt(month) - 1) + " " + year;
                    break;
                case YEAR:
                    tmpDate = year;
                    break;
            }
        }
        timePeriodButton.setText(tmpDate);
    }

    /**
     * Set the onClickListener for the time period picker button
     */
    private void setupButtonListener() {

        // Choose a time period
        timePeriodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePeriodPickerDialog dialog = new TimePeriodPickerDialog(getActivity());
                dialog.setListener(new TimePeriodPickerListener() {
                    @Override
                    public void dialogValueReturn(Date dateEnum) {

                        // Set global date enum
                        dateType = dateEnum;

                        // Choose a date (day, week, month, year)
                        DatepickerDialog dialog2 = new DatepickerDialog(getActivity(), dateEnum, locationID);
                        dialog2.setListener(new DatepickerListener() {
                            @Override
                            public void dialogValueReturn(String year, String week, String month, String day) {
                                lookUpDateAndGetValues(false, year, week, month, day);
                            }
                        });
                        dialog2.show();
                        // Important hack! Makes the dialog wider!!!!
                        dialog2.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    }
                });
                dialog.show();
                // Important hack! Makes the dialog wider!!!!
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
    }

    /**
     * Looks up the date from the date picker and finds the min/max values OR Looks up the newest min/max values in the DB
     */
    private void lookUpDateAndGetValues(boolean newest, String year, String week, String month, String day) {

        Cursor cursor = null;

        if (newest) {
            cursor = dbHandler.queryNewestDayMinMax(locationID);    // Will always query the newest day at start!

        } else {
            switch (dateType) {
                case DAY:
                    cursor = dbHandler.queryMinMaxDay(locationID, year, month, day);
                    break;
                case WEEK:
                    cursor = dbHandler.queryMinMaxWeek(locationID, year, week);
                    break;
                case MONTH:
                    cursor = dbHandler.queryMinMaxMonth(locationID, year, month);
                    break;
                case YEAR:
                    cursor = dbHandler.queryMinMaxYear(locationID, year);
                    break;
            }
        }

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                switch (sensorType) {
                    case FINEDUST:
                        minValue = cursor.getFloat(cursor.getColumnIndex("min2_5"));
                        maxValue = cursor.getFloat(cursor.getColumnIndex("max2_5"));
                        minValue2 = cursor.getFloat(cursor.getColumnIndex("min10"));
                        maxValue2 = cursor.getFloat(cursor.getColumnIndex("max10"));
                        break;
                    case TEMPERATURE:
                        minValue = cursor.getFloat(cursor.getColumnIndex("minTemp"));
                        maxValue = cursor.getFloat(cursor.getColumnIndex("maxTemp"));
                        break;
                    case HUMIDITY:
                        minValue = cursor.getFloat(cursor.getColumnIndex("minHum"));
                        maxValue = cursor.getFloat(cursor.getColumnIndex("maxHum"));
                        break;
                }

                if (newest) {
                    // Get newest measurement
                    Cursor newestM = dbHandler.queryNewestMeasurement(locationID);
                    if (newestM != null) {
                        if(newestM.getCount() > 0) {
                            newestM.moveToFirst();

                            year = newestM.getString(newestM.getColumnIndex("year"));
                            month = newestM.getString(newestM.getColumnIndex("month"));
                            week = newestM.getString(newestM.getColumnIndex("week"));
                            day = newestM.getString(newestM.getColumnIndex("day"));

                            upperTitle.setVisibility(View.VISIBLE);
                            upperValue.setVisibility(View.VISIBLE);
                            lowerTitle.setVisibility(View.VISIBLE);
                            lowerValue.setVisibility(View.VISIBLE);
                            noDataText.setVisibility(View.GONE);
                            setTimePeriodButton(false, year, week, month, day);

                            insertMinMaxValues();

                        } else {
                            noMeasurementsSetup();
                        }
                        newestM.close();
                    }
                } else {
                    upperTitle.setVisibility(View.VISIBLE);
                    upperValue.setVisibility(View.VISIBLE);
                    lowerTitle.setVisibility(View.VISIBLE);
                    lowerValue.setVisibility(View.VISIBLE);
                    noDataText.setVisibility(View.GONE);
                    setTimePeriodButton(false, year, week, month, day);

                    insertMinMaxValues();
                }
            } else {
                noMeasurementsSetup();
            }
            cursor.close();
        }
    }

    /**
     * When there are no measurements
     */
    private void noMeasurementsSetup() {
        upperTitle.setVisibility(View.GONE);
        upperValue.setVisibility(View.GONE);
        lowerTitle.setVisibility(View.GONE);
        lowerValue.setVisibility(View.GONE);
        noDataText.setVisibility(View.VISIBLE);
        setTimePeriodButton(true, null, null, null, null);
    }

    /**
     * Inserts the min max values into the views
     */
    private void insertMinMaxValues() {
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.HALF_UP);
        switch (sensorType) {
            case FINEDUST:
                upperValue.setText(getString(R.string.min_max_finedust_values, df.format(minValue), df.format(minValue2)));
                lowerValue.setText(getString(R.string.min_max_finedust_values, df.format(maxValue), df.format(maxValue2)));
                break;
            case TEMPERATURE:
                upperValue.setText(getString(R.string.temperature_value_celsius, df.format(minValue)));
                lowerValue.setText(getString(R.string.temperature_value_celsius, df.format(maxValue)));
                break;
            case HUMIDITY:
                upperValue.setText(getString(R.string.humidity_value, df.format(minValue)));
                lowerValue.setText(getString(R.string.humidity_value, df.format(maxValue)));
                break;
        }
    }
}