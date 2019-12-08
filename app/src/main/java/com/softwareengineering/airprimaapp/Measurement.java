package com.softwareengineering.airprimaapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A measurement holds the sensor readings at a specific timestamp. It belongs to a location.
 */
public class Measurement implements Parcelable {

    private long id;
    private long timestamp;
    private double pm2_5;
    private double pm10;
    private double temperature;
    private double humidity;
    private long locationId;   // foreign key

    public Measurement(long id, long timestamp, double pm2_5, double pm10, double temperature, double humidity, long locationId) {
        this.id = id;
        this.timestamp = timestamp;
        this.pm2_5 = pm2_5;
        this.pm10 = pm10;
        this.temperature = temperature;
        this.humidity = humidity;
        this.locationId = locationId;
    }

    protected Measurement(Parcel in) {
        this.id = in.readLong();
        this.timestamp = in.readLong();
        this.pm2_5 = in.readDouble();
        this.pm10 = in.readDouble();
        this.temperature = in.readDouble();
        this.humidity = in.readDouble();
        this.locationId = in.readLong();
    }

    public static final Creator<Measurement> CREATOR = new Creator<Measurement>() {
        @Override
        public Measurement createFromParcel(Parcel in) {
            return new Measurement(in);
        }

        @Override
        public Measurement[] newArray(int size) {
            return new Measurement[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.timestamp);
        dest.writeDouble(this.pm2_5);
        dest.writeDouble(this.pm10);
        dest.writeDouble(this.temperature);
        dest.writeDouble(this.humidity);
        dest.writeLong(this.timestamp);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getPm2_5() {
        return pm2_5;
    }

    public void setPm2_5(double pm2_5) {
        this.pm2_5 = pm2_5;
    }

    public double getPm10() {
        return pm10;
    }

    public void setPm10(double pm10) {
        this.pm10 = pm10;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public long getLocationId() {
        return locationId;
    }

    public void setLocationId(long locationId) {
        this.locationId = locationId;
    }
}
