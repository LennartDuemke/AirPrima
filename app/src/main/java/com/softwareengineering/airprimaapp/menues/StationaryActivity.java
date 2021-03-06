package com.softwareengineering.airprimaapp.menues;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.softwareengineering.airprimaapp.R;

/**
 * Activity that lets the user choose between connecting to a sensor station via bluetooth
 * or viewing the readings of a location.
 */
public class StationaryActivity extends AppCompatActivity {

    /**
     * Setup for the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stationary);

        // Find view instances
        LinearLayout connectLayout = findViewById(R.id.stationary_connect);
        LinearLayout oldDataLayout = findViewById(R.id.stationary_old_data);

        // Register event listeners

        // Connect to sensor station
        connectLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locationsIntent = new Intent(v.getContext(), LocationsActivity.class);
                locationsIntent.putExtra("mode", "CONNECTED");
                startActivity(locationsIntent);
            }
        });

        // View old measurements (without connecting)
        oldDataLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locationsIntent = new Intent(v.getContext(), LocationsActivity.class);
                locationsIntent.putExtra("mode", "DISCONNECTED");
                startActivity(locationsIntent);
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
