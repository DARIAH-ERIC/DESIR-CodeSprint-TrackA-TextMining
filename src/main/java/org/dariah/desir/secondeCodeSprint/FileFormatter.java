package org.dariah.desir.secondeCodeSprint;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileFormatter {
    public void formatAnnotatedFile(String file) {
        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader br = Files.newBufferedReader(Paths.get(file));
            String strLine = null;
            ArrayList<String> contents = new ArrayList<>();
            while((strLine = br.readLine()) != null){
                contents.add(strLine);
            }

            sb.append("[\n");
            for (int i=0;i<contents.size();i++ ){
                sb.append(contents.get(i));
                if (i != (contents.size()-1)){
                    sb.append(",\n");
                }
            }
            sb.append("\n]");
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        writeFileJson(sb);
        System.out.println(sb);
    }

    public void writeFileJson(StringBuilder content){
        String outputFile = "data/secondCodeSprint/json/annotated/acknowledgmentsAnnotatedExampleFormatted.json";
        BufferedWriter writer = null;
        try{
            File file = new File(outputFile);
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(content.toString());
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String inputFile = "data/secondCodeSprint/json/annotated/acknowledgmentsAnnotatedExample.json";

        FileFormatter fileFormatter = new FileFormatter();
        fileFormatter.formatAnnotatedFile(inputFile);

    }
}
