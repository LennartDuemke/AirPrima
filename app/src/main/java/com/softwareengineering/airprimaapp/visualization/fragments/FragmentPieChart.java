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
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
import com.anychart.enums.SelectionMode;
import com.softwareengineering.airprimaapp.R;
import com.softwareengineering.airprimaapp.other.DatabaseHandler;
import com.softwareengineering.airprimaapp.visualization.Date;
import com.softwareengineering.airprimaapp.visualization.DatepickerDialog;
import com.softwareengineering.airprimaapp.visualization.DatepickerListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment in VisualizationActivity: Days of a year categorized in the levels of the EAQI
 */
public class FragmentPieChart extends Fragment {

    private TextView chartTitle;
    private Button datePickerButton;
    private AnyChartView anyChartView;
    private ProgressBar progressBar;
    private TextView noDataText;

    private DatabaseHandler dbHandler;

    private List<String> timestampList = new ArrayList<>();
    private List<Double> pm2_5List = new ArrayList<>();
    private List<Double> pm10List = new ArrayList<>();

    private List<String> processedTimestampList = new ArrayList<>();
    private List<Double> processedPm2_5List = new ArrayList<>();
    private List<Double> processedPm10List = new ArrayList<>();

    private Pie pie;
    private List<DataEntry> chartData = new ArrayList<>();

    private long locationID;
    private boolean isConnected;
    private boolean showPm10;
    private boolean init = true;

    /**
     * Method to initialize fragment
     */
    public static FragmentPieChart newInstance(long locationID, boolean pm10, boolean isConnected) {
        Bundle bundle = new Bundle();
        bundle.putLong("id", locationID);
        bundle.putBoolean("pm10", pm10);
        bundle.putBoolean("connected", isConnected);

        FragmentPieChart fragment = new FragmentPieChart();
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     * Setup for the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pie_chart, container, false);
    }

    /**
     * Triggered after onCreateView(). It is for view setup.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chartTitle = view.findViewById(R.id.pie_chart_title);
        datePickerButton = view.findViewById(R.id.pie_chart_date_button);
        progressBar = view.findViewById(R.id.pie_chart_progressbar);

        noDataText = view.findViewById(R.id.pie_chart_no_data);
        noDataText.setVisibility(View.GONE);

        anyChartView = view.findViewById(R.id.pie_chart);
        APIlib.getInstance().setActiveAnyChartView(anyChartView);   // Important! Fixes one chart per activity problem!

        dbHandler = new DatabaseHandler(view.getContext());

        // Read the bundle data
        Bundle arguments = getArguments();
        if (arguments != null) {
            locationID = arguments.getLong("id");
            showPm10 = arguments.getBoolean("pm10");
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
        datePickerButton.setText(getString(R.string.date_button_empty, getString(R.string.year)));

        // Set the title of the fragment
        if(showPm10) {
            chartTitle.setText(getString(R.string.pie_chart_title, "PM10"));
        } else {
            chartTitle.setText(getString(R.string.pie_chart_title, "PM2.5"));
        }

        setupDiagram();
        setupButtonListener();
        lookUpDateAndFillList(true, null, null, null, null);   // Newest measurements
    }

    /**
     * Setup for the pie chart
     */
    private void setupDiagram() {

        anyChartView.setProgressBar(progressBar);

        pie = AnyChart.pie();
        pie.padding(10d, 15d, 40d, 15d);        // Distance to monitor sides
        pie.interactivity().selectionMode(SelectionMode.NONE);
        pie.animation(true);
        pie.legend().title().enabled(true);
        pie.legend().title()
                .text(getString(R.string.pie_chart_legend_name))
                .padding(0d, 0d, 10d, 0d);
        pie.legend()
                .position("center-bottom")
                .itemsLayout(LegendLayout.HORIZONTAL_EXPANDABLE)
                .align(Align.CENTER);
        pie.palette(new String[]{"#50f0e6", "#50ccaa", "#f3eb6a", "#ff5050", "#960032", "#7d2181"});
    }

    /**
     * Set the onClickListener for the date picker button
     */
    private void setupButtonListener() {

        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatepickerDialog dialog = new DatepickerDialog(getActivity(), Date.YEAR, locationID);
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

        Cursor cursor;

        if (newest) {
            if(showPm10) {
                cursor = dbHandler.queryNewestYearPm10(locationID);
            } else {
                cursor = dbHandler.queryNewestYearPm2_5(locationID);
            }

        } else {
            if(showPm10) {
                cursor = dbHandler.queryYearPm10(locationID, year);
            } else {
                cursor = dbHandler.queryYearPm2_5(locationID, year);
            }
        }

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String tmpTimestamp = cursor.getString(cursor.getColumnIndex("measurement_timestamp_fmt"));
                    timestampList.add(tmpTimestamp);
                    if(showPm10) {
                        Double tmpPM10 = cursor.getDouble(cursor.getColumnIndex("measurement_pm_10"));
                        pm10List.add(tmpPM10);
                    } else {
                        Double tmpPM2_5 = cursor.getDouble(cursor.getColumnIndex("measurement_pm_2_5"));
                        pm2_5List.add(tmpPM2_5);
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
            datePickerButton.setText(getString(R.string.date_button_empty, getString(R.string.year)));

        } else {

            if(newest) {
                // Get newest measurement
                Cursor newestM = dbHandler.queryNewestMeasurement(locationID);
                if(newestM != null) {
                    newestM.moveToFirst();
                    year = newestM.getString(newestM.getColumnIndex("year"));
                    newestM.close();
                }
            }

            anyChartView.setVisibility(View.VISIBLE);
            noDataText.setVisibility(View.GONE);
            datePickerButton.setText(year);

            processMeasurements();
        }
    }

    /**
     * Calculating averages for whole days
     */
    private void processMeasurements() {

        processedTimestampList.clear();
        processedPm2_5List.clear();
        processedPm10List.clear();

        int measurementCounter = 0;
        String tmpTimestamp;
        Double tmpPm2_5 = 0.0;
        Double tmpPm10 = 0.0;

        String previousDay = getDayFromTimestring(timestampList.get(0));
        String previousMonth = getMonthFromTimestring(timestampList.get(0));
        String previousYear = getYearFromTimestring(timestampList.get(0));

        for(int i = 0; i < timestampList.size(); i++) {

            String currentMonth = getMonthFromTimestring(timestampList.get(i));
            String currentDay = getDayFromTimestring(timestampList.get(i));

            if(currentDay.equals(previousDay) && currentMonth.equals(previousMonth)) {  // Same day

                measurementCounter++;

                if(showPm10) {
                    tmpPm10 += pm10List.get(i);
                } else {
                    tmpPm2_5 += pm2_5List.get(i);
                }

                previousDay = currentDay;
                previousMonth = currentMonth;

            } else {    // Next day

                tmpTimestamp = previousYear + "-" + previousMonth + "-" + previousDay + " 01:01:01";
                processedTimestampList.add(tmpTimestamp);
                if(showPm10) {
                    processedPm10List.add(tmpPm10 / measurementCounter);
                } else {
                    processedPm2_5List.add(tmpPm2_5 / measurementCounter);
                }

                measurementCounter = 1;
                if(showPm10) {
                    tmpPm10 = pm10List.get(i);
                } else {
                    tmpPm2_5 = pm2_5List.get(i);
                }

                previousDay = currentDay;
                previousMonth = currentMonth;
            }

            if(i == timestampList.size() - 1) { // Termination condition: last index reached
                tmpTimestamp = previousYear + "-" + previousMonth + "-" + previousDay + " 01:01:01";
                processedTimestampList.add(tmpTimestamp);
                if(showPm10) {
                    processedPm10List.add(tmpPm10 / measurementCounter);
                } else {
                    processedPm2_5List.add(tmpPm2_5 / measurementCounter);
                }
            }
        }
        generateDiagram();
    }

    /**
     * Returns the year from a timestring (YYYY-MM-DD HH:MM:SS)
     */
    private String getYearFromTimestring(String timestring) {
        return timestring.substring(0, 4);
    }

    /**
     * Returns the month from a timestring (YYYY-MM-DD HH:MM:SS)
     */
    private String getMonthFromTimestring(String timestring) {
        return timestring.substring(5, 7);
    }

    /**
     * Returns the day from a timestring (YYYY-MM-DD HH:MM:SS)
     */
    private String getDayFromTimestring(String timestring) {
        return timestring.substring(8, 10);
    }

    /**
     * Generates the pie chart from the lists
     */
    private void generateDiagram() {

        APIlib.getInstance().setActiveAnyChartView(anyChartView);   // Important! Fixes one chart per activity problem!

        chartData.clear();

        double goodDaysCount = 0;
        double fairDaysCount = 0;
        double moderateDaysCount = 0;
        double poorDaysCount = 0;
        double veryPoorDaysCount = 0;
        double extremelyPoorDaysCount = 0;

        for(int i = 0; i < processedTimestampList.size(); i++) {

            if(showPm10) {
                double tmpValue = processedPm10List.get(i);

                if(tmpValue >= 0 && tmpValue < 20) {
                    goodDaysCount++;
                } else if(tmpValue >= 20 && tmpValue < 40) {
                    fairDaysCount++;
                } else if(tmpValue >= 40 && tmpValue < 50) {
                    moderateDaysCount++;
                } else if(tmpValue >= 50 && tmpValue < 100) {
                    poorDaysCount++;
                } else if(tmpValue >= 100 && tmpValue < 150) {
                    veryPoorDaysCount++;
                } else if(tmpValue >= 150 && tmpValue <= 1200) {
                    extremelyPoorDaysCount++;
                }

            } else {
                double tmpValue = processedPm2_5List.get(i);

                if(tmpValue >= 0 && tmpValue < 10) {
                    goodDaysCount++;
                } else if(tmpValue >= 10 && tmpValue < 20) {
                    fairDaysCount++;
                } else if(tmpValue >= 20 && tmpValue < 25) {
                    moderateDaysCount++;
                } else if(tmpValue >= 25 && tmpValue < 50) {
                    poorDaysCount++;
                } else if(tmpValue >= 50 && tmpValue < 75) {
                    veryPoorDaysCount++;
                } else if(tmpValue >= 75 && tmpValue <= 800) {
                    extremelyPoorDaysCount++;
                }
            }
        }

        chartData.add(new ValueDataEntry(getString(R.string.good), goodDaysCount));
        chartData.add(new ValueDataEntry(getString(R.string.fair), fairDaysCount));
        chartData.add(new ValueDataEntry(getString(R.string.moderate), moderateDaysCount));
        chartData.add(new ValueDataEntry(getString(R.string.poor), poorDaysCount));
        chartData.add(new ValueDataEntry(getString(R.string.very_poor), veryPoorDaysCount));
        chartData.add(new ValueDataEntry(getString(R.string.extremely_poor), extremelyPoorDaysCount));

        // Draw the diagram
        pie.data(chartData);

        if (init) {
            init = false;
            anyChartView.setChart(pie);
        }
    }
}
