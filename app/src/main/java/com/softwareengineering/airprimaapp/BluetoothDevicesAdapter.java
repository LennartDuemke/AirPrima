package com.softwareengineering.airprimaapp;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * ListAdapter for the activity "ConnectActivty".
 */
public class BluetoothDevicesAdapter extends BaseAdapter {

    private static final String TAG = BluetoothDevicesAdapter.class.getSimpleName();

    private ArrayList<BluetoothDevice> devices;
    private boolean isPaired;
    private LayoutInflater mInflater;
    private Context mContext;
    private ConnectInterface connectInterface;

    /**
     * BroadcastReceiver that listens if pairing was successful
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device != null) {
                    switch (device.getBondState()) {
                        case BluetoothDevice.BOND_BONDED:
                            Log.d(TAG, "Bluetooth pairing finished!");
                            Toast.makeText(context, R.string.bt_connect_pairing_success, Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothDevice.BOND_NONE:
                            Log.d(TAG, "Bluetooth pairing canceled!");
                            Toast.makeText(context, R.string.bt_connect_pairing_cancel, Toast.LENGTH_SHORT).show();
                        default:
                            break;
                    }
                }
            }
        }
    };

    /**
     * Constructor
     */
    BluetoothDevicesAdapter(Context context, ConnectInterface connectInterface, ArrayList<BluetoothDevice> devices, boolean isPaired) {
        this.devices = devices;
        this.isPaired = isPaired;
        this.connectInterface = connectInterface;
        this.mContext = context;
        mInflater = LayoutInflater.from(context);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mContext.registerReceiver(receiver, filter);
    }

    /**
     * Updates the list
     */
    void update(ArrayList<BluetoothDevice> list) {
        this.devices = list;
        //Triggers the list update
        notifyDataSetChanged();
    }

    /**
     * Returns the item count
     */
    public int getCount() {
        return devices.size();
    }

    /**
     * Returns the current item
     */
    public Object getItem(int position) {
        return devices.get(position);
    }

    /**
     * Returns the item id
     */
    public long getItemId(int position) {
        return position;
    }

    /**
     * Returns the view of the list
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.item_bt_devices, null);
            holder = new ViewHolder();

            holder.deviceNameView = convertView.findViewById(R.id.item_device_name);
            holder.deviceAddressView = convertView.findViewById(R.id.item_device_address);
            holder.buttonView = convertView.findViewById(R.id.item_bluetooth_button);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Insert data into view
        final BluetoothDevice tmpDevice = devices.get(position);
        if(tmpDevice != null) {
            holder.deviceNameView.setText(tmpDevice.getName());
            holder.deviceAddressView.setText(mContext.getString(R.string.bt_connect_device_address, tmpDevice.getAddress()));

            if(isPaired) {

                // Set button text
                holder.buttonView.setText(R.string.connecting);

                // Register OnClickListener for paired bluetooth devices
                holder.buttonView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        connectInterface.stopDiscovery();
                        Log.d(TAG, "Start connecting to device " + tmpDevice.getAddress());
                        connectInterface.startConnecting(tmpDevice);    // Connecting the bluetooth devices
                    }
                });

            } else {

                // Set button text
                holder.buttonView.setText(R.string.pair);

                // Register OnClickListener for available bluetooth devices
                holder.buttonView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        connectInterface.stopDiscovery();
                        Log.d(TAG, "Start pairing with device " + tmpDevice.getAddress());
                        tmpDevice.createBond(); // Pairing the bluetooth devices
                    }
                });
            }
        }
        return convertView;
    }

    /**
     * Class that holds the views of a list item
     */
    static class ViewHolder {
        TextView deviceNameView;
        TextView deviceAddressView;
        Button buttonView;
    }

    /**
     * Unregister the broadcast receiver when the adapter gets destroyed
     */
    void onDestroy() {
        mContext.unregisterReceiver(receiver);
    }
}