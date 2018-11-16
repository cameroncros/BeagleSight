package com.cross.beaglesightlibs;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

@Entity
public class Target {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private boolean builtin;

    @Embedded
    private LocationDescription targetLocation;

    public Target()
    {
        id=UUID.randomUUID().toString();
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

    @Dao
    public interface TargetDao {
        @Transaction
        @Query("SELECT * FROM target")
        List<Target> getAll();

        @Update
        void insertAll(Target target);

        @Delete
        void delete(Target user);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Target)) return false;
        Target target = (Target) o;
        return builtin == target.builtin &&
                Objects.equals(id, target.id) &&
                Objects.equals(name, target.name) &&
                Objects.equals(targetLocation, target.targetLocation);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {

        return Objects.hash(id, name, builtin, targetLocation);
    }

}