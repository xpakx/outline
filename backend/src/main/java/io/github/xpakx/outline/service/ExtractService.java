package io.github.xpakx.outline.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class ExtractService {
    public Document parse(String input) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(input);
    }

    public String extractTitle(Document doc) {
        final int MIN_LENGTH = 2;

        Element rootElem = doc.getDocumentElement();
        NodeList titleElems = rootElem.getElementsByTagName("title");

        //no title elem
        if(titleElems.getLength() == 0) {

        }

        String titleContent = titleElems.item(0).getTextContent().strip();

        NodeList h1Elems = rootElem.getElementsByTagName("h1");
        List<String> h1Candidates = new ArrayList<>();

        for(int i = 0; i < h1Elems.getLength(); i++) {
            String h1Title = h1Elems.item(i).getTextContent().strip();
            if(h1Title.equalsIgnoreCase(titleContent)) {
                return titleContent;
            }
            if(h1Title.length() > MIN_LENGTH && titleContent.contains(h1Title)) {
                h1Candidates.add(h1Title);
            }
        }

        if(h1Candidates.size() > 0) {
            h1Candidates.sort(Comparator.comparingInt(String::length).reversed());
            return h1Candidates.get(0);
        }


        return titleContent;
    }
}
