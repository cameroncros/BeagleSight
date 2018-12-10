package com.cross.beaglesightlibs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.cross.beaglesightlibs.exceptions.InvalidNumberFormatException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Target.class, LocationDescription.class}, version = 1)
public abstract class TargetManager extends RoomDatabase {
    @SuppressLint("StaticFieldLeak")
    private static volatile TargetManager instance;
    private static LocationDescription.LocationDescriptionDao locationDescriptionDao;
    private static Target.TargetDao targetDao;

    public abstract Target.TargetDao targetDao();
    public abstract LocationDescription.LocationDescriptionDao locationDescriptionDao();

    public static TargetManager getInstance(Context cont) {
        synchronized (TargetManager.class) {
            if (instance == null && cont != null) {
                instance = Room.databaseBuilder(cont, TargetManager.class, "targets").build();

                targetDao = instance.targetDao();
                locationDescriptionDao = instance.locationDescriptionDao();
            }
        }
        return instance;
    }

    public List<Target> getTargets()
    {
        return targetDao().getAll();
    }

    public List<Target> getTargetsWithShootPositions()
    {
        List<Target> targets = targetDao.getAll();
        for (Target target : targets)
        {
            target.setShootLocations(locationDescriptionDao.getLocationsForTargetId(target.getId()));
        }
        return targets;
    }

    public void saveTargets(List<Target> targets)
    {
        for (Target target : targets)
        {
            saveTarget(target);
        }
    }

    public void deleteTarget(Target selectedTarget) {
        // Should CASCADE through and remove all shoot positions as well.
        targetDao.delete(selectedTarget);
    }

    public void saveTarget(Target target) {
        targetDao.insertAll(target);
        List<LocationDescription> locations = target.getShootLocations();
        if (locations != null)
        {
            for (LocationDescription location : locations)
            {
                if (!location.getTargetId().equals(target.getId()))
                {
                    Log.e("BeagleSight", "Shootlocation has incorrect targetID");
                    location.setTargetId(target.getId());
                }
                locationDescriptionDao.insertAll(location);
            }
        }
    }
}

