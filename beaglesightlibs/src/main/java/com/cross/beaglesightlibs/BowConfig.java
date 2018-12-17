package com.cross.beaglesightlibs;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Transaction;

import static androidx.room.OnConflictStrategy.REPLACE;

@Entity
public class BowConfig implements Parcelable {
    @PrimaryKey
    @NonNull
    private String id = "";
    private String name = "";
    private String description = "";

    @Ignore
    private List<PositionPair> positionArray = new ArrayList<>();

    public BowConfig()
    {

    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PositionPair> getPositionArray() {
        return positionArray;
    }

    public void setPositionArray(List<PositionPair> positionArray) {
        this.positionArray = positionArray;
    }

    public PositionCalculator getPositionCalculator() {
        PositionCalculator positionCalculator = new LineOfBestFitCalculator();
        positionCalculator.setPositions(positionArray);
        return positionCalculator;
    }

    @Override
    public String toString() {
        return String.format("ID: %s, Name: %s, Desc: %s", id, name, description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BowConfig)) return false;
        BowConfig bowConfig = (BowConfig) o;
        return Objects.equals(id, bowConfig.id) &&
                Objects.equals(name, bowConfig.name) &&
                Objects.equals(description, bowConfig.description) &&
                Objects.equals(positionArray, bowConfig.positionArray);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, positionArray);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(description);

        for (PositionPair pair : positionArray)
        {
            parcel.writeParcelable(pair, i);
        }
    }

    public BowConfig(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();

        while (in.dataPosition() < in.dataSize())
        {
            PositionPair pair = in.readParcelable(PositionPair.class.getClassLoader());
            positionArray.add(pair);
        }
    }

    public static final Creator<BowConfig> CREATOR = new Creator<BowConfig>() {
        @Override
        public BowConfig createFromParcel(Parcel in) {
            return new BowConfig(in);
        }

        @Override
        public BowConfig[] newArray(int size) {
            return new BowConfig[size];
        }
    };

    @Dao
    public interface BowConfigDao {
        @Transaction
        @Query("SELECT * FROM bowconfig")
        List<BowConfig> getAll();

        @Insert(onConflict = REPLACE)
        void insert(BowConfig bowConfig);

        @Delete
        void delete(BowConfig bowConfig);

        @Query("SELECT * FROM bowconfig WHERE id IS (:id)")
        BowConfig get(String id);
    }
}