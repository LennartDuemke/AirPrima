package com.softwareengineering.airprimaapp.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.nio.channels.IllegalBlockingModeException;
import java.util.UUID;

/**
 * This class gets the bluetooth socket instance for the bluetooth client
 */
class ClientSocketThread extends SocketThread {

    private static final String TAG = ClientSocketThread.class.getSimpleName();
    private BluetoothSocket socket;

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

        } catch (IOException e) {
            Log.e(TAG, "Error occurred during the BT connection.", e);
            cancel();
        } catch (IllegalBlockingModeException e) {
            Log.e(TAG, "Socket has an associated channel and the channel is in non-blocking mode.", e);
            cancel();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Endpoint is null or a SocketAddress subclass.", e);
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
