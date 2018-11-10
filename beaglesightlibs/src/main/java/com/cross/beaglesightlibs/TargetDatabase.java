package com.cross.beaglesightlibs;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Target.class, LocationDescription.class}, version = 1)
public abstract class TargetDatabase extends RoomDatabase {
    public abstract Target.TargetDao targetDao();
    public abstract LocationDescription.LocationDescriptionDao locationDescriptionDao();
}
