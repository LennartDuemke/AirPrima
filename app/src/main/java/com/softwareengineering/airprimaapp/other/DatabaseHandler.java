package com.softwareengineering.airprimaapp.other;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.softwareengineering.airprimaapp.bluetooth.ConnectActivity;

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
    public DatabaseHandler(Context context) {
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
     * Deletes a location in the table "location"
     */
    public void deleteLocation(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int numDeleted = db.delete(TABLE_LOCATION, LOCATION_ID + " = ?", new String[]{Long.toString(id)});
        Log.d(TAG, "DB - deleteLocation(): id = " + id + " -> " + numDeleted);
    }

    /**
     * Deletes all measurements that belong to a specific locationID
     */
    public void deleteLocationMeasurements(long locationID) {
        SQLiteDatabase db = getWritableDatabase();
        int numDeleted = db.delete(TABLE_MEASUREMENT, MEASUREMENT_LOCATION + " = ?",
                new String[]{Long.toString(locationID)});
        Log.d(TAG, "DB - deleteLocationMeasurements(): locationId = " + locationID + " -> " + numDeleted);
    }

    /**
     * Deletes the mobile measurements that do not have to be saved
     */
    public void deleteAllMobileMeasurements() {
        SQLiteDatabase db = getWritableDatabase();
        int numDeleted = db.delete(TABLE_MEASUREMENT, MEASUREMENT_LOCATION + " = ?",
                new String[]{String.valueOf(ConnectActivity.MOBILE_MEASUREMENT_ID)});
        Log.d(TAG, "DB - deleteAllMobileMeasurements(): count -> " + numDeleted);
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
     * SQL query that returns the newest timestamp for one locationID
     */
    public Cursor queryNewestTimestamp(long locationID) {
        SQLiteDatabase db = getWritableDatabase();
        String strLocationID = String.valueOf(locationID);
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP},
                "measurement_location=? AND measurement_timestamp_fmt=(Select max(measurement_timestamp_fmt)" +
                        " from measurement where measurement_location=?)",
                new String[]{strLocationID, strLocationID}, null, null, null);
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
                null, null, "year DESC");
    }

    /**
     * SQL query that returns the unique calendar weeks that are in the database (for specific locationId)
     */
    public Cursor queryAllWeeks(long locationID, String year) {
        SQLiteDatabase db = getWritableDatabase();
        String whereString = MEASUREMENT_LOCATION + "=? AND strftime('%Y', measurement_timestamp_fmt)=?";
        return db.query(TABLE_MEASUREMENT, new String[]{"DISTINCT (strftime('%W', measurement_timestamp_fmt)) AS week"},
                whereString, new String[]{String.valueOf(locationID), year},
                null, null, "week ASC");
    }

    /**
     * SQL query that returns the unique months with measurements of a specific year and locationId
     */
    public Cursor queryAllMonths(long locationID, String year) {
        SQLiteDatabase db = getWritableDatabase();
        String whereString = MEASUREMENT_LOCATION + "=? AND strftime('%Y', measurement_timestamp_fmt)=?";
        return db.query(TABLE_MEASUREMENT, new String[]{"DISTINCT (strftime('%m', measurement_timestamp_fmt)) AS month"},
                whereString, new String[]{String.valueOf(locationID), year},
                null, null, "month ASC");
    }

    /**
     * SQL query that returns the unique days with measurements of a specific year and month and locationId
     */
    public Cursor queryAllDays(long locationID, String year, String month) {
        SQLiteDatabase db = getWritableDatabase();
        String whereString = MEASUREMENT_LOCATION + "=? AND strftime('%Y', measurement_timestamp_fmt)=? AND " +
                "strftime('%m', measurement_timestamp_fmt)=?";
        return db.query(TABLE_MEASUREMENT, new String[]{"DISTINCT (strftime('%d', measurement_timestamp_fmt)) AS day"},
                whereString, new String[]{String.valueOf(locationID), year, month},
                null, null, "day ASC");
    }

    /**
     * SQL query that returns all the measurements of one specific day for one locationID
     */
    public Cursor queryDay(long locationID, String year, String month, String day) {
        SQLiteDatabase db = getWritableDatabase();
        String tmpDay = year + "-" + month + "-" + day + " 01:01:01";
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP_FMT, MEASUREMENT_PM_2_5,
                        MEASUREMENT_PM_10, MEASUREMENT_TEMP, MEASUREMENT_HUM},
                "measurement_location=? AND date(measurement_timestamp_fmt)=date(?)",
                new String[]{String.valueOf(locationID), tmpDay}, null, null,
                MEASUREMENT_TIMESTAMP_FMT + " ASC");
    }

    /**
     * SQL query that returns all the measurements of one specific calendar week for one year and locationID
     */
    public Cursor queryWeek(long locationID, String year, String week) {
        SQLiteDatabase db = getWritableDatabase();
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP_FMT, MEASUREMENT_PM_2_5,
                        MEASUREMENT_PM_10, MEASUREMENT_TEMP, MEASUREMENT_HUM},
                "measurement_location=? AND strftime('%Y', measurement_timestamp_fmt)=? AND " +
                        "strftime('%W', measurement_timestamp_fmt)=?",
                new String[]{String.valueOf(locationID), year, week}, null, null,
                MEASUREMENT_TIMESTAMP_FMT + " ASC");
    }

    /**
     * SQL query that returns all the measurements of one specific month for one year and locationID
     */
    public Cursor queryMonth(long locationID, String year, String month) {
        SQLiteDatabase db = getWritableDatabase();
        String tmpMonth = year + "-" + month + "-01 01:01:01";
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP_FMT, MEASUREMENT_PM_2_5,
                        MEASUREMENT_PM_10, MEASUREMENT_TEMP, MEASUREMENT_HUM},
                "measurement_location=? AND strftime('%Y-%m', measurement_timestamp_fmt)=strftime('%Y-%m',?)",
                new String[]{String.valueOf(locationID), tmpMonth}, null, null,
                MEASUREMENT_TIMESTAMP_FMT + " ASC");
    }

    /**
     * SQL query that returns all the measurements of one specific year for one locationID
     */
    public Cursor queryYear(long locationID, String year) {
        SQLiteDatabase db = getWritableDatabase();
        String tmpYear = year + "-01-01 01:01:01";
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP_FMT, MEASUREMENT_PM_2_5,
                        MEASUREMENT_PM_10, MEASUREMENT_TEMP, MEASUREMENT_HUM},
                "measurement_location=? AND strftime('%Y', measurement_timestamp_fmt)=strftime('%Y',?)",
                new String[]{String.valueOf(locationID), tmpYear}, null, null,
                MEASUREMENT_TIMESTAMP_FMT + " ASC");
    }

    /**
     * SQL query that returns all the measurements of one specific year for one locationID --> returns PM2.5
     */
    public Cursor queryYearPm2_5(long locationID, String year) {
        SQLiteDatabase db = getWritableDatabase();
        String tmpYear = year + "-01-01 01:01:01";
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP_FMT, MEASUREMENT_PM_2_5},
                "measurement_location=? AND strftime('%Y', measurement_timestamp_fmt)=strftime('%Y',?)",
                new String[]{String.valueOf(locationID), tmpYear}, null, null,
                MEASUREMENT_TIMESTAMP_FMT + " ASC");
    }

    /**
     * SQL query that returns all the measurements of one specific year for one locationID --> returns PM10
     */
    public Cursor queryYearPm10(long locationID, String year) {
        SQLiteDatabase db = getWritableDatabase();
        String tmpYear = year + "-01-01 01:01:01";
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP_FMT, MEASUREMENT_PM_10},
                "measurement_location=? AND strftime('%Y', measurement_timestamp_fmt)=strftime('%Y',?)",
                new String[]{String.valueOf(locationID), tmpYear}, null, null,
                MEASUREMENT_TIMESTAMP_FMT + " ASC");
    }

    /**
     * SQL query that returns all the measurements of the newest day for one locationID
     */
    public Cursor queryNewestDay(long locationID) {
        SQLiteDatabase db = getWritableDatabase();
        String strLocationID = String.valueOf(locationID);
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP_FMT, MEASUREMENT_PM_2_5,
                        MEASUREMENT_PM_10, MEASUREMENT_TEMP, MEASUREMENT_HUM},
                "measurement_location=? AND date(measurement_timestamp_fmt)=date((SELECT " +
                        "max(measurement_timestamp_fmt) from measurement where measurement_location=?))",
                new String[]{strLocationID, strLocationID}, null, null,
                MEASUREMENT_TIMESTAMP_FMT + " ASC");
    }

    /**
     * SQL query that returns all the measurements of the newest calendar week for one locationID
     */
    public Cursor queryNewestWeek(long locationID) {
        SQLiteDatabase db = getWritableDatabase();
        String strLocationID = String.valueOf(locationID);
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP_FMT, MEASUREMENT_PM_2_5,
                        MEASUREMENT_PM_10, MEASUREMENT_TEMP, MEASUREMENT_HUM},
                "measurement_location=? AND strftime('%Y', measurement_timestamp_fmt)" +
                        "=strftime('%Y',(SELECT max(measurement_timestamp_fmt) from measurement" +
                        " where measurement_location=?)) AND strftime('%W', measurement_timestamp_fmt)" +
                        "=strftime('%W',(SELECT max(measurement_timestamp_fmt)" +
                        " from measurement where measurement_location=?))",
                new String[]{strLocationID, strLocationID, strLocationID}, null, null,
                MEASUREMENT_TIMESTAMP_FMT + " ASC");
    }

    /**
     * SQL query that returns all the measurements of the newest month for one locationID
     */
    public Cursor queryNewestMonth(long locationID) {
        SQLiteDatabase db = getWritableDatabase();
        String strLocationID = String.valueOf(locationID);
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP_FMT, MEASUREMENT_PM_2_5,
                        MEASUREMENT_PM_10, MEASUREMENT_TEMP, MEASUREMENT_HUM},
                "measurement_location=? AND strftime('%Y-%m', measurement_timestamp_fmt)" +
                        "=strftime('%Y-%m',(SELECT max(measurement_timestamp_fmt) from measurement " +
                        "where measurement_location=?))",
                new String[]{strLocationID, strLocationID}, null, null,
                MEASUREMENT_TIMESTAMP_FMT + " ASC");
    }

    /**
     * SQL query that returns all the measurements of the newest year for one locationID
     */
    public Cursor queryNewestYear(long locationID) {
        SQLiteDatabase db = getWritableDatabase();
        String strLocationID = String.valueOf(locationID);
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP_FMT, MEASUREMENT_PM_2_5,
                        MEASUREMENT_PM_10, MEASUREMENT_TEMP, MEASUREMENT_HUM},
                "measurement_location=? AND strftime('%Y', measurement_timestamp_fmt)" +
                        "=strftime('%Y',(SELECT max(measurement_timestamp_fmt) from measurement" +
                        " where measurement_location=?))",
                new String[]{strLocationID, strLocationID}, null, null,
                MEASUREMENT_TIMESTAMP_FMT + " ASC");
    }

    /**
     * SQL query that returns all the measurements of the newest year for one locationID --> returns PM2.5
     */
    public Cursor queryNewestYearPm2_5(long locationID) {
        SQLiteDatabase db = getWritableDatabase();
        String strLocationID = String.valueOf(locationID);
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP_FMT, MEASUREMENT_PM_2_5},
                "measurement_location=? AND strftime('%Y', measurement_timestamp_fmt)" +
                        "=strftime('%Y',(SELECT max(measurement_timestamp_fmt) from measurement" +
                        " where measurement_location=?))",
                new String[]{strLocationID, strLocationID}, null, null,
                MEASUREMENT_TIMESTAMP_FMT + " ASC");
    }

    /**
     * SQL query that returns all the measurements of the newest year for one locationID --> returns PM10
     */
    public Cursor queryNewestYearPm10(long locationID) {
        SQLiteDatabase db = getWritableDatabase();
        String strLocationID = String.valueOf(locationID);
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_TIMESTAMP_FMT, MEASUREMENT_PM_10},
                "measurement_location=? AND strftime('%Y', measurement_timestamp_fmt)" +
                        "=strftime('%Y',(SELECT max(measurement_timestamp_fmt) from measurement" +
                        " where measurement_location=?))",
                new String[]{strLocationID, strLocationID}, null, null,
                MEASUREMENT_TIMESTAMP_FMT + " ASC");
    }

    /**
     * SQL query that returns the newest measurement for one locationID
     */
    public Cursor queryNewestMeasurement(long locationID) {
        SQLiteDatabase db = getWritableDatabase();
        String strLocationID = String.valueOf(locationID);
        return db.query(TABLE_MEASUREMENT, new String[]{MEASUREMENT_PM_2_5, MEASUREMENT_PM_10, MEASUREMENT_TEMP,
                MEASUREMENT_HUM, "strftime('%d', measurement_timestamp_fmt) AS 'day'",
                "strftime('%m', measurement_timestamp_fmt) AS 'month'",
                "strftime('%W', measurement_timestamp_fmt) AS 'week'",
                "strftime('%Y', measurement_timestamp_fmt) AS 'year'"},
                "measurement_location=? AND measurement_timestamp_fmt=(Select max(measurement_timestamp_fmt)" +
                        " from measurement where measurement_location=?)",
                new String[]{strLocationID, strLocationID}, null, null, null);
    }

    /**
     * SQL query that returns the min and max values for one specific day for one locationID
     */
    public Cursor queryMinMaxDay(long locationID, String year, String month, String day) {
        SQLiteDatabase db = getWritableDatabase();
        String tmpDay = year + "-" + month + "-" + day + " 01:01:01";
        return db.query(TABLE_MEASUREMENT, new String[]{"max(measurement_pm_2_5) AS 'max2_5'",
                        "min(measurement_pm_2_5) AS 'min2_5'", "max(measurement_pm_10) AS 'max10'",
                        "min(measurement_pm_10) AS 'min10'", "max(measurement_temp) AS 'maxTemp'",
                        "min(measurement_temp) AS 'minTemp'", "max(measurement_hum) AS 'maxHum'",
                        "min(measurement_hum) AS 'minHum'"},
                "measurement_location=? AND date(measurement_timestamp_fmt)=date(?)",
                new String[]{String.valueOf(locationID), tmpDay}, null, null, null);
    }

    /**
     * SQL query that returns the min and max values for one specific calendar week of one year and locationID
     */
    public Cursor queryMinMaxWeek(long locationID, String year, String week) {
        SQLiteDatabase db = getWritableDatabase();
        return db.query(TABLE_MEASUREMENT, new String[]{"max(measurement_pm_2_5) AS 'max2_5'",
                        "min(measurement_pm_2_5) AS 'min2_5'", "max(measurement_pm_10) AS 'max10'",
                        "min(measurement_pm_10) AS 'min10'", "max(measurement_temp) AS 'maxTemp'",
                        "min(measurement_temp) AS 'minTemp'", "max(measurement_hum) AS 'maxHum'",
                        "min(measurement_hum) AS 'minHum'"},
                "measurement_location=? AND strftime('%Y', measurement_timestamp_fmt)=? AND " +
                        "strftime('%W', measurement_timestamp_fmt)=?",
                new String[]{String.valueOf(locationID), year, week}, null, null, null);
    }

    /**
     * SQL query that returns the min and max values for one specific month for one year and locationID
     */
    public Cursor queryMinMaxMonth(long locationID, String year, String month) {
        SQLiteDatabase db = getWritableDatabase();
        String tmpMonth = year + "-" + month + "-01 01:01:01";
        return db.query(TABLE_MEASUREMENT, new String[]{"max(measurement_pm_2_5) AS 'max2_5'",
                        "min(measurement_pm_2_5) AS 'min2_5'", "max(measurement_pm_10) AS 'max10'",
                        "min(measurement_pm_10) AS 'min10'", "max(measurement_temp) AS 'maxTemp'",
                        "min(measurement_temp) AS 'minTemp'", "max(measurement_hum) AS 'maxHum'",
                        "min(measurement_hum) AS 'minHum'"},
                "measurement_location=? AND strftime('%Y-%m', measurement_timestamp_fmt)=strftime('%Y-%m',?)",
                new String[]{String.valueOf(locationID), tmpMonth}, null, null, null);
    }

    /**
     * SQL query that returns the min and max values for one specific year for one locationID
     */
    public Cursor queryMinMaxYear(long locationID, String year) {
        SQLiteDatabase db = getWritableDatabase();
        String tmpYear = year + "-01-01 01:01:01";
        return db.query(TABLE_MEASUREMENT, new String[]{"max(measurement_pm_2_5) AS 'max2_5'",
                        "min(measurement_pm_2_5) AS 'min2_5'", "max(measurement_pm_10) AS 'max10'",
                        "min(measurement_pm_10) AS 'min10'", "max(measurement_temp) AS 'maxTemp'",
                        "min(measurement_temp) AS 'minTemp'", "max(measurement_hum) AS 'maxHum'",
                        "min(measurement_hum) AS 'minHum'"},
                "measurement_location=? AND strftime('%Y', measurement_timestamp_fmt)=strftime('%Y',?)",
                new String[]{String.valueOf(locationID), tmpYear}, null, null, null);
    }

    /**
     * SQL query that returns the min and max values of the newest day for one locationID
     */
    public Cursor queryNewestDayMinMax(long locationID) {
        SQLiteDatabase db = getWritableDatabase();
        String strLocationID = String.valueOf(locationID);
        return db.query(TABLE_MEASUREMENT, new String[]{"max(measurement_pm_2_5) AS 'max2_5'",
                        "min(measurement_pm_2_5) AS 'min2_5'", "max(measurement_pm_10) AS 'max10'",
                        "min(measurement_pm_10) AS 'min10'", "max(measurement_temp) AS 'maxTemp'",
                        "min(measurement_temp) AS 'minTemp'", "max(measurement_hum) AS 'maxHum'",
                        "min(measurement_hum) AS 'minHum'"},
                "measurement_location=? AND date(measurement_timestamp_fmt)=date((SELECT " +
                        "max(measurement_timestamp_fmt) from measurement where measurement_location=?))",
                new String[]{strLocationID, strLocationID}, null, null, null);
    }
}