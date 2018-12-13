package com.cross.beaglesightlibs;

import android.os.Parcel;

import org.junit.Before;
import org.robolectric.RobolectricTestRunner;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class TargetTest {
    private ArrayList<Target> targets;
    private Target target;
    private Target target2;

    @Before
    public void setup()
    {
        targets = new ArrayList<>();

        target = new Target();
        target.setName(randomString());
        target.setBuiltin(false);
        target.setId(randomString());

        target.setTargetLocation(randomLocation(target.getId()));

        List<LocationDescription> positionList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            positionList.add(randomLocation(target.getId()));
        }
        target.setShootLocations(positionList);

        target2 = new Target();
        target2.setName(randomString());
        target2.setBuiltin(true);
        target2.setId(randomString());

        target2.setTargetLocation(randomLocation(target.getId()));

        targets.add(target);
        targets.add(target2);
    }

    @Test
    public void PublishedTargetsXML() throws ParserConfigurationException, SAXException, IOException {
        File targetFile = new File("../default_configs/targets.xml");
        FileInputStream fis = new FileInputStream(targetFile);
        List<Target> targets = XmlParser.parseTargetsXML(fis);
        assertNotEquals(0, targets.size());
    }

    @Test
    public void TargetXML() throws ParserConfigurationException, SAXException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XmlParser.serialiseTargets(outputStream, targets);

        String string = outputStream.toString();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(string.getBytes());

        List<Target> targetList = XmlParser.parseTargetsXML(inputStream);

        assertEquals(targets.size(), targetList.size());
        assertTrue(targetList.contains(target));
        assertTrue(targetList.contains(target2));
    }

    @Test
    public void TargetParcelable() {
        {
            Parcel parcel = Parcel.obtain();
            target.writeToParcel(parcel, target.describeContents());
            parcel.setDataPosition(0);

            Target targetOut = Target.CREATOR.createFromParcel(parcel);

            assertEquals(target, targetOut);
        }
        {
            Parcel parcel2 = Parcel.obtain();
            target2.writeToParcel(parcel2, target2.describeContents());
            parcel2.setDataPosition(0);

            Target targetOut2 = Target.CREATOR.createFromParcel(parcel2);

            assertEquals(target2, targetOut2);
        }
    }

    private String randomString() {
        return UUID.randomUUID().toString();
    }

    private LocationDescription randomLocation(String targetID) {
        LocationDescription locationDescription = new LocationDescription();
        locationDescription.setTargetId(targetID);
        locationDescription.setDescription(randomString());
        locationDescription.setLatitude(randomDouble());
        locationDescription.setLongitude(randomDouble());
        locationDescription.setAltitude(randomDouble());

        locationDescription.setLatlng_accuracy(randomFloat());
        locationDescription.setAltitude_accuracy(randomFloat());
        return locationDescription;
    }

    private float randomFloat() {
        return (float) Math.random();
    }

    private double randomDouble() {
        return Math.random();
    }
}
