package com.softwareengineering.airprimaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.Date;

/**
 * Activity that lets the user connect to a sensor station via bluetooth or pair with it
 */
public class ConnectActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ConnectActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 123;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 321;
    private static final UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
    static final int MOBILE_MEASUREMENT_ID = 4000;

    private ProgressDialog progressDialog;
    private BluetoothAdapter adapter;
    private ArrayList<String> deviceList;
    private ListView listView;

    private Context context;


    /**
     * Setup for the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        // Find view instances
        Button pairButton = findViewById(R.id.connect_button);

        // Register event listeners
        pairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(viewIntent);
            }
        });

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
        }

        context = this;
        progressDialog = new ProgressDialog(this);

        deviceList = new ArrayList<>();

        listView = findViewById(R.id.connect_listview);
        ArrayAdapterBluetoothDevices adapter = new ArrayAdapterBluetoothDevices(this, R.layout.item_connect, deviceList);
        listView.setAdapter(adapter);

        showDevices();
    }


    /**
     * Result of bluetooth permission request
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == RESULT_OK) && (requestCode == REQUEST_ENABLE_BT)) {
            showDevices();
        }
        else {
            Toast.makeText(this, R.string.bluetooth_not_active, Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Show paired bluetooth devices
     */
    private void showDevices() {
        boolean enabled;
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            enabled = adapter.isEnabled();
            if (!enabled) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else {
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                for (BluetoothDevice device : devices) {
                    deviceList.add(device.getName());
                }
                listView.invalidateViews();
            }
        }
    }


    /**
     * Bluetooth connection with AirPrima sensor station
     */
    private Thread createAndStartThread(final ClientSocketThread t) {
        Thread workerThread = new Thread() {
            boolean keepRunning = true;

            @Override
            public void run() {
                try {
                    t.start();
                    Log.d(TAG, "joining " + t.getName());
                    t.join();
                    BluetoothSocket socket = t.getSocket();

                    Intent intent = getIntent();
                    String mode = intent.getStringExtra("mode");
                    Log.d(TAG, String.format("MODE -------------------> %s", mode));

                    if (socket != null) {

                        // create OutputStream
                        Log.d(TAG, String.format("connection type %d for %s", socket.getConnectionType(), t.getName()));
                        OutputStream _os = null;
                        try {
                            _os = socket.getOutputStream();
                        } catch (IOException e) {
                            Log.e(TAG, null, e);
                        }
                        final OutputStream os = _os;
                        InputStream is = socket.getInputStream();

                        // Ask device if it is an AirPrima sensor station
                        send(os, "Hallo. Bist du eine AirPrima Station ?");

                        // The sensor station has 3 seconds to answer
                        sleep(3000);

                        stopLoading();

                        DatabaseHandler dbHandler;
                        dbHandler = new DatabaseHandler(context);

                        // Analyse message
                        String txt = receive(is);
                        if (txt != null) {

                            // It is an AirPrima sensor station
                            if (txt.equals("Ja, bin ich.")) {
                                Log.d(TAG, "Confirmation received.");


                                int locationTransFreq = 3;

                                if (mode.equals("mobile")) {
                                    dbHandler.deleteAllMobileMeasurements();

                                    ClientSocketThread.currentLocation = MOBILE_MEASUREMENT_ID;

                                    Intent intentVisualize = new Intent();
                                    intentVisualize.setClass(context, VisualizationActivity.class);
                                    intentVisualize.putExtra("id", MOBILE_MEASUREMENT_ID);
                                    startActivity(intentVisualize);
                                }
                                else if (mode.equals("stationary")) {
                                    Intent viewIntent = new Intent(context, LocationsActivity.class);
                                    startActivity(viewIntent);

                                    // Wait till user has chosen a location
                                    while (ClientSocketThread.currentLocation == -1) {
                                        sleep(1000);
                                        Log.d(TAG, "Waiting for user to choose location.");
                                    }

                                    Cursor cursor = dbHandler.querySpecificLocation(ClientSocketThread.currentLocation);
                                    cursor.moveToFirst();
                                    locationTransFreq = cursor.getInt(cursor.getColumnIndex("location_transmission_freq"));
                                    cursor.close();
                                }



                                send(os, "INIT;" + ClientSocketThread.currentLocation + ";" + locationTransFreq);

                                sleep(3000);

                                txt = receive(is);

                                if (txt.equals("Initialisierung erfolgreich.")) {
                                    Log.d(TAG, "Initialization successful.");
                                }
                                else {
                                    keepRunning = false;
                                    Log.e(TAG, "Initialization failed.");
                                }
                            }
                        }
                        while (keepRunning) {
                            // Receive data
                            txt = receive(is);

                            if (txt != null) {
                                Log.d(TAG, "Message: " + txt);

                                if (txt.equals("Deine Datenbank ist aktuell."))  {
                                    sleep(10000);
                                }
                                else if (txt.matches(".*MEASUREMENT;.*")) {
                                    String[] measurement = txt.split(";");
                                    dbHandler.insertMeasurement(
                                            Long.parseLong(measurement[1]),
                                            unixTimestampToSQLiteTimestring(measurement[1]),
                                            Float.parseFloat(measurement[2]),
                                            Float.parseFloat(measurement[3]),
                                            Float.parseFloat(measurement[4]),
                                            Float.parseFloat(measurement[5]),
                                            Long.parseLong(measurement[6])
                                    );
                                }
                            }


                            Cursor cursor = dbHandler.queryNewestTimestamp(ClientSocketThread.currentLocation);
                            if(cursor != null) {
                                // Get data from cursor
                                cursor.moveToFirst();
                                long timestamp = cursor.getInt(cursor.getColumnIndex("measurement_timestamp"));
                                cursor.close();
                                send(os, "TIMESTAMP;" + timestamp);
                            }

                            sleep(1000);
                        }
                    }
                    else {
                        Log.e(TAG, "Bluetooth socket is empty.");
                    }
                } catch (InterruptedException | IOException e) {
                    Log.e(TAG, null, e);
                    keepRunning = false;
                } finally {
                    Log.d(TAG, "Calling cancel() of " + t.getName());
                    t.cancel();
                }
            }
        };
        workerThread.start();
        return workerThread;
    }

    /**
     * Converts an Unix timestamp to a SQlite timestring
     */
    private String unixTimestampToSQLiteTimestring(String timestamp) {
        long tmpTimestamp = Long.parseLong(timestamp);
        Date date = new Date((long)tmpTimestamp*1000); // Needs milliseconds and not seconds
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }


    /**
     * Send string to AirPrima station
     */
    private void send(OutputStream os, String text) {
        try {
            os.write(text.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Error while sending", e);
        }
    }


    /**
     * Receive string from AirPrima station
     */
    private String receive(InputStream in) {
        try {
            int num = in.available();
            if (num > 0) {
                byte[] buffer = new byte[num];
                int read = in.read(buffer);
                if (read != -1) {
                    return new String(buffer, 0, read);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Receive()", e);
        }
        return null;
    }


    /**
     * Handling onclick events
     */
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.layoutBT) {
            TextView tv = v.findViewById(R.id.item_connect_text);
            String station = tv.getText().toString();


            progressDialog.setMessage(getString(R.string.connecting));
            progressDialog.show();

            BluetoothDevice remoteDevice = null;
            Set<BluetoothDevice> devices = adapter.getBondedDevices();
            for (BluetoothDevice device : devices) {
                if (station.equals(device.getName())) {
                    remoteDevice = device;
                    Log.d(TAG, "Device: " + remoteDevice.getName());

                }
            }
            if (remoteDevice != null) {
                ClientSocketThread clientSocketThread = new ClientSocketThread(remoteDevice, MY_UUID);
                Thread clientThread = createAndStartThread(clientSocketThread);
            }
        }
    }

    /**
     * Remove the progress dialog
     */
    void stopLoading() {
        progressDialog.dismiss();
    }

    /**
     * Create the options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handling click events of menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.item_home:    // Home button
                Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                return true;
            case R.id.item_info:    // Information button
                Intent informationIntent = new Intent(this, InformationActivity.class);
                startActivity(informationIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}