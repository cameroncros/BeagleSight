package com.cross.beaglesightlibs;

import android.location.Location;

import androidx.room.TypeConverter;

public class LockStatus {
    private Location lastLocation;

    public enum Status {
        WEAK,
        MEDIUM,
        STRONG
    }

    public Status updateLocation(Location location)
    {
        if (lastLocation == null)
        {
            lastLocation = location;
            return Status.WEAK;
        }

        double driftx = Math.abs(lastLocation.getLatitude() - location.getLatitude());
        double drifty = Math.abs(lastLocation.getLongitude() - location.getLongitude());
        double driftTotal = Math.sqrt(driftx * driftx + drifty * drifty);

        lastLocation = location;

        if (driftTotal < 0.000001 && location.getAccuracy() < 5)
        {
            return Status.STRONG;
        }

        if (driftTotal < 0.000001 || location.getAccuracy() < 5)
        {
            return Status.MEDIUM;
        }

        return Status.WEAK;
    }
}