package com.softwareengineering.airprimaapp;

/**
 * Interface that allows the returning of values fom the date picker dialog to the fragment.
 */
public interface DatepickerListener {
    void dialogValueReturn(String year, String week, String month, String day);
}