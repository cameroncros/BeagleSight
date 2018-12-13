package com.cross.beaglesightlibs;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class BowConfigTest {
    BowConfig bowConfig;

    @Before
    public void setup()
    {
        bowConfig = new BowConfig();
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
    }

    @Test
    public void BowConfigXML() throws IOException, ParserConfigurationException, SAXException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XmlParser.serialiseSingleBowConfig(outputStream, bowConfig);

        String string = outputStream.toString();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(string.getBytes());

        BowConfig bowConfig1 = XmlParser.parseSingleBowConfigXML(inputStream);

        assertEquals(bowConfig, bowConfig1);
    }

    @Test
    public void BowConfigParcel() {
        Parcel parcel = Parcel.obtain();
        bowConfig.writeToParcel(parcel, bowConfig.describeContents());
        parcel.setDataPosition(0);

        BowConfig bowConfigOut = BowConfig.CREATOR.createFromParcel(parcel);

        assertEquals(bowConfig, bowConfigOut);
    }

    private String randomString() {
        return UUID.randomUUID().toString();
    }

    private float randomFloat() {
        return (float) Math.random();
    }
}
