package com.cross.beaglesightlibs;

import android.location.Location;
import android.os.Build;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Update;

import static androidx.room.ForeignKey.CASCADE;
import static java.lang.Math.atan;

@Entity(foreignKeys = @ForeignKey(entity = Target.class,
                                  parentColumns = "id",
                                  childColumns = "targetId",
                                  onDelete = CASCADE),
        indices = @Index("targetId"))
public class LocationDescription {
    @PrimaryKey
    @NonNull
    private String locationId;
    private String targetId;
    private double latitude;
    private double longitude;
    private float latlng_accuracy;
    private double altitude;
    private float altitude_accuracy;

    @ColumnInfo(name = "location_description")
    private String description;

    public LocationDescription()
    {

    }

    public LocationDescription(Location location, String description)
    {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.altitude = location.getAltitude();
        this.latlng_accuracy = location.getAccuracy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.altitude_accuracy = location.getVerticalAccuracyMeters();
        }
        this.description = description;
    }

    public LocationDescription(Location currentLocation) {
        this(currentLocation, "");
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLng getLatLng()
    {
        return new LatLng(latitude, longitude);
    }

    public void setLatLng(LatLng latlng)
    {
        this.latitude = latlng.latitude;
        this.longitude = latlng.longitude;

    }

    public float getLatlng_accuracy() {
        return latlng_accuracy;
    }

    public void setLatlng_accuracy(float latlng_accuracy) {
        this.latlng_accuracy = latlng_accuracy;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getAltitude_accuracy() {
        return altitude_accuracy;
    }

    public void setAltitude_accuracy(float altitude_accuracy) {
        this.altitude_accuracy = altitude_accuracy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the distance to a location
     * @param location The location to calculate the distance to
     * @return Distance in meters
     */
    public double distanceTo(LocationDescription location) {
        LatLng pos = location.getLatLng();
        float results[] = new float[2];
        Location.distanceBetween(latitude, longitude, pos.latitude, pos.longitude, results);
        return results[0];
    }

    /**
     * Get angle to the target
     * @param pos position to the target
     * @return Angle in degrees to the target. Positive means aiming uphill.
     */
    public double pitchTo(LocationDescription pos) {
        double distance = distanceTo(pos);
        double elevation = altitude-pos.altitude;
        double radians = atan(elevation/distance);
        return radians*180/Math.PI;
    }

    @Dao
    public interface LocationDescriptionDao {
        @Query("SELECT * FROM locationdescription")
        List<LocationDescription> getAll();

        @Query("SELECT * FROM locationdescription WHERE targetId IN (:targetId)")
        List<LocationDescription> getLocationsForTarget(String targetId);

        @Update
        void insertAll(LocationDescription... locationDescriptions);

        @Delete
        void delete(LocationDescription user);
    }
}
