package com.softwareengineering.airprimaapp.bluetooth;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.os.Build;
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

import com.softwareengineering.airprimaapp.other.DatabaseHandler;
import com.softwareengineering.airprimaapp.menues.InformationActivity;
import com.softwareengineering.airprimaapp.menues.MainActivity;
import com.softwareengineering.airprimaapp.R;
import com.softwareengineering.airprimaapp.visualization.VisualizationActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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

    public volatile static boolean THREAD_RUN = false;
    public volatile static long MOBILE_MEASUREMENT_ID = 5000;
    private long locationID;

    private static final String TAG = ConnectActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 123;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 321;
    private static final UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

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

    // Notification
    private static final String CHANNEL_ID = "AIRPRIMA_YDLD";
    private static final int notificationId = 1066;

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

        // Notification
        createNotificationChannel();

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

        // Get the location id
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            locationID = bundle.getLong("id");
            Log.d(TAG, "==========> LocationID = " + locationID);
        }
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
            DatabaseHandler dbHandler;

            @Override
            public void run() {
                try {
                    THREAD_RUN = true;
                    Log.d(TAG, "Starting " + t.getName());
                    t.start();
                    Log.d(TAG, "JOIN - Waiting for " + t.getName() + " to die!");
                    t.join();
                    BluetoothSocket socket = t.getSocket();

                    if (socket != null) {

                        Log.d(TAG, "Got BluetoothSocket instance! (Socket != null)");
                        dbHandler = new DatabaseHandler(context);

                        // Create OutputStream
                        OutputStream _os = null;
                        try {
                            _os = socket.getOutputStream();
                        } catch (IOException e) {
                            Log.e(TAG, "OutputStream null", e);
                        }
                        final OutputStream outputStream = _os;

                        // Create InputStream
                        InputStream inputStream = socket.getInputStream();

                        // Ask device if it is an AirPrima sensor station
                        send(outputStream, "Hallo. Bist du eine AirPrima Station ?");

                        // Receive answer: Is it an AirPrima sensor station?
                        String initStr = receive(inputStream, 1000);
                        if (initStr != null) {

                            // Analyse received message
                            if(initStr.equals("Ja, bin ich.")) {
                                Log.d(TAG, "INIT - Confirmation received!");

                                int locationMeasuringFreq = 1;  // Server overrides this value! Uses 20 seconds for mobile measurement
                                long timestamp = 1454000043;    // Some old timestamp ...

                                // Get measuring frequency and latest timestamp for stationary measurement
                                if(locationID != MOBILE_MEASUREMENT_ID) {

                                    // Get measuring frequency
                                    Cursor cursor = dbHandler.querySpecificLocation(locationID);
                                    if (cursor != null) {
                                        if (cursor.getCount() > 0) {
                                            cursor.moveToFirst();
                                            locationMeasuringFreq = cursor.getInt(cursor.getColumnIndex("location_measuring_freq"));
                                        }
                                        cursor.close();
                                    }

                                    // Get latest timestamp
                                    Cursor cursor2 = dbHandler.queryNewestTimestamp(locationID);
                                    if (cursor2 != null) {
                                        if(cursor2.getCount() > 0) {
                                            cursor2.moveToFirst();
                                            timestamp = cursor2.getInt(cursor2.getColumnIndex("measurement_timestamp"));
                                        }
                                        cursor2.close();
                                    }

                                } else {
                                    dbHandler.deleteAllMobileMeasurements();
                                }

                                // Send initialization message
                                Log.d(TAG, "INIT - Send initialization");
                                send(outputStream, "INIT;" + locationID + ";" + locationMeasuringFreq + ";" + timestamp);

                                // Receive answer: Initialization successful?
                                String initFeedback = receive(inputStream, 1000);
                                if (initFeedback != null) {

                                    if (initFeedback.equals("Initialisierung erfolgreich.")) {
                                        Log.d(TAG, "INIT - Successful!");

                                        progressDialog.dismiss();

                                        // Open the visualization activity
                                        Intent intentVisualize = new Intent();
                                        intentVisualize.setClass(context, VisualizationActivity.class);
                                        intentVisualize.putExtra("id", locationID);

                                        if(locationID != MOBILE_MEASUREMENT_ID) {
                                            intentVisualize.putExtra("mobile", false);
                                            intentVisualize.putExtra("connected", true);
                                        } else {
                                            intentVisualize.putExtra("mobile", true);
                                            intentVisualize.putExtra("connected", true);
                                        }
                                        startActivity(intentVisualize);

                                    } else {
                                        keepRunning = false;
                                        Log.e(TAG, "INIT - Failed on Server side!");
                                    }
                                } else {
                                    keepRunning = false;
                                    Log.e(TAG, "INIT - InitFeedback is null!");
                                }
                            } else {
                                keepRunning = false;
                                Log.e(TAG, "INIT - It is no AirPrima sensor station!");
                            }
                        } else {
                            keepRunning = false;
                            Log.e(TAG, "INIT - InitStr is null!");
                        }

                        // Toast for user if no connection could be made
                        if(!keepRunning) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, R.string.bt_connect_connection_failed, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        // Main logic after initialization
                        while (keepRunning && THREAD_RUN) {

                            Log.d(TAG, "LOOP - THREAD_RUN = " + String.valueOf(THREAD_RUN));

                            // Receive data in main loop
                            String data = receive(inputStream, 1000);
                            if (data != null) {

                                if (data.equals("Deine Datenbank ist aktuell.")) {

                                    try {
                                        sleep(10000);
                                    } catch (Exception e) {
                                        Log.e(TAG, "LOOP - Sleep interrupted!");
                                    }

                                    // Send latest/newest timestamp to sensor station, so that it can compare it with its latest timestamp
                                    Log.d(TAG, "LOOP - Sending latest timestamp");
                                    long timestamp = 1454000043;    // Some old timestamp ...
                                    Cursor cursor = dbHandler.queryNewestTimestamp(locationID);
                                    if (cursor != null) {
                                        if(cursor.getCount() > 0) {
                                            cursor.moveToFirst();
                                            timestamp = cursor.getInt(cursor.getColumnIndex("measurement_timestamp"));
                                            Log.d(TAG, "LOOP - Send latest timestamp " + timestamp);
                                        } else {
                                            Log.d(TAG, "LOOP - Send old timestamp " + timestamp);
                                        }
                                        send(outputStream, "TIMESTAMP;" + timestamp);
                                        cursor.close();
                                    }

                                } else if (data.matches(".*MEASUREMENT;.*")) {
                                    String[] measurements = data.split(";");
                                    int counter = 1;
                                    for(int i = 0; i < (measurements.length - 1) / 6; i++) {

                                        String strTemp = measurements[counter + 3];
                                        String strHumid = measurements[counter + 4];

                                        if(!strTemp.equals("None") && !strHumid.equals("None")) {

                                            dbHandler.insertMeasurement(
                                                    Long.parseLong(measurements[counter]),
                                                    unixTimestampToSQLiteTimestring(measurements[counter]),
                                                    Float.parseFloat(measurements[counter + 1]),
                                                    Float.parseFloat(measurements[counter + 2]),
                                                    Float.parseFloat(measurements[counter + 3]),
                                                    Float.parseFloat(measurements[counter + 4]),
                                                    Long.parseLong(measurements[counter + 5])
                                            );

                                            // Check the finedust measurements and notify user when they are too high
                                            float pm2_5 = Float.parseFloat(measurements[counter + 1]);
                                            float pm10 = Float.parseFloat(measurements[counter + 2]);
                                            if (pm10 > 50.0) {
                                                notifyUser("10", pm10);
                                            }
                                            if (pm2_5 > 25.0) {
                                                notifyUser("2.5", pm2_5);
                                            }
                                        }

                                        counter += 6;
                                    }
                                    Log.d(TAG, "LOOP - " + counter / 6 + " Measurements received!");
                                }
                            } else {
                                Log.e(TAG, "LOOP - Data is null!");
                            }
                        }
                    } else {
                        Log.e(TAG, "INIT - BluetoothSocket is null!");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, R.string.bt_connect_connection_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (InterruptedException | IOException e) {
                    Log.e(TAG, null, e);
                    keepRunning = false;
                } finally {
                    Log.d(TAG, "FINALLY - Calling cancel() of " + t.getName());
                    if(dbHandler != null) {
                        dbHandler.close();
                    }
                    t.cancel();
                    progressDialog.dismiss();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.bt_connect_thread_dead, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
        workerThread.start();
        return workerThread;
    }

    /**
     * Converts an Unix timestamp to a SQlite timestring
     */
    public String unixTimestampToSQLiteTimestring(String timestamp) {
        long tmpTimestamp = Long.parseLong(timestamp);
        Date date = new Date(tmpTimestamp * 1000); // Needs milliseconds and not seconds
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
            Log.d(TAG, "WRITE - >>" + text + "<<");
            os.write(text.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "WRITE - Error while sending", e);
        }
    }

    /**
     * Receive string from AirPrima station
     */
    private String receive(InputStream in, int maxLoopCount) {

        final byte delimiter = 10;              // This is the ASCII code for a newline character
        byte[] readBuffer = new byte[1024];
        int readBufferPosition = 0;
        int loopCounter = 0;
        boolean stop = false;

        while(!stop && loopCounter != maxLoopCount) {
            try {
                int bytesAvailable = in.available();
                if(bytesAvailable > 0) {

                    byte[] packetBytes = new byte[bytesAvailable];
                    in.read(packetBytes);

                    for(int i = 0; i < bytesAvailable; i++) {
                        byte b = packetBytes[i];

                        if(b == delimiter) {    // Is the byte a '\n'
                            byte[] encodedBytes = new byte[readBufferPosition];
                            System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                            final String data = new String(encodedBytes, StandardCharsets.US_ASCII);
                            readBufferPosition = 0;

                            Log.d(TAG, "RECEIVE - >>" + data + "<<");
                            return data;

                        } else {
                            readBuffer[readBufferPosition++] = b;
                        }
                    }
                } else {
                    Log.d(TAG, "RECEIVE - Available bytes < 0!");
                }
            } catch (IOException ex) {
                stop = true;
            }

            loopCounter++;
            Log.d(TAG, "RECEIVE - In progress (Counter: " + loopCounter + ")");
        }
        Log.d(TAG, "RECEIVE - NULL!");
        return null;
    }

    /**
     * Register notification channel
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Notifies the user when the finedust measurements are too high
     */
    public void notifyUser(String pm, float value) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(getString(R.string.finedust_notification_title))
                .setContentText(getString(R.string.finedust_notification_content, pm, String.valueOf(value)))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, builder.build());
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