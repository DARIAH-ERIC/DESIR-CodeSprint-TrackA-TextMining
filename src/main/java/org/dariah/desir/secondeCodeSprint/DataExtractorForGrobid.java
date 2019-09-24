package org.dariah.desir.secondeCodeSprint;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DataExtractorForGrobid {
    public enum acknoledgmentLabel {
        unknown(0, "unknown"),
        educationalInstitution(2, "educationalInstitution"),
        fundingAgency(3, "fundingAgency"),
        grantNumber(4, "grantNumber"),
        individual(5, "individual"),
        projectName(6, "projectName"),
        researchInstitution(7, "researchInstitution"),
        otherInstitution(8, "otherInstitution"),
        affiliation(9, "affiliation"),
        grantName(10, "grantName");

        private static HashMap<Integer, DataExtractorForGrobid.acknoledgmentLabel> enumById = new HashMap<>();

        static {
            Arrays.stream(values()).forEach(e -> enumById.put(e.getId(), e));
        }

        public static DataExtractorForGrobid.acknoledgmentLabel getById(int id) {
            return enumById.getOrDefault(id, unknown);
        }

        private int id;
        private String label;

        private acknoledgmentLabel(int id, String label) {
            this.id = id;
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public int getId() {
            return id;
        }
    }

    /* read json file source
    errors might occur if the source file has errors in the offset positions, like larger offset precedes smaller offset:
    {
        "label": 3,
            "start_offset": 236,
            "end_offset": 268,
            "user": 1
    },
    {
        "label": 7,
            "start_offset": 187,
            "end_offset": 219,
            "user": 1
    }
    The solution: the source file needs to be fixed manually first
    */
    public void readJsonFileSource(String file) throws IOException {
        String pathOutputFile = "data/secondCodeSprint/xml/acknowledgementsAnnotatedFormattedForGrobid.xml";

        StringBuilder sb = new StringBuilder();
        ArrayList<RawText> rawTexts = new ArrayList<>();
        ArrayList<Acknowledgment> acknowledgments = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document;

        JSONParser parser = new JSONParser();
        try {
            builder = factory.newDocumentBuilder();

            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");

            // root element
            sb.append("<acknowledgments>\n");

            Object object = parser.parse(new FileReader(file));
            JSONArray textArray = (JSONArray) object;

            for (Object obj : textArray) {
                RawText rawText = new RawText();
                JSONObject textObject = (JSONObject) obj;

                // get the raw text
                String text = (String) textObject.get("text");

                // get the meta information : identifier-title
                JSONObject meta = (JSONObject) textObject.get("meta");
                String identifier = (String) meta.get("identifier");
                String title = (String) meta.get("title");

                // fill the object of raw text
                rawText.setIdentifier(identifier);
                rawText.setTitle(title);
                rawText.setText(text);

                rawTexts.add(rawText);

                // get annotations tag
                JSONArray annotations = (JSONArray) textObject.get("annotations");
                if (!annotations.isEmpty()) {

                    // acknowledgment element
                    sb.append("<acknowledgment>");

                    String subText = "";

                    ArrayList<Acknowledgment> acknowledgmentPerAnnotation = new ArrayList<>();

                    // iteratate through annotations
                    for (Object annotation : annotations) {
                        Acknowledgment acknowledgment = new Acknowledgment();

                        JSONObject objectLabel = (JSONObject) annotation;

                        // get label number
                        int label = Integer.parseInt(String.valueOf(objectLabel.get("label")));
                        // get label text
                        String textOfLabel = String.valueOf(DataExtractorForGrobid.acknoledgmentLabel.getById(label));
                        // get offsets
                        int start_offset = Integer.parseInt(String.valueOf(objectLabel.get("start_offset")));
                        int end_offset = Integer.parseInt(String.valueOf(objectLabel.get("end_offset")));
                        subText = text.substring(start_offset, end_offset);

                        // fill the list of acknowledgment object
                        acknowledgment.setLabel(textOfLabel);
                        acknowledgment.setText(subText);
                        acknowledgment.setStart_offset(start_offset);
                        acknowledgment.setEnd_offset(end_offset);

                        // the list of acknowledgment for all json object
                        acknowledgments.add(acknowledgment);

                        // the list of acknowledgment per annotation json object
                        acknowledgmentPerAnnotation.add(acknowledgment);
                    }
                    int beginPointer = 0, endPointer = 0;
                    String beginText = "", combinedText = "";
                    for (int i = 0; i < acknowledgmentPerAnnotation.size(); i++) {
                        if (i == 0) {
                            beginPointer = 0;
                        }

                        // stubstring and escaping special character when generating an XML file
                        beginText =  StringEscapeUtils.escapeXml(text.substring(beginPointer, acknowledgmentPerAnnotation.get(i).getStart_offset()));

                        //System.out.println("offset rest : " + beginPointer + " - " + acknowledgmentPerAnnotation.get(i).getStart_offset());
                        //System.out.println("offset sub text : " + acknowledgmentPerAnnotation.get(i).getStart_offset() + " - " + acknowledgmentPerAnnotation.get(i).getEnd_offset());
                        combinedText += beginText + "<" + StringEscapeUtils.escapeXml(acknowledgmentPerAnnotation.get(i).getLabel()) + ">" + StringEscapeUtils.escapeXml(acknowledgmentPerAnnotation.get(i).getText()) + "</" + StringEscapeUtils.escapeXml(acknowledgmentPerAnnotation.get(i).getLabel()) + ">" ;

                        beginPointer = (acknowledgmentPerAnnotation.get(i).getEnd_offset());
                        endPointer = acknowledgmentPerAnnotation.get(i).getEnd_offset();
                    }

                    //System.out.println("Text : " + combinedText + text.substring(endPointer));
                    combinedText += StringEscapeUtils.escapeXml(text.substring(endPointer));

                    sb.append(combinedText);
                    // close every paragraph
                    sb.append("</acknowledgment>\n");
                }
            }
            // close the document
            sb.append("</acknowledgments>\n");

            // create a file xml
            document = builder.parse(new InputSource(new StringReader(sb.toString())));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);

            File outputFile = new File(pathOutputFile);
            StreamResult streamResult = new StreamResult(outputFile);
            transformer.transform(domSource, streamResult);

            System.out.println("File is saved in " +outputFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        String inputFile = "data/secondCodeSprint/json/annotated/acknowledgementAnnotatedLabelInNumber.json";

        DataExtractorForGrobid dataExtractorForDelft = new DataExtractorForGrobid();
        dataExtractorForDelft.readJsonFileSource(inputFile);

    }
}
