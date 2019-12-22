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


public class ArrayAdapterBluetoothDevices extends ArrayAdapter<String> {

    private Context mContext;
    private int mResource;

    public ArrayAdapterBluetoothDevices(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvName = (TextView) convertView.findViewById(R.id.textBT);

        convertView.setOnClickListener((View.OnClickListener) mContext);

        tvName.setText(getItem(position));

        return convertView;
    }
}
