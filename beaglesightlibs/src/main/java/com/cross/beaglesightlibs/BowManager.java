package com.cross.beaglesightlibs;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.List;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {BowConfig.class, PositionPair.class}, version = 1)
public abstract class BowManager extends RoomDatabase {
    @SuppressLint("StaticFieldLeak")
    private static volatile BowManager instance;
    private static BowConfig.BowConfigDao bowConfigDao;
    private static PositionPair.PositionPairDao positionPairDao;
    private WearSync wearSync;

    public abstract BowConfig.BowConfigDao bowConfigDao();
    public abstract PositionPair.PositionPairDao positionPairDao();

    public static BowManager getInstance(Context cont) {
        synchronized (BowManager.class) {
            if (instance == null && cont != null) {
                instance = Room.databaseBuilder(cont, BowManager.class, "bowconfigs").build();
                instance.wearSync = new WearSync(cont);
                bowConfigDao = instance.bowConfigDao();
                positionPairDao = instance.positionPairDao();
            }
        }
        return instance;
    }

    public List<BowConfig> getAllBowConfigsWithPositions() {
        List<BowConfig> bowConfigs = bowConfigDao.getAll();
        for (BowConfig bowConfig : bowConfigs)
        {
            bowConfig.setPositionArray(positionPairDao.getPositionForBow(bowConfig.getId()));
        }
        return bowConfigs;
    }

    public void deleteBowConfig(BowConfig bowConfig) {
        bowConfigDao.delete(bowConfig);
        wearSync.removeBowConfig(bowConfig);
    }

    public void addBowConfig(BowConfig bowConfig) {
        bowConfigDao.delete(bowConfig);

        bowConfigDao.insert(bowConfig);
        for (PositionPair pair : bowConfig.getPositionArray())
        {
            positionPairDao.insert(pair);
        }

        wearSync.addBowConfig(bowConfig);
    }

    public BowConfig getBowConfig(String id) {
        BowConfig bc = bowConfigDao().get(id);
        if (bc != null) {
            bc.setPositionArray(positionPairDao().getPositionForBow(id));
        }
        return bc;
    }
}
