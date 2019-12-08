package com.softwareengineering.airprimaapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A user can view and save the measurements of a specific location.
 */
public class Location implements Parcelable {

    private long id;
    private String name;
    private int measuringFreq;

    public Location(long id, String name, int measuringFreq) {
        this.id = id;
        this.name = name;
        this.measuringFreq = measuringFreq;
    }

    protected Location(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.measuringFreq = in.readInt();
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.measuringFreq);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMeasuringFreq() {
        return measuringFreq;
    }

    public void setMeasuringFreq(int measuringFreq) {
        this.measuringFreq = measuringFreq;
    }
}