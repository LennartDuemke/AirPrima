package com.softwareengineering.airprimaapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Get bluetooth socket instance for bluetooth client
 */
class ClientSocketThread extends SocketThread {

    private static final String TAG = ClientSocketThread.class.getSimpleName();
    private BluetoothSocket socket;
    static long currentLocation = -1;   // TODO Relocate

    /**
     * Constructor. Creates bluetooth socket
     */
    ClientSocketThread(BluetoothDevice device, UUID uuid) {
        socket = null;
        setName(TAG);
        try {
            socket = device.createRfcommSocketToServiceRecord(uuid);
            Log.d(TAG, "createRfcommSocketToServiceRecord() worked.");
        } catch (IOException e) {
            Log.e(TAG, "createRfcommSocketToServiceRecord() failed.", e);
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
    @Override
    public BluetoothSocket getSocket() {
        return socket;
    }

    /**
     * Cancel bluetooth connection
     */
    @Override
    public void cancel() {
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
