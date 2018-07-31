package org.dariah.desir.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.*;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.dariah.desir.grobid.GrobidParsers;
import org.springframework.core.io.ClassPathResource;

import javax.print.URIException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.apache.http.entity.ContentType.MULTIPART_FORM_DATA;


/**
 * Examples of accessing entity-fishing's rest API
 */

public class EntityFishingService {
    private String HOST = null;
    private String DISAMBIGUATE_SERVICE = "/disambiguate";
    private String CONCEPT_SERVICE = "/kb/concept";

    private int PORT = -1;

    public EntityFishingService() {

    }

    public EntityFishingService(String host) {
        HOST = host;
    }

    public EntityFishingService(String host, int port) {
        HOST = host;
        PORT = PORT;
    }

    public String termDisambiguate(Map<String, Double> listOfTerm, String language){

        String result = null, term = null;
        double score = 0.0;
        try {
            final URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(this.HOST + DISAMBIGUATE_SERVICE)
                    .build();


            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();

            ArrayNode termsNode = mapper.createArrayNode();

            for(Map.Entry<String, Double> list : listOfTerm.entrySet()){
                term = list.getKey();
                score= list.getValue();
                ObjectNode termNode = mapper.createObjectNode();
                termNode.put("term",term);
                termNode.put("score",score);
                termsNode.add(termNode);
            }

            node.set("termVector",termsNode);

            if (language != null) {
                ObjectNode dataNode = mapper.createObjectNode();
                dataNode.put("lang", language);
                node.set("language", dataNode);
            }
            HttpPost httpPost = new HttpPost(uri);
            CloseableHttpClient httpResponse = HttpClients.createDefault();

            httpPost.setHeader("Content-Type", APPLICATION_JSON.toString());
            httpPost.setEntity(new StringEntity(node.toString()));
            CloseableHttpResponse closeableHttpResponse = httpResponse.execute(httpPost);

            if (closeableHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return IOUtils.toString(closeableHttpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
            } else {
                return result;
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String textDisambiguate(String text, String language)  {
        String result = null;
        try {
            final URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(this.HOST + DISAMBIGUATE_SERVICE)
                    .build();

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("text", text);
            if (language != null) {
                ObjectNode dataNode = mapper.createObjectNode();
                dataNode.put("lang", language);
                node.set("language", dataNode);
            }
            HttpPost httpPost = new HttpPost(uri);
            CloseableHttpClient httpResponse = HttpClients.createDefault();

            httpPost.setHeader("Content-Type", APPLICATION_JSON.toString());
            httpPost.setEntity(new StringEntity(node.toString()));
            CloseableHttpResponse closeableHttpResponse = httpResponse.execute(httpPost);

            if (closeableHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return IOUtils.toString(closeableHttpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
            } else {
                return result;
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }catch (ClientProtocolException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        return result;
    }

    public String pdfProcessing(String fileToBeUploaded, String language) throws Exception {
        // need to be checked
        String result = null;
        File file = new File(fileToBeUploaded);
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);
            final URI uri = new URIBuilder()
                    .setScheme("http")
                    .setHost(this.HOST + DISAMBIGUATE_SERVICE)
                    .build();

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();

            if (language != null) {
                ObjectNode dataNode = mapper.createObjectNode();
                dataNode.put("lang", language);
                node.set("language", dataNode);
            }

            //FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("file", new InputStreamBody(fis, file.getName()));
            HttpEntity entity = builder.build();

            HttpPost httpPost = new HttpPost(uri);
            CloseableHttpClient httpResponse = HttpClients.createDefault();

            httpPost.setHeader("Content-Type", APPLICATION_JSON.toString());
            httpPost.setEntity(new StringEntity(node.toString()));
            httpPost.setEntity(entity);
            CloseableHttpResponse closeableHttpResponse = httpResponse.execute(httpPost);

            if (closeableHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return IOUtils.toString(closeableHttpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
            } else {
                return result;
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return result;
    }

//    public String pdfProcessing (String fileToBeuploaded, String language, String outputFile) {
//        String result = null;
//        String url = "http://" + this.HOST + DISAMBIGUATE_SERVICE;
//        String query = "{\'language\': {\'lang\':\'"+ language +"\'}}, \'entities\': [], \'nbest\': false, \'sentence\': false, \'customisation\': \'generic\'}";
//        File file = new File(fileToBeuploaded);
//        //curl 'http://cloud.science-miner.com/nerd/service/disambiguate' -X POST -F "query={'language': {'lang':'en'}}, 'entities': [], 'nbest': false, 'sentence': false, 'customisation': 'generic'}" -F"file=@11_Anne FOCKE_The influence of catch trials on the consolidation of motor memory in force field adaptation tasks.pdf"
//        try{
//            // adding new elements of command in array list of string
//            ArrayList<String> element = new ArrayList<String>();
//
//            element.add("curl");
//            element.add(url);
//            element.add("-XPOST");
//            element.add("-F");
//            element.add("\"query=" + query + "\"");
//            element.add("-F");
//            element.add("file=@" + file.getName() + "\"");
//
//            //element.add("query={ \"termVector\": [ { \"term\" : \"computer science\", \"score\" : 0.3 }, { \"term\" : \"engine\", \"score\" : 0.1 } ], \"language\": { \"lang\": \"en\" }, \"resultLanguages\": [\"de\"], \"nbest\": 0, \"customisation\": \"generic\" }");
//            System.out.println(element);
//
//            // converting array list of string to string array
//            String[] command = element.toArray(new String[element.size()]);
//
//            // executing the command
//            Process process = Runtime.getRuntime().exec(command);
//
//            // getting the result of execution
//            StringBuilder processOutput = new StringBuilder();
//            try (BufferedReader processOutputReader = new BufferedReader(
//                    new InputStreamReader(process.getInputStream()));) {
//                String readLine;
//                while ((readLine = processOutputReader.readLine()) != null) {
//                    processOutput.append(readLine + System.lineSeparator());
//                }
//                process.waitFor();
//            }
//
//            System.out.println(processOutput);
//            result = processOutput.toString();
//
//        }catch (IOException e){
//            e.printStackTrace();
//        }catch (InterruptedException e){
//            e.printStackTrace();
//        }
//        return result;
//    }

    public String getConcept(String id)  {
        String response = null;
        String urlNerd =  "http://"+ this.HOST + CONCEPT_SERVICE + "/" + id;
        if ((id != null) || (id.startsWith("Q") || (id.startsWith("P")))){
            try {
                HttpClient client = HttpClientBuilder.create().build();

                HttpGet request = new HttpGet(urlNerd);
                HttpResponse httpResponse = client.execute(request);
                HttpEntity entity = httpResponse.getEntity();

                int responseId = httpResponse.getStatusLine().getStatusCode();
                if (responseId == HttpStatus.SC_OK) {
                    response = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
                    return response;
                }else {
                    return response;
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        return response;
    }

    public String toJson(String jsonString){
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJsonString = gson.toJson(jsonObject);
        return prettyJsonString;
    }

    public void saveToFile(String resultToSave) {
        String outputFile ="data/json/resultEntityFishing.json";
        try {
            File fl = new File(outputFile);

            BufferedWriter result = new BufferedWriter(new FileWriter(fl));

            result.write(resultToSave);
            result.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception{
        String result, text = null;
        String lang = "en";
        String fileInputXML = "11_Anne FOCKE_The influence of catch trials on the consolidation of motor memory in force field adaptation tasks.pdf.tei.xml";
        String fileInputPdf = "src/main/resources/11_Anne FOCKE_The influence of catch trials on the consolidation of motor memory in force field adaptation tasks.pdf";
        GrobidParsers grobidParsers = new GrobidParsers();
        try {
            EntityFishingService entityFishingService = new EntityFishingService("cloud.science-miner.com/nerd/service");
            ClassPathResource resource = new ClassPathResource(fileInputXML);
            InputStream inputStream =resource.getInputStream();

            // text disambiguation
            //text = grobidParsers.processAbstract(inputStream);
            //result = entityFishingService.textDisambiguate(text, lang);

            // term disambiguation
            //keywordList = grobidParsers.processKeyword(inputStream);
            //result = entityFishingService.termDisambiguate(keywordList, lang);

            //pdf processing
            //result = entityFishingService.pdfProcessing(fileInputPdf,lang);

            //kb concept
            result = entityFishingService.getConcept("Q1");

            // saving the result
            String resultInJson = entityFishingService.toJson(result);
            entityFishingService.saveToFile(resultInJson);

            System.out.println(result);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
