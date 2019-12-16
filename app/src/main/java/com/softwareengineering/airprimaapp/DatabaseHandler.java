package com.softwareengineering.airprimaapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * DatabaseHandler class can create a database, create and drop tables, insert data into tables,
 * update and delete data and handle SQL queries.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // Tag for the logger
    private static final String TAG = DatabaseHandler.class.getSimpleName();

    // Name and version of database
    private static final String DATABASE_NAME = "airprima.db";
    private static final int DATABASE_VERSION = 1;

    // Name und attributes of table "location"
    private static final String TABLE_LOCATION = "location";
    private static final String LOCATION_ID = "_id";
    private static final String LOCATION_NAME = "location_name";
    private static final String LOCATION_MEASURING_FREQ = "location_measuring_freq";

    // Name und attributes of table "measurement"
    private static final String TABLE_MEASUREMENT = "measurement";
    private static final String MEASUREMENT_ID = "_id";
    private static final String MEASUREMENT_TIMESTAMP = "measurement_timestamp";
    private static final String MEASUREMENT_TIMESTAMP_FMT = "measurement_timestamp_fmt";    // timestamp formatted
    private static final String MEASUREMENT_PM_2_5 = "measurement_pm_2_5";
    private static final String MEASUREMENT_PM_10 = "measurement_pm_10";
    private static final String MEASUREMENT_TEMP = "measurement_temp";
    private static final String MEASUREMENT_HUM = "measurement_hum";
    private static final String MEASUREMENT_LOCATION = "measurement_location";      // foreign key

    // Create table "location"
    private static final String CREATE_TABLE_LOCATION = "CREATE TABLE IF NOT EXISTS " + TABLE_LOCATION + " ("
            + LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + LOCATION_NAME + " TEXT NOT NULL, "
            + LOCATION_MEASURING_FREQ + " INTEGER NOT NULL);";

    // Create table "measurement"
    private static final String CREATE_TABLE_MEASUREMENT = "CREATE TABLE IF NOT EXISTS " + TABLE_MEASUREMENT + " ("
            + MEASUREMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + MEASUREMENT_TIMESTAMP + " INTEGER NOT NULL, "
            + MEASUREMENT_TIMESTAMP_FMT + " TEXT NOT NULL, "
            + MEASUREMENT_PM_2_5 + " REAL, "
            + MEASUREMENT_PM_10 + " REAL, "
            + MEASUREMENT_TEMP + " REAL, "
            + MEASUREMENT_HUM + " REAL, "
            + MEASUREMENT_LOCATION + " INTEGER, "
            + "FOREIGN KEY (" + MEASUREMENT_LOCATION + ") REFERENCES " + TABLE_LOCATION + "(" + LOCATION_ID + "));";

    // Drop table "location"
    private static final String DROP_TABLE_LOCATION = "DROP TABLE IF EXISTS " + TABLE_LOCATION;

    // Drop table "measurement"
    private static final String DROP_TABLE_MEASUREMENT = "DROP TABLE IF EXISTS " + TABLE_MEASUREMENT;


    /**
     * Constructor
     */
    DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database file gets created. Creates the tables inside the database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LOCATION);
        db.execSQL(CREATE_TABLE_MEASUREMENT);
    }

    /**
     * Drops tables "location" and "measurement" and creates them new
     */
    public void dropAllAndCreateNew() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(DROP_TABLE_MEASUREMENT);
        db.execSQL(DROP_TABLE_LOCATION);
        onCreate(db);
    }

    /**
     * Drops table "location" and creates it new
     */
    public void dropLocationAndCreateNew() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(DROP_TABLE_LOCATION);
        db.execSQL(CREATE_TABLE_LOCATION);
    }

    /**
     * Drops table "measurement" and creates it new
     */
    public void dropMeasurementAndCreateNew() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(DROP_TABLE_MEASUREMENT);
        db.execSQL(CREATE_TABLE_MEASUREMENT);
    }

    /**
     * Called when the database gets upgraded (DB schema changed). All the tables have to be dropped!
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Database upgrade from " + oldVersion + " to " + newVersion);
        db.execSQL(DROP_TABLE_MEASUREMENT);
        db.execSQL(DROP_TABLE_LOCATION);
        onCreate(db);
    }

    /**
     * Inserts a location into the table "location"
     */
    public void insertLocation(String locationName, int measuringFreq) {
        long rowId = -1;
        try {
            SQLiteDatabase db = getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(LOCATION_NAME, locationName);
            values.put(LOCATION_MEASURING_FREQ, measuringFreq);

            rowId = db.insert(TABLE_LOCATION, null, values);

        } catch (SQLiteException e) {
            Log.e(TAG, "DB - insertLocation()", e);
        } finally {
            Log.d(TAG, "DB - Inserted location: rowId = " + rowId);
        }
    }

    /**
     * Inserts a measurement into the table "measurement"
     */
    public void insertMeasurement(long timestamp, String timestampFmt, float pm2_5, float pm10, float temp, float hum, long locationId) {
        long rowId = -1;
        try {
            SQLiteDatabase db = getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(MEASUREMENT_TIMESTAMP, timestamp);
            values.put(MEASUREMENT_TIMESTAMP_FMT, timestampFmt);
            values.put(MEASUREMENT_PM_2_5, pm2_5);
            values.put(MEASUREMENT_PM_10, pm10);
            values.put(MEASUREMENT_TEMP, temp);
            values.put(MEASUREMENT_HUM, hum);
            values.put(MEASUREMENT_LOCATION, locationId);

            rowId = db.insert(TABLE_MEASUREMENT, null, values);

        } catch (SQLiteException e) {
            Log.e(TAG, "DB - insertMeasurement()", e);
        } finally {
            Log.d(TAG, "DB - Inserted measurement: rowId = " + rowId);
        }
    }

    /**
     * Updates a location in the table "location"
     */
    public void updateLocation(long id, String locationName, int measuringFreq) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LOCATION_NAME, locationName);
        values.put(LOCATION_MEASURING_FREQ, measuringFreq);
        int numUpdated = db.update(TABLE_LOCATION, values, LOCATION_ID + " = ?", new String[]{Long.toString(id)});
        Log.d(TAG, "DB - updateLocation(): id = " + id + " -> " + numUpdated);
    }

    /**
     * Updates a measurement in the table "measurement"
     */
    public void updateMeasurement(long id, long timestamp, String timestampFmt, float pm2_5, float pm10, float temp, float hum, long locationId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MEASUREMENT_TIMESTAMP, timestamp);
        values.put(MEASUREMENT_TIMESTAMP_FMT, timestampFmt);
        values.put(MEASUREMENT_PM_2_5, pm2_5);
        values.put(MEASUREMENT_PM_10, pm10);
        values.put(MEASUREMENT_TEMP, temp);
        values.put(MEASUREMENT_HUM, hum);
        values.put(MEASUREMENT_LOCATION, locationId);
        int numUpdated = db.update(TABLE_MEASUREMENT, values, MEASUREMENT_ID + " = ?", new String[]{Long.toString(id)});
        Log.d(TAG, "DB - updateMeasurement(): id = " + id + " -> " + numUpdated);
    }

    /**
     * Deletes a location in the table "location"
     */
    public void deleteLocation(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int numDeleted = db.delete(TABLE_LOCATION, LOCATION_ID + " = ?", new String[]{Long.toString(id)});
        Log.d(TAG, "DB - deleteLocation(): id = " + id + " -> " + numDeleted);
    }

    /**
     * Deletes a measurement in the table "measurement"
     */
    public void deleteMeasurement(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int numDeleted = db.delete(TABLE_MEASUREMENT, MEASUREMENT_ID + " = ?", new String[]{Long.toString(id)});
        Log.d(TAG, "DB - deleteMeasurement(): id = " + id + " -> " + numDeleted);
    }

    /**
     * SQL query that returns a Cursor with all the locations (sorted)
     */
    public Cursor queryLocations() {
        SQLiteDatabase db = getWritableDatabase();
        return db.query(TABLE_LOCATION, null, null, null,
                null, null, LOCATION_NAME + " ASC");
    }

    /**
     * SQL query that returns a Cursor with all the measurements (sorted)
     */
    public Cursor queryMeasurements() {
        SQLiteDatabase db = getWritableDatabase();
        return db.query(TABLE_MEASUREMENT, null, null, null,
                null, null, MEASUREMENT_TIMESTAMP + " DESC");
    }

    /**
     * SQL query that returns a Cursor with one specific location
     */
    public Cursor querySpecificLocation(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.query(TABLE_LOCATION, null, LOCATION_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
    }

    /**
     * SQL query that returns the unique years that are in the database (for specific locationId)
     */
    public Cursor queryAllYears(long locationID) {
        SQLiteDatabase db = getWritableDatabase();
        return db.query(TABLE_MEASUREMENT, new String[]{"DISTINCT (strftime('%Y', measurement_timestamp_fmt)) AS year"},
                MEASUREMENT_LOCATION + "=?", new String[]{String.valueOf(locationID)},
                null, null, null);
    }

    /**
     * SQL query that returns the unique calendar weeks that are in the database (for specific locationId)
     */
    public Cursor queryAllWeeks(long locationID, String year) {
        SQLiteDatabase db = getWritableDatabase();
        String whereString = MEASUREMENT_LOCATION + "=? AND strftime('%Y', measurement_timestamp_fmt)=?";
        return db.query(TABLE_MEASUREMENT, new String[]{"DISTINCT (strftime('%W', measurement_timestamp_fmt)) AS week"},
                whereString, new String[]{String.valueOf(locationID), year},
                null, null, null);
    }

    /**
     * SQL query that returns the unique months with measurements of a specific year and locationId
     */
    public Cursor queryAllMonths(long locationID, String year) {
        SQLiteDatabase db = getWritableDatabase();
        String whereString = MEASUREMENT_LOCATION + "=? AND strftime('%Y', measurement_timestamp_fmt)=?";
        return db.query(TABLE_MEASUREMENT, new String[]{"DISTINCT (strftime('%m', measurement_timestamp_fmt)) AS month"},
                whereString, new String[]{String.valueOf(locationID), year},
                null, null, null);
    }

    /**
     * SQL query that returns the unique days with measurements of a specific year and month and locationId
     */
    public Cursor queryAllDays(long locationID, String year, String month) {
        SQLiteDatabase db = getWritableDatabase();
        String whereString = MEASUREMENT_LOCATION + "=? AND strftime('%Y', measurement_timestamp_fmt)=? AND strftime('%m', measurement_timestamp_fmt)=?";
        return db.query(TABLE_MEASUREMENT, new String[]{"DISTINCT (strftime('%d', measurement_timestamp_fmt)) AS day"},
                whereString, new String[]{String.valueOf(locationID), year, month},
                null, null, null);
    }

    /**
     * SQL query that returns all the measurements of one specific day for one locationID
     */
    public Cursor queryDay(long locationID, String year, String month, String day) {
        SQLiteDatabase db = getWritableDatabase();
        String tmpDay = year + "-" + month + "-" + day + " 00:00:00";
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP_FMT, MEASUREMENT_PM_2_5,
                        MEASUREMENT_PM_10, MEASUREMENT_TEMP, MEASUREMENT_HUM},
                "measurement_location=? AND date(measurement_timestamp_fmt)=date(?)",
                new String[]{String.valueOf(locationID), tmpDay}, null, null, null);
    }

    /**
     * SQL query that returns all the measurements of one specific calendar week for one year and locationID
     */
    public Cursor queryWeek(long locationID, String year, String week) {
        SQLiteDatabase db = getWritableDatabase();
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP_FMT, MEASUREMENT_PM_2_5,
                        MEASUREMENT_PM_10, MEASUREMENT_TEMP, MEASUREMENT_HUM},
                "measurement_location=? AND strftime('%Y', measurement_timestamp_fmt)=strftime('%Y',?) AND strftime('%W', measurement_timestamp_fmt)=?",
                new String[]{String.valueOf(locationID), year, week}, null, null, null);
    }

    /**
     * SQL query that returns all the measurements of one specific month for one year and locationID
     */
    public Cursor queryMonth(long locationID, String year, String month) {
        SQLiteDatabase db = getWritableDatabase();
        String tmpMonth = year + "-" + month + "-01 00:00:00";
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP_FMT, MEASUREMENT_PM_2_5,
                        MEASUREMENT_PM_10, MEASUREMENT_TEMP, MEASUREMENT_HUM},
                "measurement_location=? AND strftime('%Y-%m', measurement_timestamp_fmt)=strftime('%Y-%m',?)",
                new String[]{String.valueOf(locationID), tmpMonth}, null, null, null);
    }

    /**
     * SQL query that returns all the measurements of one specific year for one locationID
     */
    public Cursor queryYear(long locationID, String year) {
        SQLiteDatabase db = getWritableDatabase();
        String tmpYear = year + "-01-01 00:00:00";
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP_FMT, MEASUREMENT_PM_2_5,
                        MEASUREMENT_PM_10, MEASUREMENT_TEMP, MEASUREMENT_HUM},
                "measurement_location=? AND strftime('%Y', measurement_timestamp_fmt)=strftime('%Y',?)",
                new String[]{String.valueOf(locationID), tmpYear}, null, null, null);
    }
}
