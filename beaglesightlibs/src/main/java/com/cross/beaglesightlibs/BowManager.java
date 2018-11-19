package com.cross.beaglesightlibs;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {BowConfig.class, PositionPair.class}, version = 1)
public abstract class BowManager extends RoomDatabase {
    @SuppressLint("StaticFieldLeak")
    private static volatile BowManager instance;

    public abstract BowConfig.BowConfigDao bowConfigDao();
    public abstract PositionPair.PositionPairDao positionPairDao();

    public static BowManager getInstance(Context cont) {
        synchronized (BowManager.class) {
            if (instance == null && cont != null) {
                instance = Room.databaseBuilder(cont, BowManager.class, "bowconfigs").build();
            }
        }
        return instance;
    }
}
