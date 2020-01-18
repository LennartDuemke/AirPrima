package com.softwareengineering.airprimaapp;

import android.bluetooth.BluetoothSocket;

/**
 * Abstract class for the ClientSocketThread
 */
public abstract class SocketThread extends Thread {

    public abstract BluetoothSocket getSocket();

    public abstract void cancel();
}
