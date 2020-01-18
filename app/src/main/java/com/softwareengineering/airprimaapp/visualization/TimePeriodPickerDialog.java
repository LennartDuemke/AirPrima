package com.softwareengineering.airprimaapp.visualization;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.softwareengineering.airprimaapp.R;

/**
 * This dialog allows the user to pick a time period (year, month, week or day).
 */
public class TimePeriodPickerDialog extends Dialog {

    private TimePeriodPickerListener listener;

    /**
     * Constructor
     */
    public TimePeriodPickerDialog(Activity activity) {
        super(activity);
    }

    /**
     * Setter for the listener
     */
    public void setListener(TimePeriodPickerListener listener) {
        this.listener = listener;
    }

    /**
     * Setup for the dialog
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_time_period_picker);

        TextView viewDay = findViewById(R.id.dialog_time_period_day);
        TextView viewWeek = findViewById(R.id.dialog_time_period_week);
        TextView viewMonth = findViewById(R.id.dialog_time_period_month);
        TextView viewYear = findViewById(R.id.dialog_time_period_year);

        Button cancel = findViewById(R.id.dialog_time_period_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDialog();
            }
        });

        viewDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.dialogValueReturn(Date.DAY);
                closeDialog();
            }
        });

        viewWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.dialogValueReturn(Date.WEEK);
                closeDialog();
            }
        });

        viewMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.dialogValueReturn(Date.MONTH);
                closeDialog();
            }
        });

        viewYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.dialogValueReturn(Date.YEAR);
                closeDialog();
            }
        });
    }

    /**
     * Clean up on dialog close.
     */
    private void closeDialog() {
        dismiss();
    }
}