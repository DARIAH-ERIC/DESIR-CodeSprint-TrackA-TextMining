package org.dariah.desir.grobid;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;

public class GrobidParsers {

    public void parseDocument(InputStream is) {
        XPath xPath = XPathFactory.newInstance().newXPath();

        Document teiDoc = null;

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            teiDoc = docBuilder.parse(is);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }


        String title = "no title";

        try {

            Element teiHeader = (Element) xPath.compile(TeiPaths.MetadataElement).evaluate(teiDoc, XPathConstants.NODE);
            NodeList authorsFromfulltextTeiHeader = (NodeList) xPath.compile(TeiPaths.FulltextTeiHeaderAuthors).evaluate(teiDoc, XPathConstants.NODESET);

            Element titleElement = null;
            if (teiHeader.getElementsByTagName("title") != null) {
                titleElement = (Element) teiHeader.getElementsByTagName("title").item(0);
                if (titleElement != null) {
                    title = titleElement.getTextContent().trim();
                }
            }
            Node language = (Node) xPath.compile(TeiPaths.LanguageElement).evaluate(teiDoc, XPathConstants.NODE);
            Node type = (Node) xPath.compile(TeiPaths.TypologyElement).evaluate(teiDoc, XPathConstants.NODE);
            Node submission_date = (Node) xPath.compile(TeiPaths.SubmissionDateElement).evaluate(teiDoc, XPathConstants.NODE);
            Node domain = (Node) xPath.compile(TeiPaths.DomainElement).evaluate(teiDoc, XPathConstants.NODE);
            //more than one domain / article
            NodeList editors = teiHeader.getElementsByTagName("editor");
            NodeList authors = teiHeader.getElementsByTagName("author");
            Element monogr = (Element) xPath.compile(TeiPaths.MonogrElement).evaluate(teiDoc, XPathConstants.NODE);
            NodeList ids = (NodeList) xPath.compile(TeiPaths.IdnoElement).evaluate(teiDoc, XPathConstants.NODESET);


            System.out.println(title);
            // for some pub types we just keep the submission date.
            if (submission_date != null) {
                System.out.println("Submission date: " + submission_date.getTextContent());
                System.out.println("Printed date " + submission_date.getTextContent());
            }
            if (type != null) {
                System.out.println("type: " + type.getTextContent());
            }

            if (language != null) {
                System.out.println("Language: " + language.getTextContent());
            }
//        processMonogr(monogr, pub);

//        processPersons(authors, "author", pub, teiDoc, authorsFromfulltextTeiHeader);
//        processPersons(editors, "editor", pub, teiDoc, authorsFromfulltextTeiHeader);


        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

    }

}
