package com.cross.beaglesightlibs;

import android.util.Log;
import android.util.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        private static final String TARGET = "target";
    }

    public static void parseTargetXML(InputStream stream,
                                      List<Target> targets,
                                      List<LocationDescription> locations)
            throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(stream);
        Document document = db.parse(inputSource);

        NodeList nodelist = document.getElementsByTagName(XML_TAGS.TARGET);

        for (int i = 0; i < nodelist.getLength(); i++) {
            Target target = parseTarget(nodelist.item(i), locations);
            targets.add(target);
        }
    }

    private static Target parseTarget(Node node, List<LocationDescription> locations) {
        Target target = new Target();

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

    public static void serialiseTarget(OutputStream stream, Target target, List<LocationDescription> shootPositions) throws IOException {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(stream, "UTF-8");
        serializer.startDocument(null, Boolean.TRUE);
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        serializer.startTag(null, XML_TAGS.TARGET);
        serializer.startTag(null, XML_TAGS.ID);
        serializer.text(target.getId());
        serializer.endTag(null, XML_TAGS.ID);
        serializer.startTag(null, XML_TAGS.NAME);
        serializer.text(target.getName());
        serializer.endTag(null, XML_TAGS.NAME);
        serializer.startTag(null, XML_TAGS.BUILTIN);
        serializer.text(target.getName());
        serializer.endTag(null, XML_TAGS.BUILTIN);


        serializer.startTag(null, XML_TAGS.LOCATION);
        serialiseLocation(serializer, target.getTargetLocation());
        serializer.endTag(null, XML_TAGS.LOCATION);

        for (LocationDescription locationDescription : shootPositions) {
            serializer.startTag(null, XML_TAGS.SHOOT_POSITION);
            serialiseLocation(serializer, locationDescription);
            serializer.endTag(null, XML_TAGS.SHOOT_POSITION);
        }

        serializer.endTag(null, XML_TAGS.TARGET);
        serializer.endDocument();
        serializer.flush();
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
}