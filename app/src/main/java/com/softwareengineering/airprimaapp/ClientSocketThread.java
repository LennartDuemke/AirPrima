package com.softwareengineering.airprimaapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Get bluetooth socket instance for client
 */
public class ClientSocketThread extends Thread {

    private static final String TAG = ClientSocketThread.class.getSimpleName();
    static long currentLocation = -1;

    private BluetoothSocket socket;

    /**
     * Connect to bluetooth device
     */
    ClientSocketThread(BluetoothDevice device, UUID uuid) {
        socket = null;
        setName(TAG);
        try {
            socket = device.createRfcommSocketToServiceRecord(uuid);
            Log.d(TAG, "createRfcommSocketToServiceRecord() created");
        } catch (IOException e) {
            Log.d(TAG, "createRfcommSocketToServiceRecord() failed", e);
        }
    }

    /**
     * Thread logic
     */
    @Override
    public void run() {
        try {
            socket.connect();
        } catch (IOException connectException) {
            cancel();
        }
    }

    /**
     * Get bluetooth socket
     */
    BluetoothSocket getSocket() {
        return socket;
    }

    /**
     * Cancel bluetooth connection
     */
    void cancel() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close client socket", e);
            } finally {
                socket = null;
            }
        }
    }
}
