package com.softwareengineering.airprimaapp;

/**
 * Interface that allows the returning of values fom the time period picker dialog to the fragment.
 */
public interface TimePeriodPickerListener {
    void dialogValueReturn(Date dateEnum);
}