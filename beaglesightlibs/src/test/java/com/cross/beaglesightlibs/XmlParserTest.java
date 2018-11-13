package com.cross.beaglesightlibs;

import org.robolectric.RobolectricTestRunner;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class XmlParserTest {
    @Test
    public void ParseTarget() throws ParserConfigurationException, SAXException, IOException {
        Target target = new Target();
        target.setName(randomString());
        target.setBuiltin(false);
        target.setId(randomString());

        target.setTargetLocation(randomLocation(target.getId()));

        List<LocationDescription> positionList = new ArrayList<>();
        for (int i = 0; i < 5; i++)
        {
            positionList.add(randomLocation(target.getId()));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XmlParser.serialiseTarget(outputStream, target, positionList);

        String string = outputStream.toString();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(string.getBytes());

        List<LocationDescription> resultPositionList = new ArrayList<>();
        List<Target> targetList = new ArrayList<>();
        XmlParser.parseTargetXML(inputStream, targetList, resultPositionList);

        assertEquals(1, targetList.size());
        assertEquals(target, targetList.get(0));

        assertEquals(positionList.size(), resultPositionList.size());

        for (LocationDescription expectedLocation: positionList)
        {
            assertTrue(resultPositionList.contains(expectedLocation));
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
        return (float)Math.random();
    }

    private double randomDouble() {
        return Math.random();
    }
}