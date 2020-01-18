package com.softwareengineering.airprimaapp.visualization.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.SelectionMode;
import com.anychart.scales.DateTime;
import com.softwareengineering.airprimaapp.R;
import com.softwareengineering.airprimaapp.other.DatabaseHandler;
import com.softwareengineering.airprimaapp.visualization.Date;
import com.softwareengineering.airprimaapp.visualization.DatepickerDialog;
import com.softwareengineering.airprimaapp.visualization.DatepickerListener;
import com.softwareengineering.airprimaapp.visualization.Sensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment in VisualizationActivity: Shows the data development of a specific time period.
 */
public class FragmentDevelopment extends Fragment {

    private TextView fragmentTitle;
    private Button datePickerButton;
    private AnyChartView anyChartView;
    private ProgressBar progressBar;
    private TextView noDataText;

    private DatabaseHandler dbHandler;

    private List<String> timestampList = new ArrayList<>();
    private List<Double> pm2_5List = new ArrayList<>();
    private List<Double> pm10List = new ArrayList<>();
    private List<Double> tempList = new ArrayList<>();
    private List<Double> humidList = new ArrayList<>();

    private List<String> processedTimestampList = new ArrayList<>();
    private List<Double> processedPm2_5List = new ArrayList<>();
    private List<Double> processedPm10List = new ArrayList<>();
    private List<Double> processedTempList = new ArrayList<>();
    private List<Double> processedHumidList = new ArrayList<>();

    private Cartesian cartesian;
    private Set set;

    private long locationID;
    private boolean isConnected;
    private Sensor sensorType;
    private Date dateType;

    private boolean init = true;
    private List<String> shortMonthNames = new ArrayList<>();

    /**
     * Method to initialize fragment
     */
    public static FragmentDevelopment newInstance(long locationID, Sensor sensorType, Date dateType, boolean isConnected) {
        Bundle bundle = new Bundle();
        bundle.putLong("id", locationID);
        bundle.putSerializable("sensor", sensorType);
        bundle.putSerializable("date", dateType);
        bundle.putBoolean("connected", isConnected);

        FragmentDevelopment fragment = new FragmentDevelopment();
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     * Setup for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_development, container, false);
    }

    /**
     * Triggered after onCreateView(). It is for view setup.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentTitle = view.findViewById(R.id.development_title);
        datePickerButton = view.findViewById(R.id.development_date_button);
        progressBar = view.findViewById(R.id.development_progressbar);

        noDataText = view.findViewById(R.id.development_no_data);
        noDataText.setVisibility(View.GONE);

        anyChartView = view.findViewById(R.id.development_diagram);
        APIlib.getInstance().setActiveAnyChartView(anyChartView);   // Important! Fixes one chart per activity problem!

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

        // Read the bundle data
        Bundle arguments = getArguments();
        if (arguments != null) {
            locationID = arguments.getLong("id");
            sensorType = (Sensor) arguments.getSerializable("sensor");
            dateType = (Date) arguments.getSerializable("date");
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

        // Set date picker button text
        setDateButton(true, null, null, null, null);

        // Fragment title text
        switch (dateType) {
            case YEAR:
                switch (sensorType) {
                    case FINEDUST:
                        fragmentTitle.setText(getString(R.string.development_title_year, getString(R.string.finedust)));
                        break;
                    case TEMPERATURE:
                        fragmentTitle.setText(getString(R.string.development_title_year, getString(R.string.temperature)));
                        break;
                    case HUMIDITY:
                        fragmentTitle.setText(getString(R.string.development_title_year, getString(R.string.humidity)));
                        break;
                }
                break;
            case MONTH:
                switch (sensorType) {
                    case FINEDUST:
                        fragmentTitle.setText(getString(R.string.development_title_month, getString(R.string.finedust)));
                        break;
                    case TEMPERATURE:
                        fragmentTitle.setText(getString(R.string.development_title_month, getString(R.string.temperature)));
                        break;
                    case HUMIDITY:
                        fragmentTitle.setText(getString(R.string.development_title_month, getString(R.string.humidity)));
                        break;
                }
                break;
            case WEEK:
                switch (sensorType) {
                    case FINEDUST:
                        fragmentTitle.setText(getString(R.string.development_title_week, getString(R.string.finedust)));
                        break;
                    case TEMPERATURE:
                        fragmentTitle.setText(getString(R.string.development_title_week, getString(R.string.temperature)));
                        break;
                    case HUMIDITY:
                        fragmentTitle.setText(getString(R.string.development_title_week, getString(R.string.humidity)));
                        break;
                }
                break;
            case DAY:
                switch (sensorType) {
                    case FINEDUST:
                        fragmentTitle.setText(getString(R.string.development_title_day, getString(R.string.finedust)));
                        break;
                    case TEMPERATURE:
                        fragmentTitle.setText(getString(R.string.development_title_day, getString(R.string.temperature)));
                        break;
                    case HUMIDITY:
                        fragmentTitle.setText(getString(R.string.development_title_day, getString(R.string.humidity)));
                        break;
                }
                break;
        }

        setupDiagram();
        setupButtonListener();
        lookUpDateAndFillList(true, null, null, null, null);   // Newest measurements
    }

    /**
     * Set the text of the date picker button
     */
    private void setDateButton(boolean empty, String year, String week, String month, String day) {

        String tmpDate = "";

        if(empty) {
            switch (dateType) {
                case YEAR:
                    tmpDate = getString(R.string.date_button_empty, getString(R.string.year));
                    break;
                case MONTH:
                    tmpDate = getString(R.string.date_button_empty, getString(R.string.month));
                    break;
                case WEEK:
                    tmpDate = getString(R.string.date_button_empty, getString(R.string.week));
                    break;
                case DAY:
                    tmpDate = getString(R.string.date_button_empty, getString(R.string.day));
                    break;
            }
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
        datePickerButton.setText(tmpDate);
    }

    /**
     * Setup for the diagram
     */
    private void setupDiagram() {

        // Set progressbar of diagram
        anyChartView.setProgressBar(progressBar);

        // Setup for the cartesian coordinate system of the diagram
        cartesian = AnyChart.line();
        cartesian.xScale(DateTime.instantiate());   // Makes the x axis a date-time axis (uses date strings)
        cartesian.animation(true);
        cartesian.padding(5d, 15d, 50d, 5d);        // Distance to monitor sides
        cartesian.crosshair(false);
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);
        cartesian.interactivity().selectionMode(SelectionMode.NONE);

        set = Set.instantiate();
    }

    /**
     * Set the onClickListener for the date picker button
     */
    private void setupButtonListener() {

        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatepickerDialog dialog = new DatepickerDialog(getActivity(), dateType, locationID);
                dialog.setListener(new DatepickerListener() {
                    @Override
                    public void dialogValueReturn(String year, String week, String month, String day) {
                        lookUpDateAndFillList(false, year, week, month, day);
                    }
                });
                dialog.show();
                // Important hack! Makes the dialog wider!!!!
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
    }

    /**
     * Looks up the date from the date picker and fills measurement lists OR Looks up the newest measurements in the DB
     */
    private void lookUpDateAndFillList(boolean newest, String year, String week, String month, String day) {

        timestampList.clear();
        pm2_5List.clear();
        pm10List.clear();
        tempList.clear();
        humidList.clear();

        Cursor cursor = null;

        if (newest) {
            switch (dateType) {
                case DAY:
                    cursor = dbHandler.queryNewestDay(locationID);
                    break;
                case WEEK:
                    cursor = dbHandler.queryNewestWeek(locationID);
                    break;
                case MONTH:
                    cursor = dbHandler.queryNewestMonth(locationID);
                    break;
                case YEAR:
                    cursor = dbHandler.queryNewestYear(locationID);
                    break;
            }
        } else {
            switch (dateType) {
                case DAY:
                    cursor = dbHandler.queryDay(locationID, year, month, day);
                    break;
                case WEEK:
                    cursor = dbHandler.queryWeek(locationID, year, week);
                    break;
                case MONTH:
                    cursor = dbHandler.queryMonth(locationID, year, month);
                    break;
                case YEAR:
                    cursor = dbHandler.queryYear(locationID, year);
                    break;
            }
        }

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {

                    switch (sensorType) {
                        case FINEDUST:
                            String tmpTimestamp = cursor.getString(cursor.getColumnIndex("measurement_timestamp_fmt"));
                            timestampList.add(tmpTimestamp);
                            Double tmpPM2_5 = cursor.getDouble(cursor.getColumnIndex("measurement_pm_2_5"));
                            pm2_5List.add(tmpPM2_5);
                            Double tmpPM10 = cursor.getDouble(cursor.getColumnIndex("measurement_pm_10"));
                            pm10List.add(tmpPM10);
                            break;
                        case TEMPERATURE:
                            String tmpTimestamp2 = cursor.getString(cursor.getColumnIndex("measurement_timestamp_fmt"));
                            timestampList.add(tmpTimestamp2);
                            Double tmpTemp = cursor.getDouble(cursor.getColumnIndex("measurement_temp"));
                            tempList.add(tmpTemp);
                            break;
                        case HUMIDITY:
                            String tmpTimestamp3 = cursor.getString(cursor.getColumnIndex("measurement_timestamp_fmt"));
                            timestampList.add(tmpTimestamp3);
                            Double tmpHumid = cursor.getDouble(cursor.getColumnIndex("measurement_hum"));
                            humidList.add(tmpHumid);
                            break;
                    }
                }
            } finally {
                cursor.close();
            }
        }

        // Check if measurement lists are empty
        if(timestampList.isEmpty()) {

            anyChartView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            noDataText.setVisibility(View.VISIBLE);
            setDateButton(true, null, null, null, null);

        } else {

            if(newest) {
                // Get newest measurement
                Cursor newestM = dbHandler.queryNewestMeasurement(locationID);
                if(newestM != null) {
                    newestM.moveToFirst();
                    year = newestM.getString(newestM.getColumnIndex("year"));
                    month = newestM.getString(newestM.getColumnIndex("month"));
                    week = newestM.getString(newestM.getColumnIndex("week"));
                    day = newestM.getString(newestM.getColumnIndex("day"));
                    newestM.close();
                }
            }

            anyChartView.setVisibility(View.VISIBLE);
            noDataText.setVisibility(View.GONE);
            setDateButton(false, year, week, month, day);

            processMeasurements();
        }
    }

    /**
     * Reduces the size of the lists by calculating averages for whole days
     */
    private void processMeasurements() {

        processedTimestampList.clear();
        processedPm2_5List.clear();
        processedPm10List.clear();
        processedTempList.clear();
        processedHumidList.clear();

        if(dateType == Date.DAY) {  // No changes with daily development

            for(int i = 0; i < timestampList.size(); i++) {
                processedTimestampList.add(timestampList.get(i));
                switch (sensorType) {
                    case FINEDUST:
                        processedPm2_5List.add(pm2_5List.get(i));
                        processedPm10List.add(pm10List.get(i));
                        break;
                    case TEMPERATURE:
                        processedTempList.add(tempList.get(i));
                        break;
                    case HUMIDITY:
                        processedHumidList.add(humidList.get(i));
                        break;
                }
            }
            generateDiagram();

        } else {    // Calculate average for each day

            int measurementCounter = 0;
            String tmpTimestamp;
            Double tmpPm2_5 = 0.0;
            Double tmpPm10 = 0.0;
            Double tmpTemp = 0.0;
            Double tmpHumid = 0.0;

            String previousDay = getDayFromTimestring(timestampList.get(0));
            String previousMonth = getMonthFromTimestring(timestampList.get(0));
            String previousYear = getYearFromTimestring(timestampList.get(0));

            for(int i = 0; i < timestampList.size(); i++) {

                String currentMonth = getMonthFromTimestring(timestampList.get(i));
                String currentDay = getDayFromTimestring(timestampList.get(i));

                if(currentDay.equals(previousDay) && currentMonth.equals(previousMonth)) {  // Same day

                    measurementCounter++;

                    switch (sensorType) {
                        case FINEDUST:
                            tmpPm2_5 += pm2_5List.get(i);
                            tmpPm10 += pm10List.get(i);
                            break;
                        case TEMPERATURE:
                            tmpTemp += tempList.get(i);
                            break;
                        case HUMIDITY:
                            tmpHumid += humidList.get(i);
                            break;
                    }

                    previousDay = currentDay;
                    previousMonth = currentMonth;

                } else {    // Next day

                    tmpTimestamp = previousYear + "-" + previousMonth + "-" + previousDay + " 01:01:01";
                    processedTimestampList.add(tmpTimestamp);
                    switch (sensorType) {
                        case FINEDUST:
                            processedPm2_5List.add(tmpPm2_5 / measurementCounter);
                            processedPm10List.add(tmpPm10 / measurementCounter);
                            break;
                        case TEMPERATURE:
                            processedTempList.add(tmpTemp / measurementCounter);
                            break;
                        case HUMIDITY:
                            processedHumidList.add(tmpHumid / measurementCounter);
                            break;
                    }

                    measurementCounter = 1;
                    switch (sensorType) {
                        case FINEDUST:
                            tmpPm2_5 = pm2_5List.get(i);
                            tmpPm10 = pm10List.get(i);
                            break;
                        case TEMPERATURE:
                            tmpTemp = tempList.get(i);
                            break;
                        case HUMIDITY:
                            tmpHumid = humidList.get(i);
                            break;
                    }

                    previousDay = currentDay;
                    previousMonth = currentMonth;
                }

                if(i == timestampList.size() - 1) { // Termination condition: last index reached
                    tmpTimestamp = previousYear + "-" + previousMonth + "-" + previousDay + " 01:01:01";
                    processedTimestampList.add(tmpTimestamp);
                    switch (sensorType) {
                        case FINEDUST:
                            processedPm2_5List.add(tmpPm2_5 / measurementCounter);
                            processedPm10List.add(tmpPm10 / measurementCounter);
                            break;
                        case TEMPERATURE:
                            processedTempList.add(tmpTemp / measurementCounter);
                            break;
                        case HUMIDITY:
                            processedHumidList.add(tmpHumid / measurementCounter);
                            break;
                    }
                }
            }
            generateDiagram();
        }
    }

    /**
     * Returns the year from a timestring (YYYY-MM-DD HH:MM:SS)
     */
    public String getYearFromTimestring(String timestring) {
        return timestring.substring(0, 4);
    }

    /**
     * Returns the month from a timestring (YYYY-MM-DD HH:MM:SS)
     */
    public String getMonthFromTimestring(String timestring) {
        return timestring.substring(5, 7);
    }

    /**
     * Returns the day from a timestring (YYYY-MM-DD HH:MM:SS)
     */
    public String getDayFromTimestring(String timestring) {
        return timestring.substring(8, 10);
    }

    /**
     * Generates the charts from the lists
     */
    private void generateDiagram() {

        APIlib.getInstance().setActiveAnyChartView(anyChartView);   // Important! Fixes one chart per activity problem!
        List<DataEntry> seriesData = new ArrayList<>();

        switch (sensorType) {
            case FINEDUST:
                for (int i = 0; i < processedTimestampList.size(); i++) {
                    seriesData.add(new CustomDataEntry(processedTimestampList.get(i), processedPm2_5List.get(i), processedPm10List.get(i)));
                }
                break;
            case TEMPERATURE:
                for (int i = 0; i < processedTimestampList.size(); i++) {
                    seriesData.add(new ValueDataEntry(processedTimestampList.get(i), processedTempList.get(i)));
                }
                break;
            case HUMIDITY:
                for (int i = 0; i < processedTimestampList.size(); i++) {
                    seriesData.add(new ValueDataEntry(processedTimestampList.get(i), processedHumidList.get(i)));
                }
                break;
        }

        // Draw the diagram
        set.data(seriesData);

        if (init) {
            init = false;

            Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
            Line series1 = cartesian.line(series1Mapping);
            series1.stroke("rgb(235, 5, 189)");
            series1.name("PM2.5");
            series1.markers(false);
            series1.hovered().markers().enabled(false);
            series1.tooltip(false);

            if (sensorType == Sensor.FINEDUST) {    // Shows two lines for PM2.5 and PM10

                Mapping series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }");
                Line series2 = cartesian.line(series2Mapping);
                series2.stroke("rgb(3, 148, 13)");
                series2.name("PM10");
                series2.markers(false);
                series2.hovered().markers().enabled(false);
                series2.tooltip(false);

                cartesian.legend(true);
                cartesian.legend().fontSize(13d);
                cartesian.legend().padding(0, 0, 10, 40);
                cartesian.legend().position("top");
                cartesian.legend().align("left");
            }

            /* X axis settings
            DateTime dateTime = (DateTime) cartesian.xScale(DateTime.class);
            dateTime.ticks().interval("m", 1); */

            anyChartView.setChart(cartesian);
        }
    }

    /**
     * Custom data entry for chart. Needed in order to show both PM2.5 and PM10 in one chart!
     */
    private class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(String x, Number value, Number value2) {
            super(x, value);
            setValue("value2", value2);
        }
    }
}