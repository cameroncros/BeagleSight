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

        List<Target> targets = new ArrayList<>();

        Target target = new Target();
        target.setName(randomString());
        target.setBuiltin(false);
        target.setId(randomString());

        target.setTargetLocation(randomLocation(target.getId()));

        List<LocationDescription> positionList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            positionList.add(randomLocation(target.getId()));
        }
        target.setShootLocations(positionList);

        Target target2 = new Target();
        target2.setName(randomString());
        target2.setBuiltin(true);
        target2.setId(randomString());

        target2.setTargetLocation(randomLocation(target.getId()));

        targets.add(target);
        targets.add(target2);


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
    public void ParseSingleBowConfig() throws IOException, ParserConfigurationException, SAXException {
        BowConfig bowConfig = new BowConfig();
        String bowID = randomString();
        bowConfig.setId(bowID);
        bowConfig.setDescription(randomString());
        bowConfig.setName(randomString());
        List<PositionPair> pairs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PositionPair pair = new PositionPair();
            pair.setDistance(randomFloat());
            pair.setPosition(randomFloat());
            pair.setBowId(bowID);
            pair.setId(randomString());
        }
        bowConfig.setPositionArray(pairs);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XmlParser.serialiseSingleBowConfig(outputStream, bowConfig);

        String string = outputStream.toString();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(string.getBytes());

        BowConfig bowConfig1 = XmlParser.parseSingleBowConfigXML(inputStream);

        assertEquals(bowConfig, bowConfig1);
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