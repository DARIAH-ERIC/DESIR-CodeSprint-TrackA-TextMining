package org.dariah.desir.secondeCodeSprint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/* in order a json file can be read by doccano (https://github.com/chakki-works/doccano), it should contain a JSON object with a text key
    and other information as meta data, thus a source file should be converted first
    some conditions:
    1. remove the square brackets [] at the start and end of the file
    2. remove comma (,) after each record
* */

public class DataExtractorForDoccano {

    // read json file
    public ArrayList<RawText> readJsonFile(String file) {
        JSONParser parser = new JSONParser();

        ArrayList<RawText> listRawText = new ArrayList<>();
        try {
            Object object = parser.parse(new FileReader(file));
            JSONArray textArray = (JSONArray) object;

            for (Object obj : textArray) {
                JSONObject textObject = (JSONObject) obj;
                String identifier = (String) textObject.get("identifier");
                String title = (String) textObject.get("title");
                String text = (String) textObject.get("text");

                // fill the object of rawText
                RawText rawText = new RawText();
                rawText.setIdentifier(identifier);
                rawText.setTitle(title);
                rawText.setText(text);

                // add the object to the list
                listRawText.add(rawText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listRawText;
    }

    // write json file
    public void writeJsonFile(ArrayList<RawText> textList, String file) {

        JSONArray listText = new JSONArray();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        for (int i=0; i<textList.size();i++) {
            JSONObject textObject = new JSONObject();
            JSONObject metaInfo = new JSONObject();

            // add elements to "meta info" json object
            metaInfo.put("identifier", textList.get(i).getIdentifier());
            metaInfo.put("title", textList.get(i).getTitle());

            // add meta info object to text object
            textObject.put("meta", metaInfo);

            // add json element to "text" json object
            textObject.put("text", textList.get(i).getText());

            listText.add(textObject);
        }

        // not pretty formatting results
        String resultJSONToString = listText.toJSONString();

        // pretty formatting results
        //String prettyJson = gson.toJson(listText);

        // remove the square brackets [] at the start and end of the file; remove comma (,) after each record
        String resultJSONToStringNew = resultJSONToString.replaceAll("[\\[\\]]", "").replaceAll("},\\{","}\r\n{");

        // write json file
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(resultJSONToStringNew);

            File filePath = new File(file);
            System.out.println("File is saved in " +filePath.getAbsolutePath());
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        String inputFile = "data/secondCodeSprint/json/raw/acknowledgements.json";
        String outputFile = "data/secondCodeSprint/json/raw/acknowledgementsFormattedForDoccano.json";

        DataExtractorForDoccano dataExtractor = new DataExtractorForDoccano();

        // read a Json file
        ArrayList<RawText> textList = dataExtractor.readJsonFile(inputFile);

        // write a new Json file
        dataExtractor.writeJsonFile(textList, outputFile);
    }
}
