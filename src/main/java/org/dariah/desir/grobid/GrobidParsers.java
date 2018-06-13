package org.dariah.desir.grobid;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;

public class GrobidParsers {

    public void parseDocument(InputStream is) {
        XPath xPath = XPathFactory.newInstance().newXPath();

        Document teiDoc = null;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setValidating(false);
            //docFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docFactory.newDocumentBuilder();
                teiDoc = docBuilder.parse(is);
            } catch (Exception e) {
                //TODO: log something 
            }
            teiStream.close();

            Publication pub = new Publication();

            Element teiHeader = (Element) xPath.compile(TeiPaths.MetadataElement).evaluate(teiDoc, XPathConstants.NODE);
            NodeList authorsFromfulltextTeiHeader = (NodeList) xPath.compile(TeiPaths.FulltextTeiHeaderAuthors).evaluate(teiDoc, XPathConstants.NODESET);

            Element title = null;
            if (teiHeader.getElementsByTagName("title") != null) {
                title = (Element) teiHeader.getElementsByTagName("title").item(0);
                if (title != null) {
                    pub.setDoc_title(title.getTextContent().trim());
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
            logger.info("Extracting :" + biblioObject.getRepositoryDocId());
            if (authors.getLength() > 30) {
                throw new NumberOfCoAuthorsExceededException("Number of authors exceed 30 co-authors for this publication.");
            }

            fr.inria.anhalytics.commons.entities.Document doc = new fr.inria.anhalytics.commons.entities.Document(biblioObject.getAnhalyticsId(), biblioObject.getRepositoryDocVersion(), new ArrayList<Document_Identifier>());

            processIdentifiers(ids, doc, biblioObject.getRepositoryDocId());
            dd.create(doc);

            pub.setDocument(doc);
            // for some pub types we just keep the submission date.
            pub.setDate_eletronic(submission_date.getTextContent());
            pub.setDate_printed(Utilities.parseStringDate(submission_date.getTextContent()));
            pub.setType(type.getTextContent());
            pub.setLanguage(language.getTextContent());
            processMonogr(monogr, pub);

            pd.create(pub);
            processPersons(authors, "author", pub, teiDoc, authorsFromfulltextTeiHeader);
            processPersons(editors, "editor", pub, teiDoc, authorsFromfulltextTeiHeader);

            logger.info("#################################################################");


            if (teiDoc != null) {
                String generatedTeiString = Utilities.toString(teiDoc);
                mm.insertTEIcorpus(generatedTeiString, biblioObject.getAnhalyticsId());
            }
        }

    }

}
