package com.cross.beaglesightlibs;

import java.util.ArrayList;
import java.util.List;

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
public class BowConfig {

    @PrimaryKey
    @NonNull
    private String id = "";
    private String name = "";
    private String description = "";

    @Ignore
    private List<PositionPair> positionArray = new ArrayList<>();
    @Ignore
    PositionCalculator positionCalculator;

    public BowConfig() {
        initPositionCalculator();
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
        return positionCalculator;
    }

    @Override
    public String toString()
    {
        return String.format("ID: %s, Name: %s, Desc: %s", id, name, description);
    }

    public void initPositionCalculator()
    {
        positionCalculator = new LineOfBestFitCalculator();
    }

    @Dao
    public interface BowConfigDao {
        @Transaction
        @Query("SELECT * FROM bowconfig")
        List<BowConfig> getAll();

        @Insert(onConflict = REPLACE)
        void insertAll(BowConfig bowConfig);

        @Delete
        void delete(BowConfig bowConfig);
    }
}