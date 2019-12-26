package com.softwareengineering.airprimaapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * Adapter for the bluetooth devices activity
 */
public class ArrayAdapterBluetoothDevices extends ArrayAdapter<String> {

    private Context mContext;
    private int mResource;

    /**
     * Constructor
     */
    ArrayAdapterBluetoothDevices(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    /**
     * Returns the view of a list item
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvName = convertView.findViewById(R.id.item_connect_text);
        tvName.setText(getItem(position));

        convertView.setOnClickListener((View.OnClickListener) mContext);

        return convertView;
    }
}
