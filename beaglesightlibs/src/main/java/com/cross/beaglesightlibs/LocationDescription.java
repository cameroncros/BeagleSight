package com.cross.beaglesightlibs;

import android.location.Location;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
public class LocationDescription implements Parcelable {
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

    public LocationDescription() {
        this.locationId = UUID.randomUUID().toString();
    }

    public LocationDescription(Location location, String description) {
        super();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.altitude = location.getAltitude();
        this.latlng_accuracy = location.getAccuracy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.altitude_accuracy = location.getVerticalAccuracyMeters();
        }
        this.description = description;

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LocationDescription)) return false;
        LocationDescription that = (LocationDescription) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                Float.compare(that.latlng_accuracy, latlng_accuracy) == 0 &&
                Double.compare(that.altitude, altitude) == 0 &&
                Float.compare(that.altitude_accuracy, altitude_accuracy) == 0 &&
                Objects.equals(locationId, that.locationId) &&
                Objects.equals(targetId, that.targetId) &&
                Objects.equals(description, that.description);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {

        return Objects.hash(locationId, targetId, latitude, longitude, latlng_accuracy, altitude, altitude_accuracy, description);
    }

    public LocationDescription(Location currentLocation) {
        this(currentLocation, "");
    }

    public LocationDescription(Parcel in) {
        String[] data = new String[8];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.latitude = Double.parseDouble(data[0]);
        this.longitude = Double.parseDouble(data[1]);
        this.altitude = Double.parseDouble(data[2]);

        this.latlng_accuracy = Float.parseFloat(data[3]);
        this.altitude_accuracy = Float.parseFloat(data[4]);

        this.locationId = data[5];
        this.targetId = data[6];
        this.description = data[7];
    }

    public static final Creator<LocationDescription> CREATOR = new Creator<LocationDescription>() {
        @Override
        public LocationDescription createFromParcel(Parcel in) {
            return new LocationDescription(in);
        }

        @Override
        public LocationDescription[] newArray(int size) {
            return new LocationDescription[size];
        }
    };

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

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    public void setLatLng(LatLng latlng) {
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
     *
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
     *
     * @param pos position to the target
     * @return Angle in degrees to the target. Positive means aiming uphill.
     */
    public double pitchTo(LocationDescription pos) {
        double distance = distanceTo(pos);
        double elevation = altitude - pos.altitude;
        double radians = atan(elevation / distance);
        return radians * 180 / Math.PI;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[]{
                Double.toString(this.latitude),
                Double.toString(this.longitude),
                Double.toString(this.altitude),

                Float.toString(this.latlng_accuracy),
                Float.toString(this.altitude_accuracy),

                this.locationId,
                this.targetId,
                this.description,
        });
    }

    public String getLocationString() {
        return String.format(Locale.ENGLISH,
                "Lat: %.03f Long: %.03f Alt: %.02f",
                latitude, longitude, altitude);
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
