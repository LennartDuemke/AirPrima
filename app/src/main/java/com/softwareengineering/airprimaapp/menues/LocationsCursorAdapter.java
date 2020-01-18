package com.softwareengineering.airprimaapp.menues;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.softwareengineering.airprimaapp.R;

/**
 * Populates the ListView in LocationsActivity via CursorAdapter. Data sourced from database.
 */
public class LocationsCursorAdapter extends CursorAdapter {

    private LayoutInflater inflator;

    /**
     * Constructor
     */
    public LocationsCursorAdapter(Context context) {
        super(context, null, 0);
        inflator = LayoutInflater.from(context);
    }

    /**
     * Inflates a new view and returns it
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflator.inflate(R.layout.item_location, null);
    }

    /**
     * Binds data to a given view
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // Get the name of the location
        String locationName = cursor.getString(cursor.getColumnIndex("location_name"));
        TextView itemText = view.findViewById(R.id.item_text);
        itemText.setText(locationName);

        // Get the id of the location
        final long locationID = cursor.getLong(cursor.getColumnIndex("_id"));

        // Edit button listener
        ImageView edit = view.findViewById(R.id.item_icon_edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create new Edit-Location activity
                Intent intentEdit = new Intent();
                intentEdit.setClass(context, EditLocationActivity.class);
                intentEdit.putExtra("id", locationID);
                intentEdit.putExtra("new", false);
                context.startActivity(intentEdit);
            }
        });
    }
}