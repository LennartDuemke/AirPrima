package com.softwareengineering.airprimaapp;

import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConnectActivityTest {

    @Rule
    public ActivityTestRule<ConnectActivity> activityRule = new ActivityTestRule(ConnectActivity.class);

    private ConnectActivity connectActivity = null;

    @Before
    public void setUp() {
        connectActivity = activityRule.getActivity();
    }

    @Test
    public void unixTimestampToSQLiteTimestring() {
        assertEquals("2020-01-14 21:08:12", connectActivity.unixTimestampToSQLiteTimestring("1579036092"));
    }
}