package com.softwareengineering.airprimaapp;

import androidx.fragment.app.FragmentActivity;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class FragmentDevelopmentTest {

    private FragmentDevelopment fragmentDevelopment;

    @Before
    public void setUp() {
        fragmentDevelopment = new FragmentDevelopment();
    }

    @Test
    public void getYearFromTimestring() {
        assertEquals("2020", fragmentDevelopment.getYearFromTimestring("2020-01-14 21:08:12"));
    }

    @Test
    public void getMonthFromTimestring() {
        assertEquals("01", fragmentDevelopment.getMonthFromTimestring("2020-01-14 21:08:12"));
    }

    @Test
    public void getDayFromTimestring() {
        assertEquals("14", fragmentDevelopment.getDayFromTimestring("2020-01-14 21:08:12"));
    }
}