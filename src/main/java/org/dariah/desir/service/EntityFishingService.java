package org.dariah.desir.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.dariah.desir.grobid.GrobidParsers;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;


/**
 * Examples of accessing entity-fishing's rest API
 */

public class EntityFishingService {
    private String HOST = null;
    private String DISAMBIGUATE_SERVICE = "/disambiguate";
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

    public String termDisambiguate(Map<String, Double> listOfTerm, String language) throws Exception {

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
        }
        return result;
    }

    public String textDisambiguate(String text, String language) throws Exception {
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
        }
        return result;
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
        String fileInput = "11_Anne FOCKE_The influence of catch trials on the consolidation of motor memory in force field adaptation tasks.pdf.tei.xml";
        Map<String, Double> keywordList = new HashMap<>();
        GrobidParsers grobidParsers = new GrobidParsers();
        try {
            EntityFishingService entityFishingService = new EntityFishingService("cloud.science-miner.com/nerd/service");
            ClassPathResource resource = new ClassPathResource(fileInput);
            InputStream inputStream =resource.getInputStream();

            // processing text disambiguation
            //text = grobidParsers.processAbstract(inputStream);
            //result = entityFishingService.textDisambiguate(text, lang);

            // processing term disambiguation
            keywordList = grobidParsers.processKeyword(inputStream);
            result = entityFishingService.termDisambiguate(keywordList, lang);

            // saving the result
            String resultInJson = entityFishingService.toJson(result);
            entityFishingService.saveToFile(resultInJson);
            System.out.println(resultInJson);
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
