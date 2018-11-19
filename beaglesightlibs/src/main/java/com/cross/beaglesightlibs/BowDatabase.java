package com.cross.beaglesightlibs;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {BowConfig.class, PositionPair.class}, version = 1)
public abstract class BowDatabase extends RoomDatabase {
    public abstract BowConfig.BowConfigDao bowConfigDao();
    public abstract PositionPair.PositionPairDao positionPairDao();
}
