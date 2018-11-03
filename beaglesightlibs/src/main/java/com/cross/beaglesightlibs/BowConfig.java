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
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class BowConfig {
    private String id = "";
    private String name = "";
    private String description = "";
    private final List<PositionPair> positionArray = new ArrayList<>();
    PositionCalculator positionCalculator = null;
    
    private static class XML_TAGS {
        private static final String BOW = "bow";
        private static final String ID = "id";
        private static final String DESCRIPTION = "description";
        private static final String NAME = "name";
        private static final String POSITION = "position";
    }

    public BowConfig(String name, String description) {
        initPositionCalculator();
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
    }

    public BowConfig(InputStream stream) throws IOException, ParserConfigurationException, SAXException {
        initPositionCalculator();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = factory.newDocumentBuilder();
        InputSource inputSource = new InputSource(stream);
        Document document = db.parse(inputSource);

        NodeList nodelist = document.getElementsByTagName(XML_TAGS.BOW);
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node e = nodelist.item(i);
            NodeList children = e.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node nd = children.item(j);
                switch (nd.getNodeName()) {
                    case XML_TAGS.ID:
                        id = nd.getTextContent();
                        break;
                    case XML_TAGS.NAME:
                        name = nd.getTextContent();
                        break;
                    case XML_TAGS.DESCRIPTION:
                        description = nd.getTextContent();
                        break;
                    case XML_TAGS.POSITION:
                        String values = nd.getTextContent();
                        String parts[] = values.split(",");
                        try {
                            PositionPair pair = new PositionPair(parts[0], parts[1]);
                            addPosition(pair);
                        }
                        catch (InvalidNumberFormatException f)
                        {
                            // Do nothing, should never happen
                        }
                        break;
                }
            }
        }
    }

    public void save(OutputStream fileOS) {
        try {
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fileOS, "UTF-8");
            serializer.startDocument(null, Boolean.TRUE);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            serializer.startTag(null, XML_TAGS.BOW);
            serializer.startTag(null, XML_TAGS.ID);
            serializer.text(id);
            serializer.endTag(null, XML_TAGS.ID);
            serializer.startTag(null, XML_TAGS.NAME);
            serializer.text(name);
            serializer.endTag(null, XML_TAGS.NAME);
            serializer.startTag(null, XML_TAGS.DESCRIPTION);
            serializer.text(description);
            serializer.endTag(null, XML_TAGS.DESCRIPTION);

            for (PositionPair pair : positionArray) {
                serializer.startTag(null, XML_TAGS.POSITION);
                serializer.text(pair.toString());
                serializer.endTag(null, XML_TAGS.POSITION);
            }
            serializer.endTag(null, XML_TAGS.BOW);
            serializer.endDocument();
            serializer.flush();
            fileOS.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("Exception",e.toString());
        }

    }

    public void initPositionCalculator() {
        positionCalculator = new LineOfBestFitCalculator();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void addPosition(PositionPair pair)
    {
        positionArray.add(pair);
        positionCalculator.setPositions(positionArray);
    }

    public PositionCalculator getPositionCalculator() {
        return positionCalculator;
    }

    public List<PositionPair> getPositions() {
        return positionArray;
    }

    public void deletePosition(PositionPair selectedPair) {
        positionArray.remove(selectedPair);
    }

    @Override
    public String toString()
    {
        return String.format("ID: %s, Name: %s, Desc: %s", id, name, description);
    }
}