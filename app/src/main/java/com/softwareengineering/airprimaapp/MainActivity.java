package com.softwareengineering.airprimaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

/**
 * First activity of the app. User can choose if he wants to continue with
 * stationary measurement or if he wants to continue with mobile measurement.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Setup for the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find view instances
        LinearLayout mobileLayout = findViewById(R.id.main_mobile);
        LinearLayout stationaryLayout = findViewById(R.id.main_stationary);

        // Register event listeners

        // Mobile measurement
        mobileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentConnect = new Intent();
                intentConnect.setClass(v.getContext(), ConnectActivity.class);
                intentConnect.putExtra("id", ConnectActivity.MOBILE_MEASUREMENT_ID);
                startActivity(intentConnect);
            }
        });

        // Stationary measurement
        stationaryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent stationaryIntent = new Intent(v.getContext(), StationaryActivity.class);
                startActivity(stationaryIntent);
            }
        });
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