package com.softwareengineering.airprimaapp.menues;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.softwareengineering.airprimaapp.R;
import com.softwareengineering.airprimaapp.other.DatabaseHandler;

/**
 * Activity that lets the user rename and delete a location.
 */
public class EditLocationActivity extends AppCompatActivity {

    private static final String TAG = EditLocationActivity.class.getSimpleName();   // Tag for the logger
    private final int minFreq = 1;
    private final int maxFreq = 60;
    private DatabaseHandler dbHandler;
    private Bundle bundle;

    private TextView title;
    private EditText edit1;
    private EditText edit2;
    private FrameLayout save;
    private FrameLayout cancel;
    private FrameLayout delete;

    /**
     * Setup for the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        // Database handler
        dbHandler = new DatabaseHandler(this);

        // Get view instances
        title = findViewById(R.id.location_edit_title);
        edit1 = findViewById(R.id.location_edit_questname);
        edit2 = findViewById(R.id.location_edit_freq);
        save = findViewById(R.id.location_edit_save);
        cancel = findViewById(R.id.location_edit_cancel);
        delete = findViewById(R.id.location_edit_delete);

        // Check if activity has to make a new location or edit an existing location
        boolean isNew = true;

        bundle = getIntent().getExtras();
        if(bundle != null) {
            isNew = bundle.getBoolean("new");    // Get parameter "new"
        }

        if(isNew) {
            // Make new location
            newLocation();
        } else {
            // Edit existing location
            editLocation();
        }
    }

    /**
     * Make new location
     */
    private void newLocation() {

        // Update title
        title.setText(R.string.location_edit_title);

        // Hide delete button
        delete.setVisibility(View.GONE);

        // Register listeners
        // Save
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Checks the EditText views
                if(!checkInput()) {
                    return;
                }

                // Update DB
                String name = edit1.getText().toString();
                int freq = Integer.parseInt(edit2.getText().toString());
                dbHandler.insertLocation(name, freq);

                // Close activity
                finish();
            }
        });

        // Cancel
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close activity
                finish();
            }
        });

    }

    /**
     * Edit existing location
     */
    private void editLocation() {

        // Get name and transmission frequency of specific location
        final long id = bundle.getLong("id");    // Get parameter "id"

        // Debug Toast
        //Toast.makeText(this, "Passed Id = " + id, Toast.LENGTH_SHORT).show();

        Cursor cursor = dbHandler.querySpecificLocation(id);
        if(cursor != null) {

            // Get data from cursor
            cursor.moveToFirst();
            String locationName = cursor.getString(cursor.getColumnIndex("location_name"));
            int locationMeasureFreq = cursor.getInt(cursor.getColumnIndex("location_measuring_freq"));
            cursor.close();

            // Update Views
            title.setText(locationName);
            edit1.setText(locationName);
            edit2.setText(String.valueOf(locationMeasureFreq));

            // Register listeners
            // Save
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Checks the EditText views
                    if(!checkInput()) {
                        return;
                    }

                    // Update DB
                    String name = edit1.getText().toString();
                    int freq = Integer.parseInt(edit2.getText().toString());
                    dbHandler.updateLocation(id, name, freq);

                    // Close activity
                    finish();
                }
            });

            // Cancel
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close activity
                    finish();
                }
            });

            // Delete
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder adb = new AlertDialog.Builder(v.getContext());
                    adb.setTitle(R.string.location_edit_delete_title);
                    adb.setMessage(R.string.location_edit_delete_message);
                    adb.setNegativeButton(R.string.no, null);
                    adb.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            dbHandler.deleteLocationMeasurements(id);   // Delete all measurements of the location
                            dbHandler.deleteLocation(id);               // Delete location itself
                            // Close activity
                            finish();
                        }
                    });
                    adb.show();
                }
            });

        } else {
            Log.e(TAG, "EditLocationActivity: Cursor is null!");
        }
    }

    /**
     * Checks if the EditText views are empty or if the measurementFreq is outside its range
     */
    private boolean checkInput() {

        boolean noError = true;

        // Check if empty
        if(TextUtils.isEmpty(edit1.getText())) {
            edit1.setError(getString(R.string.location_edit_empty_name));
            noError = false;
        }
        if(TextUtils.isEmpty(edit2.getText())) {
            edit2.setError(getString(R.string.location_edit_empty_freq));
            noError = false;

        } else {

            // Check if transmission frequency is between 2 and 60
            int mfreq = Integer.parseInt(edit2.getText().toString());
            if(mfreq < minFreq || mfreq > maxFreq) {
                edit2.setError(getString(R.string.location_edit_freq_range));
                noError = false;
            }
        }

        return noError;
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