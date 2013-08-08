package com.thoughtworks.plugins.hours_lost;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: Thoughtworker
 * Date: 8/8/13
 * Time: 10:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class XMLLoader {

    public static Document getDocument(File buildXml) throws IOException, ParserConfigurationException, SAXException {
        BufferedReader br = new BufferedReader(new FileReader(buildXml));
        String line;
        StringBuilder sb = new StringBuilder();
        while((line=br.readLine())!= null){
            sb.append(line.trim());
        }
        String thing = sb.toString();
        thing.replaceAll("[^\\x20-\\x7e]", "");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(thing));
        return builder.parse(is);
    }


}
