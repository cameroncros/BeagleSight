package com.cross.beaglesightlibs;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Transaction;

import static androidx.room.OnConflictStrategy.REPLACE;

@Entity
public class Target implements Parcelable {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private boolean builtin;

    @Embedded
    private LocationDescription targetLocation;

    @Ignore
    private List<LocationDescription> shootLocations;

    public Target()
    {
        id=UUID.randomUUID().toString();
        shootLocations = new ArrayList<>();
    }

    public Target(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.builtin = Boolean.parseBoolean(in.readString());

        this.targetLocation = in.readParcelable(LocationDescription.class.getClassLoader());

        shootLocations = new ArrayList<>();
        while (in.dataPosition() < in.dataSize())
        {
            LocationDescription shootPos = in.readParcelable(LocationDescription.class.getClassLoader());
            shootLocations.add(shootPos);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocationDescription getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(LocationDescription targetLocation) {
        this.targetLocation = targetLocation;
    }

    public boolean isBuiltin() {
        return builtin;
    }

    public void setBuiltin(boolean builtin) {
        this.builtin = builtin;
    }

    public List<LocationDescription> getShootLocations() {
        return shootLocations;
    }

    public void setShootLocations(List<LocationDescription> shootLocations) {
        this.shootLocations = shootLocations;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(this.id);
        parcel.writeString(this.name);
        parcel.writeString(Boolean.toString(this.builtin));
        parcel.writeParcelable(targetLocation, flags);

        for (LocationDescription locationDescription : getShootLocations())
        {
            parcel.writeParcelable(locationDescription, flags);
        }
    }

    public static final Creator<Target> CREATOR = new Creator<Target>() {
        @Override
        public Target createFromParcel(Parcel in) {
            return new Target(in);
        }

        @Override
        public Target[] newArray(int size) {
            return new Target[size];
        }
    };

    public void addShootLocation(LocationDescription location) {
        shootLocations.add(location);
    }

    public void removeShootLocation(LocationDescription location) {
        shootLocations.add(location);
    }

    @Dao
    public interface TargetDao {
        @Transaction
        @Query("SELECT * FROM target")
        List<Target> getAll();

        @Transaction
        @Query("SELECT * FROM target WHERE builtin IS (:builtIn) ")
        List<Target> getAll(boolean builtIn);

        @Insert(onConflict = REPLACE)
        void insert(Target target);

        @Delete
        void delete(Target user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Target)) return false;
        Target target = (Target) o;
        if (builtin != target.builtin ||
                !Objects.equals(id, target.id) ||
                !Objects.equals(name, target.name) ||
                !Objects.equals(targetLocation, target.targetLocation))
        {
            return false;
        }
        if (shootLocations.size() != target.getShootLocations().size())
        {
            return false;
        }
        for (LocationDescription loc : shootLocations)
        {
            if (!target.getShootLocations().contains(loc))
            {
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {

        return Objects.hash(id, name, builtin, targetLocation, shootLocations);
    }

}
