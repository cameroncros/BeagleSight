package com.cross.beaglesightlibs;

import android.util.Log;
import android.util.Xml;

import com.cross.beaglesightlibs.exceptions.InvalidNumberFormatException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XmlParser {
    private static class XML_TAGS {
        private static final String ID = "id";
        private static final String NAME = "name";
        private static final String TARGET_ID = "target_id";
        private static final String DESCRIPTION = "description";
        private static final String LONGITUDE = "longitude";
        private static final String LATITUDE = "latitude";
        private static final String ALTITUDE = "altitude";
        private static final String LAT_LNG_ACCURACY = "lat_lng_accuracy";
        private static final String ALTITUDE_ACCURACY = "altitude_accuracy";
        private static final String BUILTIN = "builtin";
        private static final String LOCATION = "location";
        private static final String SHOOT_POSITION = "shoot_position";
        private static final String TARGETS = "targets";
        private static final String TARGET = "target";

        private static final String BOWS = "bows";
        private static final String BOW = "bow";
        private static final String POSITIONPAIR = "positionpair";
        private static final String BOW_ID = "bowid";
        private static final String DISTANCE = "distance";
        private static final String POSITION = "position";
    }

    public static List<Target> parseTargetsXML(InputStream stream)
            throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(stream);
        Document document = db.parse(inputSource);

        List<Target> targets = new ArrayList<>();

        NodeList nodelist = document.getElementsByTagName(XML_TAGS.TARGETS);
        NodeList targetNodes = nodelist.item(0).getChildNodes();

        for (int i = 0; i < targetNodes.getLength(); i++) {
            Node targetNode = targetNodes.item(i);
            if (targetNode.getNodeName().equals(XML_TAGS.TARGET)) {
                Target target = parseTarget(targetNode);
                targets.add(target);
            }
        }
        return targets;
    }

    private static Target parseTarget(Node node) {
        Target target = new Target();
        List<LocationDescription> locations = new ArrayList<>();

        NodeList children = node.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            Node nd = children.item(j);
            switch (nd.getNodeName()) {
                case XML_TAGS.ID:
                    target.setId(nd.getTextContent());
                    break;
                case XML_TAGS.NAME:
                    target.setName(nd.getTextContent());
                    break;
                case XML_TAGS.BUILTIN:
                    target.setBuiltin(Boolean.parseBoolean(nd.getTextContent()));
                    break;
                case XML_TAGS.LOCATION:
                    target.setTargetLocation(parseLocation(nd));
                    break;
                case XML_TAGS.SHOOT_POSITION:
                    LocationDescription locationDescription = parseLocation(nd);
                    locations.add(locationDescription);
                    break;
            }
        }
        target.setShootLocations(locations);
        return target;
    }

    private static LocationDescription parseLocation(Node node) {
        LocationDescription locDesc = new LocationDescription();
        NodeList children = node.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            Node nd = children.item(j);
            switch (nd.getNodeName()) {
                case XML_TAGS.ID:
                    locDesc.setLocationId(nd.getTextContent());
                    break;
                case XML_TAGS.TARGET_ID:
                    locDesc.setTargetId(nd.getTextContent());
                    break;
                case XML_TAGS.DESCRIPTION:
                    locDesc.setDescription(nd.getTextContent());
                    break;
                case XML_TAGS.LATITUDE:
                    locDesc.setLatitude(Double.parseDouble(nd.getTextContent()));
                    break;
                case XML_TAGS.LONGITUDE:
                    locDesc.setLongitude(Double.parseDouble(nd.getTextContent()));
                    break;
                case XML_TAGS.ALTITUDE:
                    locDesc.setAltitude(Double.parseDouble(nd.getTextContent()));
                    break;
                case XML_TAGS.LAT_LNG_ACCURACY:
                    locDesc.setLatlng_accuracy(Float.parseFloat(nd.getTextContent()));
                    break;
                case XML_TAGS.ALTITUDE_ACCURACY:
                    locDesc.setAltitude_accuracy(Float.parseFloat(nd.getTextContent()));
                    break;
            }
        }
        return locDesc;
    }

    public static void serialiseTargets(OutputStream stream, List<Target> targets) throws IOException {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(stream, "UTF-8");
        serializer.startDocument(null, Boolean.TRUE);
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        serializer.startTag(null, XML_TAGS.TARGETS);
        for (Target target : targets) {
            serialiseTarget(serializer, target);
        }
        serializer.endTag(null, XML_TAGS.TARGETS);
        serializer.endDocument();
        serializer.flush();
    }

    private static void serialiseTarget(XmlSerializer serializer, Target target) throws IOException {
        serializer.startTag(null, XML_TAGS.TARGET);
        serializer.startTag(null, XML_TAGS.ID);
        serializer.text(target.getId());
        serializer.endTag(null, XML_TAGS.ID);
        serializer.startTag(null, XML_TAGS.NAME);
        serializer.text(target.getName());
        serializer.endTag(null, XML_TAGS.NAME);
        serializer.startTag(null, XML_TAGS.BUILTIN);
        serializer.text(Boolean.toString(target.isBuiltin()));
        serializer.endTag(null, XML_TAGS.BUILTIN);


        serializer.startTag(null, XML_TAGS.LOCATION);
        serialiseLocation(serializer, target.getTargetLocation());
        serializer.endTag(null, XML_TAGS.LOCATION);

        for (LocationDescription locationDescription : target.getShootLocations()) {
            serializer.startTag(null, XML_TAGS.SHOOT_POSITION);
            serialiseLocation(serializer, locationDescription);
            serializer.endTag(null, XML_TAGS.SHOOT_POSITION);
        }

        serializer.endTag(null, XML_TAGS.TARGET);
    }

    private static void serialiseLocation(XmlSerializer serializer, LocationDescription locationDescription) throws IOException {
        serializer.startTag(null, XML_TAGS.ID);
        serializer.text(locationDescription.getLocationId());
        serializer.endTag(null, XML_TAGS.ID);

        serializer.startTag(null, XML_TAGS.TARGET_ID);
        serializer.text(locationDescription.getTargetId());
        serializer.endTag(null, XML_TAGS.TARGET_ID);

        serializer.startTag(null, XML_TAGS.DESCRIPTION);
        serializer.text(locationDescription.getDescription());
        serializer.endTag(null, XML_TAGS.DESCRIPTION);

        serializer.startTag(null, XML_TAGS.LATITUDE);
        serializer.text(Double.toString(locationDescription.getLatitude()));
        serializer.endTag(null, XML_TAGS.LATITUDE);

        serializer.startTag(null, XML_TAGS.LONGITUDE);
        serializer.text(Double.toString(locationDescription.getLongitude()));
        serializer.endTag(null, XML_TAGS.LONGITUDE);

        serializer.startTag(null, XML_TAGS.ALTITUDE);
        serializer.text(Double.toString(locationDescription.getAltitude()));
        serializer.endTag(null, XML_TAGS.ALTITUDE);

        serializer.startTag(null, XML_TAGS.LAT_LNG_ACCURACY);
        serializer.text(Float.toString(locationDescription.getLatlng_accuracy()));
        serializer.endTag(null, XML_TAGS.LAT_LNG_ACCURACY);

        serializer.startTag(null, XML_TAGS.ALTITUDE_ACCURACY);
        serializer.text(Float.toString(locationDescription.getAltitude_accuracy()));
        serializer.endTag(null, XML_TAGS.ALTITUDE_ACCURACY);
    }


    public static void serialiseBowConfigs(OutputStream stream, List<BowConfig> bowConfigs) throws IOException {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(stream, "UTF-8");
        serializer.startDocument(null, Boolean.TRUE);
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        serializer.startTag(null, XML_TAGS.BOWS);
        for (BowConfig bowConfig : bowConfigs) {
            serialiseBowConfig(serializer, bowConfig);
        }
        serializer.endTag(null, XML_TAGS.BOWS);
        serializer.endDocument();
        serializer.flush();
    }

    public static void serialiseSingleBowConfig(OutputStream stream, BowConfig bowConfig) throws IOException {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(stream, "UTF-8");
        serializer.startDocument(null, Boolean.TRUE);
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        serialiseBowConfig(serializer, bowConfig);

        serializer.endDocument();
        serializer.flush();
    }

    private static void serialiseBowConfig(XmlSerializer serializer, BowConfig bowConfig) throws IOException {
        serializer.startTag(null, XML_TAGS.BOW);
        serializer.startTag(null, XML_TAGS.ID);
        serializer.text(bowConfig.getId());
        serializer.endTag(null, XML_TAGS.ID);
        serializer.startTag(null, XML_TAGS.NAME);
        serializer.text(bowConfig.getName());
        serializer.endTag(null, XML_TAGS.NAME);
        serializer.startTag(null, XML_TAGS.DESCRIPTION);
        serializer.text(bowConfig.getDescription());
        serializer.endTag(null, XML_TAGS.DESCRIPTION);

        for (PositionPair pair : bowConfig.getPositionArray()) {
            serializer.startTag(null, XML_TAGS.POSITIONPAIR);
            serialisePositionPair(serializer, pair);
            serializer.endTag(null, XML_TAGS.POSITIONPAIR);
        }
        serializer.endTag(null, XML_TAGS.BOW);
    }

    private static void serialisePositionPair(XmlSerializer serializer, PositionPair pair) throws IOException {
        serializer.startTag(null, XML_TAGS.BOW_ID);
        serializer.text(pair.getBowId());
        serializer.endTag(null, XML_TAGS.BOW_ID);

        serializer.startTag(null, XML_TAGS.DISTANCE);
        serializer.text(Float.toString(pair.getDistance()));
        serializer.endTag(null, XML_TAGS.DISTANCE);

        serializer.startTag(null, XML_TAGS.POSITION);
        serializer.text(Float.toString(pair.getPosition()));
        serializer.endTag(null, XML_TAGS.POSITION);
    }


    public static List<BowConfig> parseBowConfigsXML(InputStream stream)
            throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(stream);
        Document document = db.parse(inputSource);

        List<BowConfig> bowConfigs = new ArrayList<>();

        NodeList nodelist = document.getElementsByTagName(XML_TAGS.BOWS);
        NodeList targetNodes = nodelist.item(0).getChildNodes();

        for (int i = 0; i < targetNodes.getLength(); i++) {
            Node bowNode = targetNodes.item(i);
            if (bowNode.getNodeName().equals(XML_TAGS.BOW)) {
                BowConfig bowConfig = parseBowConfig(bowNode);
                bowConfigs.add(bowConfig);
            }
        }
        return bowConfigs;
    }

    public static BowConfig parseSingleBowConfigXML(InputStream stream)
            throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(stream);
        Document document = db.parse(inputSource);

        NodeList nodelist = document.getElementsByTagName(XML_TAGS.BOW);
        return parseBowConfig(nodelist.item(0));
    }

    private static BowConfig parseBowConfig(Node node)
            throws IOException, ParserConfigurationException, SAXException {
        BowConfig bowConfig = new BowConfig();
        List<PositionPair> pairs = new ArrayList<>();

        NodeList children = node.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            Node nd = children.item(j);
            switch (nd.getNodeName()) {
                case XML_TAGS.ID:
                    bowConfig.setId(nd.getTextContent());
                    break;
                case XML_TAGS.NAME:
                    bowConfig.setName(nd.getTextContent());
                    break;
                case XML_TAGS.DESCRIPTION:
                    bowConfig.setDescription(nd.getTextContent());
                    break;
                case XML_TAGS.POSITION:
                    PositionPair pair = parsePositionPair(nd);
                    pairs.add(pair);
                    break;
            }
        }
        bowConfig.setPositionArray(pairs);
        return bowConfig;
    }

    private static PositionPair parsePositionPair(Node node) {
        PositionPair positionPair = new PositionPair();
        NodeList children = node.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            Node nd = children.item(j);
            switch (nd.getNodeName()) {
                case XML_TAGS.ID:
                    positionPair.setId(nd.getTextContent());
                    break;
                case XML_TAGS.BOW_ID:
                    positionPair.setBowId(nd.getTextContent());
                    break;
                case XML_TAGS.POSITION:
                    positionPair.setPosition(Float.parseFloat(nd.getTextContent()));
                    break;
                case XML_TAGS.DISTANCE:
                    positionPair.setDistance(Float.parseFloat(nd.getTextContent()));
                    break;
            }
        }
        return positionPair;
    }
}
