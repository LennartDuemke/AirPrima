package com.softwareengineering.airprimaapp.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Interface for the communication between the ConnectActivity and its ListAdapter
 */
public interface ConnectInterface {
    void stopDiscovery();
    void startConnecting(BluetoothDevice btDevice);
}
