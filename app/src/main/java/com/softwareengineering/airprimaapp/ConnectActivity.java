package com.softwareengineering.airprimaapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Activity that lets the user connect to a sensor station via bluetooth or pair with it
 */
public class ConnectActivity extends AppCompatActivity implements ConnectInterface {

    private static final String TAG = ConnectActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 123;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 321;
    private static final UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
    static final int MOBILE_MEASUREMENT_ID = 1000;

    private TextView bluetoothStatusView;
    private ProgressDialog progressDialog;

    private ArrayList<BluetoothDevice> pairedDevicesList = new ArrayList<>();
    private ArrayList<BluetoothDevice> availableDevicesList = new ArrayList<>();

    private BluetoothDevicesAdapter adapterPairedDevices;
    private BluetoothDevicesAdapter adapterAvailableDevices;

    private BluetoothAdapter bluetoothAdapter;
    private boolean started;        // Did the discovery of bluetooth devices already start?
    private Thread clientThread;
    private Context context;

    /**
     * Listens for available bluetooth devices near the user
     */
    private final BroadcastReceiver receiverDeviceFound = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!availableDevicesList.contains(device)) {
                    availableDevicesList.add(device);
                    adapterAvailableDevices.update(availableDevicesList);
                }
            }
        }
    };

    /**
     * Listens if the bluetooth state has changed. Is bluetooth on or off?
     */
    private final BroadcastReceiver receiverBTChange = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        turnDiscoveryOffAndClearLists();
                        break;

                    case BluetoothAdapter.STATE_ON:
                        availableDevicesList.clear();
                        adapterAvailableDevices.update(availableDevicesList);
                        changeStatus(true);
                        break;
                }
            }
        }
    };

    /**
     * On activity construction - setup
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        context = this;

        bluetoothStatusView = findViewById(R.id.bt_connect_status);
        ListView pairedDevicesListView = findViewById(R.id.bt_connect_paired_list);
        ListView availableDevicesListView = findViewById(R.id.bt_connect_available_list);
        Button bluetoothScanButtonView = findViewById(R.id.bt_connect_scan);
        progressDialog = new ProgressDialog(this);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiverDeviceFound, filter);

        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiverBTChange, filter2);

        adapterPairedDevices = new BluetoothDevicesAdapter(this, this, pairedDevicesList, true);
        adapterAvailableDevices = new BluetoothDevicesAdapter(this, this, availableDevicesList, false);
        pairedDevicesListView.setAdapter(adapterPairedDevices);
        availableDevicesListView.setAdapter(adapterAvailableDevices);

        // Click listener when "scan" button gets pressed
        bluetoothScanButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnDiscoveryOffAndClearLists();
                if (isBluetoothEnabled()) {
                    changeStatus(true);
                } else {
                    turnDiscoveryOffAndClearLists();
                }
            }
        });
    }

    /**
     * On activity destruction
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister BroadcastReceiver
        adapterPairedDevices.onDestroy();
        adapterAvailableDevices.onDestroy();
        unregisterReceiver(receiverDeviceFound);
        unregisterReceiver(receiverBTChange);
    }

    /**
     * On activity start
     */
    @Override
    protected void onStart() {
        super.onStart();

        bluetoothAdapter = null;
        started = false;
        pairedDevicesList.clear();
        availableDevicesList.clear();

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_COARSE_LOCATION);
        } else {
            if (isBluetoothEnabled()) {
                changeStatus(true);
            } else {
                turnDiscoveryOffAndClearLists();
            }
        }
    }

    /**
     * On activity pause
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (started) {
            bluetoothAdapter.cancelDiscovery();
            started = false;
        }
        if (clientThread != null) {
            clientThread.interrupt();
            clientThread = null;
        }
    }

    /**
     * Users reaction on the request permission dialog
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if ((requestCode == REQUEST_ACCESS_COARSE_LOCATION) && (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            if (isBluetoothEnabled()) {
                changeStatus(true);
            } else {
                turnDiscoveryOffAndClearLists();
            }
        } else {
            finish();
            Toast.makeText(this, R.string.bt_connect_no_geolocation, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * User reaction on the activate bluetooth dialog
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((resultCode == RESULT_OK) && (requestCode == REQUEST_ENABLE_BT)) {
            changeStatus(true);
        } else {
            turnDiscoveryOffAndClearLists();
        }
    }

    /**
     * Checks if bluetooth is enabled + gets the Bluetooth adapter
     */
    private boolean isBluetoothEnabled() {
        boolean enabled = false;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            enabled = bluetoothAdapter.isEnabled();
            if (!enabled) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        return enabled;
    }

    /**
     * Change the upper visual bluetooth status in the activity. Shows that bluetooth is on or off
     */
    private void changeStatus(boolean enabled) {
        if (enabled) {
            bluetoothStatusView.setText(R.string.bt_connect_active);
            bluetoothStatusView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_bluetooth_blue_48dp, 0);
            insertDevices();
        } else {
            bluetoothStatusView.setText(R.string.bt_connect_inactive);
            bluetoothStatusView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_bluetooth_disabled_grey_48dp, 0);
        }
    }

    /**
     * Turns the discovery of bluetooth devices off and clears the lists
     */
    private void turnDiscoveryOffAndClearLists() {
        if (started) {
            bluetoothAdapter.cancelDiscovery();
            started = false;
        }
        changeStatus(false);
        pairedDevicesList.clear();
        availableDevicesList.clear();
        adapterPairedDevices.update(pairedDevicesList);
        adapterAvailableDevices.update(availableDevicesList);
    }

    /**
     * Inserts the (paired) bluetooth devices into the ListViews
     */
    private void insertDevices() {

        pairedDevicesList.clear();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        pairedDevicesList.addAll(pairedDevices);

        if (started) {
            bluetoothAdapter.cancelDiscovery();
        }
        started = bluetoothAdapter.startDiscovery();

        // Update adapterPairedDevices
        adapterPairedDevices.update(pairedDevicesList);
    }

    /**
     * Called inside the adapter: Turns the discovery off
     */
    @Override
    public void stopDiscovery() {
        if (started) {
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "Cancel BT discovery before connection");
        }
    }

    /**
     * Called inside the adapter: Initiates a bluetooth connection
     */
    @Override
    public void startConnecting(BluetoothDevice btDevice) {
        if (btDevice != null) {
            progressDialog.setMessage(getString(R.string.connecting));
            progressDialog.show();
            SocketThread clientSocketThread = new ClientSocketThread(btDevice, MY_UUID);
            clientThread = createAndStartThread(clientSocketThread);
        }
    }

    /**
     * Starts bluetooth connection with AirPrima sensor station
     */
    private Thread createAndStartThread(final SocketThread t) {
        Thread workerThread = new Thread() {
            boolean keepRunning = true;

            @Override
            public void run() {
                try {
                    t.start();
                    Log.d(TAG, "Joining " + t.getName());
                    t.join();
                    BluetoothSocket socket = t.getSocket();

                    Intent intent = getIntent();
                    String mode = intent.getStringExtra("mode");
                    Log.d(TAG, String.format("MODE -------------------> %s", mode));

                    if (socket != null) {

                        // Create OutputStream
                        OutputStream _os = null;
                        try {
                            _os = socket.getOutputStream();
                        } catch (IOException e) {
                            Log.e(TAG, "OutputStream null", e);
                        }
                        final OutputStream os = _os;

                        InputStream is = socket.getInputStream();

                        // Ask device if it is an AirPrima sensor station
                        send(os, "Hallo. Bist du eine AirPrima Station ?");

                        // The sensor station has 3 seconds to answer
                        sleep(3000);

                        progressDialog.dismiss();

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
                                } else if (mode.equals("stationary")) {
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
                                } else {
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

                                if (txt.equals("Deine Datenbank ist aktuell.")) {
                                    sleep(10000);
                                } else if (txt.matches(".*MEASUREMENT;.*")) {
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
                                    float pm2_5 = Float.parseFloat(measurement[3]);
                                    float pm10 = Float.parseFloat(measurement[4]);
                                    if (pm10 > 40.0) {
                                        notifyUser(pm10, "10");
                                    }
                                    if (pm2_5 > 25.0) {
                                        notifyUser(pm2_5, "2.5");
                                    }
                                }
                            }


                            Cursor cursor = dbHandler.queryNewestTimestamp(ClientSocketThread.currentLocation);
                            if (cursor != null) {
                                // Get data from cursor
                                cursor.moveToFirst();
                                long timestamp = cursor.getInt(cursor.getColumnIndex("measurement_timestamp"));
                                cursor.close();
                                send(os, "TIMESTAMP;" + timestamp);
                            }

                            sleep(1000);
                        }
                    } else {
                        Log.e(TAG, "Bluetooth socket is empty.");
                        progressDialog.dismiss();
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
        Date date = new Date((long) tmpTimestamp * 1000); // Needs milliseconds and not seconds
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
     * Notifies the User
     */
    private void notifyUser(float value, String pm) {
        NotificationChannel channel = new NotificationChannel("channel01", "name", NotificationManager.IMPORTANCE_HIGH);

        // Register channel with system
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, "channel01")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Kritischer Feinstaubwert")
                .setContentText("Der PM" + pm + " Wert beträgt " + value + " µg/m3")
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        notificationManager.notify(0, notification);
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
        switch (item.getItemId()) {
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