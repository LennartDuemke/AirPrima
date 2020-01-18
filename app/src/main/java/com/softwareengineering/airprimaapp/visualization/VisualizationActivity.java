package com.softwareengineering.airprimaapp.visualization;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.softwareengineering.airprimaapp.menues.InformationActivity;
import com.softwareengineering.airprimaapp.menues.MainActivity;
import com.softwareengineering.airprimaapp.R;
import com.softwareengineering.airprimaapp.bluetooth.ConnectActivity;
import com.softwareengineering.airprimaapp.other.DatabaseHandler;
import com.softwareengineering.airprimaapp.visualization.pageradapter.ViewPagerAdapterFinedust;
import com.softwareengineering.airprimaapp.visualization.pageradapter.ViewPagerAdapterFinedustMobile;
import com.softwareengineering.airprimaapp.visualization.pageradapter.ViewPagerAdapterHumidity;
import com.softwareengineering.airprimaapp.visualization.pageradapter.ViewPagerAdapterHumidityMobile;
import com.softwareengineering.airprimaapp.visualization.pageradapter.ViewPagerAdapterOverview;
import com.softwareengineering.airprimaapp.visualization.pageradapter.ViewPagerAdapterTemperature;
import com.softwareengineering.airprimaapp.visualization.pageradapter.ViewPagerAdapterTemperatureMobile;

/**
 * Activity that visualizes the sensor station readings.
 */
public class VisualizationActivity extends AppCompatActivity {

    private ViewPagerAdapterOverview viewPagerAdapterOverview;
    private ViewPagerAdapterFinedust viewPagerAdapterFinedust;
    private ViewPagerAdapterTemperature viewPagerAdapterTemperature;
    private ViewPagerAdapterHumidity viewPagerAdapterHumidity;

    private ViewPagerAdapterFinedustMobile viewPagerAdapterFinedustMobile;
    private ViewPagerAdapterTemperatureMobile viewPagerAdapterTemperatureMobile;
    private ViewPagerAdapterHumidityMobile viewPagerAdapterHumidityMobile;

    private ViewPager viewPager;

    private ImageButton button_overview;
    private ImageButton button_finedust;
    private ImageButton button_temperature;
    private ImageButton button_humidity;

    private DatabaseHandler dbHandler;
    private long locationID;
    private boolean isMobile;
    private boolean isConnected;

    /**
     * Setup for the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualization);

        dbHandler = new DatabaseHandler(this);

        // Test data for SQLite database
        //dbHandler.dropMeasurementAndCreateNew();
        //insertTestData();

        Bundle b = getIntent().getExtras();
        if(b != null) {
            locationID = b.getLong("id");
            isMobile = b.getBoolean("mobile");
            isConnected = b.getBoolean("connected");
        } else {
            finish();
        }

        // Find view instances (ButtonBar)
        button_overview = findViewById(R.id.button_overview);
        button_finedust = findViewById(R.id.button_finedust);
        button_temperature = findViewById(R.id.button_temperature);
        button_humidity = findViewById(R.id.button_humidity);

        // Register event listeners
        button_overview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeButtonColor(0);
                // Update ViewPager Adapter
                viewPager.removeAllViews();
                viewPager.setAdapter(null);
                viewPagerAdapterOverview = new ViewPagerAdapterOverview(getSupportFragmentManager(), locationID, isConnected);
                viewPager.setAdapter(viewPagerAdapterOverview);
            }
        });

        button_finedust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeButtonColor(1);
                // Update ViewPager Adapter
                viewPager.removeAllViews();
                viewPager.setAdapter(null);
                if(isMobile) {      // Mobile measurement:
                    viewPagerAdapterFinedustMobile = new ViewPagerAdapterFinedustMobile(getSupportFragmentManager(), locationID);
                    viewPager.setAdapter(viewPagerAdapterFinedustMobile);
                } else {            // Stationary measurement:
                    viewPagerAdapterFinedust = new ViewPagerAdapterFinedust(getSupportFragmentManager(), locationID, isConnected);
                    viewPager.setAdapter(viewPagerAdapterFinedust);
                }
            }
        });

        button_temperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeButtonColor(2);
                // Update ViewPager Adapter
                viewPager.removeAllViews();
                viewPager.setAdapter(null);
                if(isMobile) {      // Mobile measurement:
                    viewPagerAdapterTemperatureMobile = new ViewPagerAdapterTemperatureMobile(getSupportFragmentManager(), locationID);
                    viewPager.setAdapter(viewPagerAdapterTemperatureMobile);
                } else {            // Stationary measurement:
                    viewPagerAdapterTemperature = new ViewPagerAdapterTemperature(getSupportFragmentManager(), locationID, isConnected);
                    viewPager.setAdapter(viewPagerAdapterTemperature);
                }
            }
        });

        button_humidity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeButtonColor(3);
                // Update ViewPager Adapter
                viewPager.removeAllViews();
                viewPager.setAdapter(null);
                if(isMobile) {      // Mobile measurement:
                    viewPagerAdapterHumidityMobile = new ViewPagerAdapterHumidityMobile(getSupportFragmentManager(), locationID);
                    viewPager.setAdapter(viewPagerAdapterHumidityMobile);
                } else {            // Stationary measurement:
                    viewPagerAdapterHumidity = new ViewPagerAdapterHumidity(getSupportFragmentManager(), locationID, isConnected);
                    viewPager.setAdapter(viewPagerAdapterHumidity);
                }
            }
        });

        // Viewpager adapter
        viewPagerAdapterOverview = new ViewPagerAdapterOverview(getSupportFragmentManager(), locationID, isConnected);
        viewPager = findViewById(R.id.pagerReadings);
        viewPager.setAdapter(viewPagerAdapterOverview);
    }

    /**
     * Closes database handler on activity destruction
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHandler.close();
        ConnectActivity.THREAD_RUN = false;
    }

    /**
     * Change color of selected button (ButtonBar)
     */
    private void changeButtonColor(int index) {
        button_overview.setBackgroundColor(ContextCompat.getColor(this, index == 0 ? R.color.colorButtonBarActive : R.color.colorButtonBarInactive));
        button_finedust.setBackgroundColor(ContextCompat.getColor(this, index == 1 ? R.color.colorButtonBarActive : R.color.colorButtonBarInactive));
        button_temperature.setBackgroundColor(ContextCompat.getColor(this, index == 2 ? R.color.colorButtonBarActive : R.color.colorButtonBarInactive));
        button_humidity.setBackgroundColor(ContextCompat.getColor(this, index == 3 ? R.color.colorButtonBarActive : R.color.colorButtonBarInactive));
    }

    /**
     * Inserts test measurements into the SQLite database
     * SQLite time string of datetime() --> YYYY-MM-DD HH:MM:SS
     */
    private void insertTestData() {

        String year = "2004";   // Test data was measured in this year
        int locationID = 1;     // A location with that id has to exist!

        int lastIndexTestData = 11;
        float[] pm10 = {18, 19, 20, 21, 22, 23, 24, 25, 30, 31, 32, 40};    // Test data that gets picked randomly
        float[] pm2_5 = {3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 20, 21};
        float[] temp = {-1, -5, 0, 4, 10, 20, 23, 30, -10, 15, 18, 27};
        float[] humid = {40, 45, 50, 55, 65, 70, 75, 80, 85, 90, 95, 100};

        int month = 1;

        // 12 months
        for(int m = 0; m < 12; m++) {

            int day = 1;

            // 31 days
            for(int d = 0; d < 31; d++) {

                int hour = 0;

                // 24 hours
                for(int h = 0; h < 24; h++) {

                    int minute = 9;

                    // 6 measurements per hour every 10 minutes
                    for(int i = 0; i < 6; i++) {

                        // Random measurements
                        float tmpPm2_5 = pm2_5[getRandomNumberInRange(0, lastIndexTestData)];
                        float tmpPm10 = pm10[getRandomNumberInRange(0, lastIndexTestData)];
                        float tmpTemp = temp[getRandomNumberInRange(0, lastIndexTestData)];
                        float tmpHumid = humid[getRandomNumberInRange(0, lastIndexTestData)];

                        // Built time string
                        String timestring = year + "-";
                        if(month < 10) { timestring += "0"; }
                        timestring += String.valueOf(month);
                        timestring += "-";
                        if(day < 10) { timestring += "0"; }
                        timestring += String.valueOf(day);
                        timestring += " ";
                        if(hour < 10) { timestring += "0"; }
                        timestring += String.valueOf(hour);
                        timestring += ":";
                        if(minute < 10) { timestring += "0"; }
                        timestring += String.valueOf(minute);
                        timestring += ":00";

                        dbHandler.insertMeasurement(1010101, timestring, tmpPm2_5, tmpPm10, tmpTemp, tmpHumid, locationID);

                        minute += 10;
                    }
                    hour++;
                }
                day++;
            }
            month++;
        }

        // Test data just to have a different year
        dbHandler.insertMeasurement(1010101, "2019-04-12 11:20:04", 22, 22, 22, 22, locationID);
        dbHandler.insertMeasurement(1010101, "2019-04-13 11:20:04", 22, 22, 22, 22, locationID);
    }

    /**
     * Returns a random number between a min and max value
     */
    private int getRandomNumberInRange(int min, int max) {
        return (int)(Math.random() * ((max - min) + 1)) + min;
    }

    /**
     * Create the options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handling click events of menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.item_home:    // Home button
                Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                return true;
            case R.id.item_info:    // Information button
                Intent informationIntent = new Intent(this, InformationActivity.class);
                startActivity(informationIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}