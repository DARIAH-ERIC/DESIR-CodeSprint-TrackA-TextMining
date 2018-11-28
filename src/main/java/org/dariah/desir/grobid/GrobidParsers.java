package org.dariah.desir.grobid;

import org.dariah.desir.data.DisambiguatedAuthor;
import org.dariah.desir.data.ResolvedCitation;
import org.dariah.desir.service.EntityFishingService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
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
        String author = "";

        try {

            Element teiHeader = (Element) xPath.compile(TeiPaths.MetadataElement).evaluate(teiDoc, XPathConstants.NODE);
            NodeList authorsFromfulltextTeiHeader = (NodeList) xPath.compile(TeiPaths.FulltextTeiHeaderAuthors).evaluate(teiDoc, XPathConstants.NODESET);


            // get the title
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

            // get the authors
            for (int i = 0; i < authors.getLength(); i++) {
                Element authorElement = null;
                if (teiHeader.getElementsByTagName("persName") != null) {
                    authorElement = (Element) teiHeader.getElementsByTagName("persName").item(0);
                    if (authorElement != null) {
                        author = authorElement.getTextContent().trim();
                        //System.out.println(author);
                    }
                }

            }

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

    public List<DisambiguatedAuthor> processAuthorNames(InputStream is) {
        XPath xPath = XPathFactory.newInstance().newXPath();

        List<DisambiguatedAuthor> output = new ArrayList<>();

        Document teiDoc = null;

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            teiDoc = docBuilder.parse(is);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            NodeList authors = (NodeList) xPath.compile(TeiPaths.FulltextTeiHeaderAuthors).evaluate(teiDoc, XPathConstants.NODESET);

            // get the authors
            for (int i = 0; i < authors.getLength(); i++) {
                DisambiguatedAuthor authorOutput = new DisambiguatedAuthor();

                Node author = authors.item(i);
                NodeList child = author.getChildNodes();
                Node persName = (Node) xPath.compile("persName").evaluate(child, XPathConstants.NODE);
                if (persName != null && persName.getNodeType() == Node.ELEMENT_NODE){
                    Node nodeCoordinates = persName.getAttributes().getNamedItem("coords");
                    if (nodeCoordinates != null) {
                        authorOutput.setCoordinates(nodeCoordinates.getTextContent());
                    }
                    Node idno = (Node) xPath.compile("idno").evaluate(persName, XPathConstants.NODE);

                    if (idno != null && idno.getNodeType() == Node.ELEMENT_NODE) {
                        Element idElement = (Element)idno;
                        String id = String.valueOf(idElement.getTextContent());
                        authorOutput.setId(id);
                        String type = idElement.getAttribute("type");
                        authorOutput.setIdType(type);
                        String cert = idElement.getAttribute("cert");
                        authorOutput.setConfidence(cert);

                        output.add(authorOutput);
                    }
            }
        }


        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return output;

    }

    public List<ResolvedCitation> processCitations(InputStream is) {
        XPath xPath = XPathFactory.newInstance().newXPath();

        List<ResolvedCitation> resolvedCitations = new ArrayList<>();

        Document teiDoc = null;

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
        //docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            teiDoc = docBuilder.parse(is);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            NodeList references = (NodeList) xPath.compile("/TEI/text/back/div/listBibl/biblStruct").evaluate(teiDoc, XPathConstants.NODESET);

            for (int i = 0; i < references.getLength(); i++) {
                Node reference = references.item(i);

                ResolvedCitation citation = new ResolvedCitation();
                if (reference.getNodeType() == Node.ELEMENT_NODE) {
                    Element referenceElement = (Element)reference;
                    String coordinates = referenceElement.getAttribute("coords");
                    citation.setCoordinates(coordinates);

                    Node titleNode = ((Node) xPath.compile("analytic/title").evaluate(reference, XPathConstants.NODE));
                    if (titleNode != null && titleNode.getNodeType() == Node.ELEMENT_NODE) {
                        String title = titleNode.getTextContent();
                        citation.setTitle(title);
                    }


                    Node doiNode = ((Node) xPath.compile("analytic/idno[@type=\"doi\"]").evaluate(reference, XPathConstants.NODE));
                    if (doiNode != null  && doiNode.getNodeType() == Node.ELEMENT_NODE) {
                        String doi = doiNode.getTextContent();
                        citation.setDoi(doi);
                        try {
                            citation.setWikidataID(new EntityFishingService().lookupWikidataByDoi(doi));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    resolvedCitations.add(citation);
                }
            }


        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return resolvedCitations;

    }

    public String processAbstract(InputStream is) {
        String abstractContent = null;
        XPath xPath = XPathFactory.newInstance().newXPath();

        Document teiDoc = null;

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
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

        try {
            Element teiHeader = (Element) xPath.compile(TeiPaths.MetadataElement).evaluate(teiDoc, XPathConstants.NODE);

            // get the abstract content
            Element abstractElement = null;
            if (teiHeader.getElementsByTagName("abstract") != null) {
                abstractElement = (Element) teiHeader.getElementsByTagName("abstract").item(0);
                if (abstractElement != null) {
                    abstractContent = abstractElement.getTextContent().trim();
                }
            }

            return abstractContent;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return abstractContent;
    }

    public Map<String, Double> processKeyword(InputStream is) {

        Map<String, Double> listOfKeywords = new HashMap<>();

        XPath xPath = XPathFactory.newInstance().newXPath();

        Document teiDoc = null;

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setValidating(false);
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

        try {

            Element teiHeader = (Element) xPath.compile(TeiPaths.MetadataElement).evaluate(teiDoc, XPathConstants.NODE);

            // get the abstract content
            Element keywordKeyElement = null;
            if (teiHeader.getElementsByTagName("keywords") != null) {
                keywordKeyElement = (Element) teiHeader.getElementsByTagName("keywords").item(0);
                NodeList nodeListOfKeyword = keywordKeyElement.getElementsByTagName("term");
                for (int i = 0; i < nodeListOfKeyword.getLength(); i++) {
                    Node nodeOfKeyword = nodeListOfKeyword.item(i);
                    if (nodeOfKeyword.getNodeType() == Node.ELEMENT_NODE) {
                        String keyword = nodeOfKeyword.getTextContent();
                        listOfKeywords.put(keyword, 0.0); // the score of the term can be changed later on
                    }
                }
            }
            return listOfKeywords;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listOfKeywords;
    }

    public static void main(String[] args) throws Exception {
        String fileInput = "11_Anne FOCKE_The influence of catch trials on the consolidation of motor memory in force field adaptation tasks.pdf.tei.xml";
        GrobidParsers grobidParsers = new GrobidParsers();
        ClassPathResource resource = new ClassPathResource(fileInput);
        InputStream inputStream = resource.getInputStream();
        grobidParsers.processKeyword(inputStream);
    }

}
