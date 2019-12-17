package com.softwareengineering.airprimaapp;

import android.app.Activity;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * This dialog allows the user to pick a date. The date can be a year, month, week or day.
 */
public class DatepickerDialog extends Dialog {

    private Activity activity;
    private Date dateEnum;
    private DatabaseHandler dbHandler;
    private long locationID;
    private DatepickerListener listener;

    private TextView title;
    private LinearLayout column2;
    private LinearLayout column3;
    private Spinner spinnerYear;
    private Spinner spinnerMonthWeek;
    private Spinner spinnerDay;
    private TextView monthWeekTitle;
    private Button select;
    private TextView noDataText;
    private LinearLayout spinnerContainer;

    private boolean initSelectSpinnerYear = true;
    private boolean initSelectSpinnerMonth = true;

    /**
     * Constructor
     */
    DatepickerDialog(Activity activity, Date dateEnum, long locationID) {
        super(activity);
        this.activity = activity;
        this.dateEnum = dateEnum;
        this.locationID = locationID;
    }

    /**
     * Setup for the dialog
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_datepicker);

        title = findViewById(R.id.dialog_datepicker_title);
        column2 = findViewById(R.id.dialog_datepicker_column2);
        column3 = findViewById(R.id.dialog_datepicker_column3);
        spinnerYear = findViewById(R.id.dialog_datepicker_spinner_year);
        spinnerMonthWeek = findViewById(R.id.dialog_datepicker_spinner_month);
        spinnerDay = findViewById(R.id.dialog_datepicker_spinner_day_week);
        monthWeekTitle = findViewById(R.id.dialog_datepicker_month_week_title);
        Button cancel = findViewById(R.id.dialog_datepicker_cancel);
        select = findViewById(R.id.dialog_datepicker_select);
        spinnerContainer = findViewById(R.id.dialog_datepicker_spinner_container);

        // Text that will be shown, when DB is empty for specific locationID
        noDataText = findViewById(R.id.dialog_datepicker_no_data_text);
        noDataText.setVisibility(View.GONE);

        dbHandler = new DatabaseHandler(activity);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDialog();
            }
        });

        switch (dateEnum) {
            case YEAR:
                configureYearDialog();
                break;
            case WEEK:                      // Calendar week
                configureWeekDialog();
                break;
            case MONTH:
                configureMonthDialog();
                break;
            case DAY:
                configureDayDialog();
                break;
        }
    }

    /**
     * Setter for the listener
     */
    void setListener(DatepickerListener listener) {
        this.listener = listener;
    }

    /**
     * Clean up on dialog close.
     */
    private void closeDialog() {
        dbHandler.close();
        dismiss();
    }

    /**
     * Configure a dialog that lets the user pick a year
     */
    private void configureYearDialog() {

        title.setText(R.string.dialog_datepicker_title_year);
        column2.setVisibility(View.GONE);
        column3.setVisibility(View.GONE);

        queryAndAddYears();
        if (spinnerYear.getCount() != 0) {

            select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.dialogValueReturn(String.valueOf(spinnerYear.getSelectedItem()), null, null, null);
                    closeDialog();
                }
            });

        } else {
            spinnerContainer.setVisibility(View.GONE);
            noDataText.setVisibility(View.VISIBLE);
            select.setVisibility(View.GONE);
        }
    }

    /**
     * Configure a dialog that lets the user pick a calendar week
     */
    private void configureWeekDialog() {

        title.setText(R.string.dialog_datepicker_title_week);
        monthWeekTitle.setText(R.string.calendar_week);
        column3.setVisibility(View.GONE);

        queryAndAddYears();
        if (spinnerYear.getCount() != 0) {

            String tmpYear = String.valueOf(spinnerYear.getSelectedItem());
            queryAndAddWeeks(tmpYear);

            select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.dialogValueReturn(String.valueOf(spinnerYear.getSelectedItem()),
                            String.valueOf(spinnerMonthWeek.getSelectedItem()), null, null);
                    closeDialog();
                }
            });

            // When user changes the year, the calendar weeks have to be looked up in DB
            spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                    if (!initSelectSpinnerYear) {
                        String tmpYear = String.valueOf(spinnerYear.getSelectedItem());
                        queryAndAddWeeks(tmpYear);
                    } else {
                        initSelectSpinnerYear = false;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }

            });

        } else {
            spinnerContainer.setVisibility(View.GONE);
            noDataText.setVisibility(View.VISIBLE);
            select.setVisibility(View.GONE);
        }
    }

    /**
     * Configure a dialog that lets the user pick a month
     */
    private void configureMonthDialog() {

        title.setText(R.string.dialog_datepicker_title_month);
        monthWeekTitle.setText(R.string.month);
        column3.setVisibility(View.GONE);

        queryAndAddYears();
        if (spinnerYear.getCount() != 0) {

            String tmpYear = String.valueOf(spinnerYear.getSelectedItem());
            queryAndAddMonths(tmpYear);

            select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.dialogValueReturn(String.valueOf(spinnerYear.getSelectedItem()), null,
                            String.valueOf(spinnerMonthWeek.getSelectedItem()), null);
                    closeDialog();
                }
            });

            // When user changes the year, the months have to be looked up in DB
            spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                    if (!initSelectSpinnerYear) {
                        String tmpYear = String.valueOf(spinnerYear.getSelectedItem());
                        queryAndAddMonths(tmpYear);
                    } else {
                        initSelectSpinnerYear = false;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }

            });

        } else {
            spinnerContainer.setVisibility(View.GONE);
            noDataText.setVisibility(View.VISIBLE);
            select.setVisibility(View.GONE);
        }
    }

    /**
     * Configure a dialog that lets the user pick a day
     */
    private void configureDayDialog() {

        title.setText(R.string.dialog_datepicker_title_day);

        queryAndAddYears();
        if (spinnerYear.getCount() != 0) {

            String tmpYear = String.valueOf(spinnerYear.getSelectedItem());
            queryAndAddMonths(tmpYear);
            String tmpMonth = String.valueOf(spinnerMonthWeek.getSelectedItem());
            queryAndAddDays(tmpYear, tmpMonth);

            select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.dialogValueReturn(String.valueOf(spinnerYear.getSelectedItem()), null,
                            String.valueOf(spinnerMonthWeek.getSelectedItem()), String.valueOf(spinnerDay.getSelectedItem()));
                    closeDialog();
                }
            });

            // When user changes the year, the months and days have to be looked up in DB
            spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                    if (!initSelectSpinnerYear) {
                        String tmpYear = String.valueOf(spinnerYear.getSelectedItem());
                        queryAndAddMonths(tmpYear);
                        String tmpMonth = String.valueOf(spinnerMonthWeek.getSelectedItem());
                        queryAndAddDays(tmpYear, tmpMonth);
                    } else {
                        initSelectSpinnerYear = false;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }

            });

            // When user changes the month, the days have to be looked up in DB
            spinnerMonthWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                    if (!initSelectSpinnerMonth) {
                        String tmpYear = String.valueOf(spinnerYear.getSelectedItem());
                        String tmpMonth = String.valueOf(spinnerMonthWeek.getSelectedItem());
                        queryAndAddDays(tmpYear, tmpMonth);
                    } else {
                        initSelectSpinnerMonth = false;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                }

            });

        } else {
            spinnerContainer.setVisibility(View.GONE);
            noDataText.setVisibility(View.VISIBLE);
            select.setVisibility(View.GONE);
        }
    }

    /**
     * Looks for years in the database and inserts the items into the spinner
     */
    private void queryAndAddYears() {

        List<String> yearList = new ArrayList<>();

        Cursor cursor = dbHandler.queryAllYears(locationID);
        if (cursor != null) {

            // Get data from cursor
            try {
                while (cursor.moveToNext()) {
                    String tmpYear = cursor.getString(cursor.getColumnIndex("year"));
                    yearList.add(tmpYear);
                }

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(activity,
                        android.R.layout.simple_spinner_item, yearList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerYear.setAdapter(dataAdapter);

            } finally {
                cursor.close();
            }
        }
    }

    /**
     * Looks for weeks in the database and inserts the items into the spinner
     */
    private void queryAndAddWeeks(String tmpYear) {

        List<String> weekList = new ArrayList<>();

        Cursor cursor = dbHandler.queryAllWeeks(locationID, tmpYear);
        if (cursor != null) {

            // Get data from cursor
            try {
                while (cursor.moveToNext()) {
                    String tmpWeek = cursor.getString(cursor.getColumnIndex("week"));
                    int weekInt = Integer.parseInt(tmpWeek);
                    weekInt++;                                  // Add 1 because the weeks start at 0
                    if (weekInt < 10) {
                        tmpWeek = "0" + weekInt;
                    }
                    weekList.add(tmpWeek);
                }

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(activity,
                        android.R.layout.simple_spinner_item, weekList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerMonthWeek.setAdapter(dataAdapter);

            } finally {
                cursor.close();
            }
        }
    }

    /**
     * Looks for months in the database and inserts the items into the spinner
     */
    private void queryAndAddMonths(String tmpYear) {

        List<String> monthList = new ArrayList<>();

        Cursor cursor = dbHandler.queryAllMonths(locationID, tmpYear);
        if (cursor != null) {

            // Get data from cursor
            try {
                while (cursor.moveToNext()) {
                    String tmpMonth = cursor.getString(cursor.getColumnIndex("month"));
                    monthList.add(tmpMonth);
                }

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(activity,
                        android.R.layout.simple_spinner_item, monthList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerMonthWeek.setAdapter(dataAdapter);

            } finally {
                cursor.close();
            }
        }
    }

    /**
     * Looks for months in the database and inserts the items into the spinner
     */
    private void queryAndAddDays(String year, String month) {

        List<String> dayList = new ArrayList<>();

        Cursor cursor = dbHandler.queryAllDays(locationID, year, month);
        if (cursor != null) {

            // Get data from cursor
            try {
                while (cursor.moveToNext()) {
                    String tmpDay = cursor.getString(cursor.getColumnIndex("day"));
                    dayList.add(tmpDay);
                }

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(activity,
                        android.R.layout.simple_spinner_item, dayList);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDay.setAdapter(dataAdapter);

            } finally {
                cursor.close();
            }
        }
    }
}