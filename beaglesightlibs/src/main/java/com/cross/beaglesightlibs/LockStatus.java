package com.cross.beaglesightlibs;

import android.location.Location;

import androidx.room.TypeConverter;

import static com.cross.beaglesightlibs.LockStatus.Status.MEDIUM;
import static com.cross.beaglesightlibs.LockStatus.Status.STRONG;
import static com.cross.beaglesightlibs.LockStatus.Status.WEAK;

public class LockStatus {
    private Location lastLocation;
    private Status status = WEAK;

    public Status getStatus() {
        return status;
    }

    public enum Status {
        WEAK,
        MEDIUM,
        STRONG
    }

    public Status updateLocation(Location location)
    {
        status = WEAK;
        if (lastLocation == null)
        {
            lastLocation = location;
            return status;
        }

        double driftx = Math.abs(lastLocation.getLatitude() - location.getLatitude());
        double drifty = Math.abs(lastLocation.getLongitude() - location.getLongitude());
        double driftTotal = Math.sqrt(driftx * driftx + drifty * drifty);

        lastLocation = location;

        if (driftTotal < 0.000001 && location.getAccuracy() < 5)
        {
            status = STRONG;
        }

        if (driftTotal < 0.000001 || location.getAccuracy() < 5)
        {
            status = MEDIUM;
        }

        return status;
    }
}