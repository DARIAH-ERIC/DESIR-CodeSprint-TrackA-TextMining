package org.dariah.desir.secondeCodeSprint;

import org.dkpro.statistics.agreement.unitizing.KrippendorffAlphaUnitizingAgreement;
import org.dkpro.statistics.agreement.unitizing.UnitizingAnnotationStudy;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DataExtractorForIAA {
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

        private static HashMap<Integer, DataExtractorForIAA.acknoledgmentLabel> enumById = new HashMap<>();

        static {
            Arrays.stream(values()).forEach(e -> enumById.put(e.getId(), e));
        }

        public static DataExtractorForIAA.acknoledgmentLabel getById(int id) {
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
    public List<Annotation> readJsonFile(String file, int idxFile) throws IOException {
        List<Annotation> annotationList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        ArrayList<RawText> rawTexts = new ArrayList<>();

        JSONParser parser = new JSONParser();
        try {
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

                    // iteratate through annotations
                    for (Object objAnnotation : annotations) {
                        Acknowledgment acknowledgment = new Acknowledgment();
                        Annotation annotation = new Annotation();

                        JSONObject objectLabel = (JSONObject) objAnnotation;

                        // get label number
                        int label = Integer.parseInt(String.valueOf(objectLabel.get("label")));
                        // get label text
                        String textOfLabel = String.valueOf(DataExtractorForIAA.acknoledgmentLabel.getById(label));
                        // get offsets
                        int start_offset = Integer.parseInt(String.valueOf(objectLabel.get("start_offset")));
                        int end_offset = Integer.parseInt(String.valueOf(objectLabel.get("end_offset")));
                        subText = text.substring(start_offset, end_offset);

                        // fill the list of annotation object
                        annotation.setCategory(textOfLabel);
                        annotation.setText(subText);
                        annotation.setAnnotatorIdx(idxFile);
                        annotation.setOffset(start_offset);
                        annotation.setLength(end_offset-start_offset);

                        // the list of acknowledgment for all json object
                        annotationList.add(annotation);

                    }
                }
            }
            /*for (Annotation annotation : annotationList){
                System.out.println(annotation.getAnnotatorIdx() + " -> " + annotation.getText() + "; ");
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }
        return annotationList;
    }

    public static void main(String[] args) throws IOException {

        String path = "data/secondCodeSprint/xml/IAA";
        Map<Integer, List<Annotation>> result = new HashMap<>();
        List<Annotation> annotationList = new ArrayList<>();
        IAA iaa = new IAA();
        DataExtractorForIAA dataExtractorForDelft = new DataExtractorForIAA();

        int idxFile=0;
        File file = new File(path);
        File[] files = file.listFiles();
        for(File f: files){
            idxFile++;
            annotationList = dataExtractorForDelft.readJsonFile(path+"/"+f.getName(),idxFile);
            result.put(idxFile, annotationList);
        }
        UnitizingAnnotationStudy unitizingStudy = new UnitizingAnnotationStudy(idxFile, annotationList.size()/idxFile);
        List<Annotation> combinedAnnotations = new ArrayList<>();

        for (Map.Entry<Integer, List<Annotation>> entry : result.entrySet()) {
            int idxAnnotator = entry.getKey();
            List<Annotation> annotations = entry.getValue();
            Annotation newAnnotation = null;
            for (Annotation annotation : annotations){
                newAnnotation = new Annotation();
                //unitizingStudy.addUnit(annotation.getOffset(),annotation.getLength(),annotation.getAnnotatorIdx(), annotation.getCategory());
                newAnnotation.setOffset(annotation.getOffset());
                newAnnotation.setLength(annotation.getLength());
                newAnnotation.setAnnotatorIdx(annotation.getAnnotatorIdx());
                newAnnotation.setCategory(annotation.getCategory());
                combinedAnnotations.add(newAnnotation);
            }
        }

        //System.out.println("Combined Annotation");
        for (int i=0;i<combinedAnnotations.size();i++){
            unitizingStudy.addUnit(combinedAnnotations.get(i).getOffset(),combinedAnnotations.get(i).getLength(),combinedAnnotations.get(i).getAnnotatorIdx(), combinedAnnotations.get(i).getCategory());
            //System.out.println(combinedAnnotations.get(i).getOffset()+";"+combinedAnnotations.get(i).getLength()+";"+combinedAnnotations.get(i).getAnnotatorIdx()+";"+combinedAnnotations.get(i).getCategory());
        }

        KrippendorffAlphaUnitizingAgreement alpha = new KrippendorffAlphaUnitizingAgreement(unitizingStudy);
        System.out.println("Agreement : " + alpha.calculateAgreement());
        System.out.println("Agreement of researchInstitution : " + alpha.calculateCategoryAgreement("researchInstitution"));
        System.out.println("Agreement of affiliation : " + alpha.calculateCategoryAgreement("affiliation"));
    }
}
