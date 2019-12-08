package com.softwareengineering.airprimaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity that lets the user select, edit or create a location.
 */
public class LocationsActivity extends AppCompatActivity {

    private DatabaseHandler dbHandler;
    private CursorAdapter ca;

    /**
     * Setup for the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);

        // New location button
        Button button = findViewById(R.id.locations_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentNew = new Intent();
                intentNew.setClass(v.getContext(), EditLocationActivity.class);
                intentNew.putExtra("new", true);
                startActivity(intentNew);
            }
        });

        // Database handler
        dbHandler = new DatabaseHandler(this);
        //dbHandler.dropAllAndCreateNew();

        // Setup ListView
        ca = new LocationsCursorAdapter(this);
        ListView listView = findViewById(R.id.locations_listview);
        listView.setAdapter(ca);
        updateList();

        // Listener called when user clicks on item in ListView.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(view.getContext(), "Pressed item Id = " + id, Toast.LENGTH_SHORT).show();
                // TODO: Open activity that visualizes the sensor readings
            }
        });
    }

    /**
     * Update the ListView
     */
    private void updateList() {
        ca.changeCursor(dbHandler.queryLocations());
    }

    /**
     * Closes database handler on activity destruction
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHandler.close();
    }

    /**
     * Update ListView on restart
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        updateList();
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
