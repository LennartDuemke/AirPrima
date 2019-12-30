package com.softwareengineering.airprimaapp;

import android.bluetooth.BluetoothSocket;

public abstract class SocketThread extends Thread {

    public abstract BluetoothSocket getSocket();

    public abstract void cancel();
}
