package com.cross.beaglesightlibs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

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

    public abstract Target.TargetDao targetDao();
    public abstract LocationDescription.LocationDescriptionDao locationDescriptionDao();

    public static TargetManager getInstance(Context cont) {
        synchronized (TargetManager.class) {
            if (instance == null && cont != null) {
                instance = Room.databaseBuilder(cont, TargetManager.class, "targets").build();
            }
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                loadFakeTargets();
            }
        });

        return instance;
    }

    private static void loadFakeTargets() {
        String targetID = "12341234-1234-1234-1234-124312341234";

        // Add target
        LocationDescription targetLoc = new LocationDescription();
        targetLoc.setLocationId(UUID.randomUUID().toString());
        targetLoc.setAltitude(300);
        targetLoc.setAltitude_accuracy(3);
        targetLoc.setLatitude(-37.627482);
        targetLoc.setLongitude(145.122773);
        targetLoc.setLatlng_accuracy(6);
        targetLoc.setDescription("Test Target");
        targetLoc.setTargetId(targetID);

        Target target = new Target();
        target.setId(targetID);
        target.setBuiltin(true);
        target.setTargetLocation(targetLoc);
        target.setName("Test target");
        instance.targetDao().insertAll(target);

        // Add shoot positions
        LocationDescription shootPos1 = new LocationDescription();
        shootPos1.setLocationId(UUID.randomUUID().toString());
        shootPos1.setAltitude(310);
        shootPos1.setAltitude_accuracy(2);
        shootPos1.setLatitude(-37.627442);
        shootPos1.setLongitude(145.122733);
        shootPos1.setLatlng_accuracy(2);
        shootPos1.setDescription("Shoot Pos 1");
        shootPos1.setTargetId(targetID);

        LocationDescription shootPos2 = new LocationDescription();
        shootPos2.setLocationId(UUID.randomUUID().toString());
        shootPos2.setAltitude(270);
        shootPos2.setAltitude_accuracy(4);
        shootPos2.setLatitude(-37.627432);
        shootPos2.setLongitude(145.122573);
        shootPos2.setLatlng_accuracy(6);
        shootPos2.setDescription("Shoot Pos 2");
        shootPos2.setTargetId(targetID);

        instance.locationDescriptionDao().insertAll(shootPos1, shootPos2);
    }
}

