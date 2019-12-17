package com.softwareengineering.airprimaapp;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.anychart.scales.DateTime;

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

    private DatabaseHandler dbHandler;

    private List<String> timestampList = new ArrayList<>();
    private List<Double> pm2_5List = new ArrayList<>();
    private List<Double> pm10List = new ArrayList<>();
    private List<Double> tempList = new ArrayList<>();
    private List<Double> humidList = new ArrayList<>();

    private Cartesian cartesian;
    private Set set;

    private long locationID;
    private Sensor sensorType;
    private Date dateType;
    private boolean init = true;

    /**
     * Method to initialize fragment
     */
    static FragmentDevelopment newInstance(long locationID, Sensor sensorType, Date dateType) {
        Bundle bundle = new Bundle();
        bundle.putLong("id", locationID);
        bundle.putSerializable("sensor", sensorType);
        bundle.putSerializable("date", dateType);

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

        anyChartView = view.findViewById(R.id.development_diagram);
        APIlib.getInstance().setActiveAnyChartView(anyChartView);   // Nothing works without this ¯\(°_o)/¯ ????

        dbHandler = new DatabaseHandler(view.getContext());

        // Read the bundle data
        Bundle arguments = getArguments();
        if (arguments != null) {
            locationID = arguments.getLong("id");
            sensorType = (Sensor) arguments.getSerializable("sensor");
            dateType = (Date) arguments.getSerializable("date");

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

        // Set date picker button text + fragment title text
        switch(dateType) {
            case YEAR:
                setDateButton(getString(R.string.development_date_button_empty, getString(R.string.year)));
                switch(sensorType) {
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
                setDateButton(getString(R.string.development_date_button_empty, getString(R.string.month)));
                switch(sensorType) {
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
                setDateButton(getString(R.string.development_date_button_empty, getString(R.string.week)));
                switch(sensorType) {
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
                setDateButton(getString(R.string.development_date_button_empty, getString(R.string.day)));
                switch(sensorType) {
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
    private void setDateButton(String strDate) {
        datePickerButton.setText(strDate);
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
        cartesian.crosshair().enabled(false);       // Vertical line that appears when user presses on data
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

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
                dialog.setListener( new DatepickerListener()
                {
                    @Override
                    public void dialogValueReturn(String year, String week, String month, String day) {
                        switch (dateType) {
                            case DAY:
                                setDateButton(day + "." + month + "." + year);
                                break;
                            case WEEK:
                                setDateButton(getString(R.string.calendar_week_shortform) + week + " " + year);
                                break;
                            case MONTH:
                                setDateButton(month + "." + year);
                                break;
                            case YEAR:
                                setDateButton(year);
                                break;
                        }
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

        if(newest) {
            switch(dateType) {
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
            switch(dateType) {
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
        generateDiagram();
    }

    private void generateDiagram() {

        APIlib.getInstance().setActiveAnyChartView(anyChartView);   // Nothing works without this ¯\(°_o)/¯ ????

        List<DataEntry> seriesData = new ArrayList<>();

        switch(sensorType) {
            case FINEDUST:
                for(int i = 0; i < timestampList.size(); i++) {
                    seriesData.add(new CustomDataEntry(timestampList.get(i), pm2_5List.get(i), pm10List.get(i)));
                }
                break;
            case TEMPERATURE:
                for(int i = 0; i < timestampList.size(); i++) {
                    seriesData.add(new ValueDataEntry(timestampList.get(i), tempList.get(i)));
                }
                break;
            case HUMIDITY:
                for(int i = 0; i < timestampList.size(); i++) {
                    seriesData.add(new ValueDataEntry(timestampList.get(i), humidList.get(i)));
                }
                break;
        }

        // Draw the diagram
        set.data(seriesData);

        if(init) {
            init = false;

            Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
            Line series1 = cartesian.line(series1Mapping);
            series1.stroke("rgb(235, 5, 189)");
            series1.name("PM2.5");
            series1.markers().enabled(false);
            series1.tooltip().enabled(false);

            if(sensorType == Sensor.FINEDUST) {
                Mapping series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }");
                Line series2 = cartesian.line(series2Mapping);
                series2.stroke("rgb(3, 148, 13)");
                series2.name("PM10");
                series2.markers().enabled(false);
                series2.tooltip().enabled(false);
                cartesian.legend().enabled(true);
                cartesian.legend().fontSize(13d);
                cartesian.legend().padding(0d, 0d, 10d, 0d);
            }

            anyChartView.setChart(cartesian);
        }
    }

    private class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(String x, Number value, Number value2) {
            super(x, value);
            setValue("value2", value2);
        }
    }
}