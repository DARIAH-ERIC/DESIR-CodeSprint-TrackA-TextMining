package org.dariah.desir.secondeCodeSprint;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TextExtractor {

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

        private static HashMap<Integer, acknoledgmentLabel> enumById = new HashMap<>();

        static {
            Arrays.stream(values()).forEach(e -> enumById.put(e.getId(), e));
        }

        public static acknoledgmentLabel getById(int id) {
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

    // read json file
    public void readAnnotatedFile(String file) {
        String outputTrainingFile = "data/xml/acknowledgementsTraining.xml";
        JSONParser parser = new JSONParser();
        ArrayList<String> listText = new ArrayList<>();
        try {
            Object object = parser.parse(new FileReader(file));
            JSONArray textArray = (JSONArray) object;
            HashMap<Integer, ArrayList<Acknowledgment>> MapOfAcknowledgment = new HashMap<>();

            for (Object obj : textArray) {
                JSONObject textObject = (JSONObject) obj;

                int idText = Integer.parseInt(String.valueOf(textObject.get("id")));

                String text = (String) textObject.get("text");

                // get annotations tag
                JSONArray annotations = (JSONArray) textObject.get("annotations");

                if (!annotations.isEmpty()) {
                    ArrayList<Acknowledgment> listOfAcknowledgment = new ArrayList<>();

                    // iteratate through annotations
                    for (Object annotation : annotations) {
                        Acknowledgment acknowledgment = new Acknowledgment();

                        JSONObject objectLabel = (JSONObject) annotation;

                        // get label number
                        int label = Integer.parseInt(String.valueOf(objectLabel.get("label")));
                        // get label text
                        String textOfLabel = String.valueOf(acknoledgmentLabel.getById(label));
                        System.out.println("Text of label : " + textOfLabel);
                        // get offsets
                        int start_offset = Integer.parseInt(String.valueOf(objectLabel.get("start_offset")));
                        int end_offset = Integer.parseInt(String.valueOf(objectLabel.get("end_offset")));
                        String subText = text.substring(start_offset, end_offset);
                        acknowledgment.setLabel(textOfLabel);
                        acknowledgment.setText(subText);

                        listOfAcknowledgment.add(acknowledgment);
                    }
                    MapOfAcknowledgment.put(idText, listOfAcknowledgment);
                }
            }
            //createXML(MapOfAcknowledgment, outputTrainingFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void createXML(HashMap<Integer, ArrayList<Acknowledgment>> listOfData, String file) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            // root element
            Element root = document.createElement("acknowledgments");
            document.appendChild(root);

            for (Map.Entry<Integer, ArrayList<Acknowledgment>> entry : listOfData.entrySet()) {
                int key = entry.getKey();
                ArrayList<Acknowledgment> acknowledgments = entry.getValue();

                // acknowledgment element
                Element acknowledgment = document.createElement("p");
                root.appendChild(acknowledgment);

                for (Acknowledgment ack : acknowledgments) {
                    String label = ack.getLabel();
                    String text = ack.getText();
                    Element labelElement = document.createElement(label);
                    labelElement.appendChild(document.createTextNode(text));
                    acknowledgment.appendChild(labelElement);
                }
            }

            // create the xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(file));
            transformer.transform(domSource, streamResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        String inputFile = "data/secondCodeSprint/json/acknowledgement/acknowledgementAnnotatedLabelInNumber.json";

        TextExtractor textExtractor = new TextExtractor();

        // read a Json file
        textExtractor.readAnnotatedFile(inputFile);

    }
}
