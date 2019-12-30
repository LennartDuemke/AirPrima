package com.softwareengineering.airprimaapp;

import android.bluetooth.BluetoothDevice;

public interface ConnectInterface {
    void stopDiscovery();
    void startConnecting(BluetoothDevice btDevice);
}
