package org.dariah.desir.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.scienceminer.nerd.client.NerdClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Examples of accessing entity-fishing's rest API
 */
@Service
public class EntityFishingService {

    NerdClient client;

    public EntityFishingService() {

    }

    public EntityFishingService(String host) {
        client = new NerdClient(host);
    }

    public EntityFishingService(String host, int port) {
        client = new NerdClient(host, port);
    }

    public String termDisambiguate(Map<String, Double> listOfTerm, String language) {


        return client.disambiguateTerm(listOfTerm, language).toString();
    }

    public String textDisambiguate(String text, String language) {

        return client.disambiguateText(text, language).toString();
    }

    /*
        curl 'http://cloud.science-miner.com/nerd/service/disambiguate'
        -X POST -F
        "query={ 'text': 'The text is here .', 'processSentence': [  ], 'sentences': [ ], 'entities': [  ]'language': {'lang':'en'} }"
    **/
    public String rawAbstractProcessing(String text) {
        return client.disambiguateText(text, "en").toString();
    }

    /*
     * curl 'http://cloud.science-miner.com/nerd/service/disambiguate'
     * -X POST
     * -F
     * "query={ 'entities': [], 'nbest': false, 'sentence': false, 'customisation': 'generic'}"
     * -F"file=@11_Anne FOCKE_The influence of catch trials on the consolidation of motor memory in force field adaptation tasks.pdf"
     *
     * */
    public String pdfProcessing(InputStream pdf) {

        return client.disambiguatePDF(pdf, "en").toString();
    }


    public String getConcept(String id) {
        return client.getConcept(id).toString();
    }

    public String lookupWikidataByDoi(String doi) throws UnsupportedEncodingException {
        RestTemplate restTemplate = new RestTemplate();

        String HOST = "nerd.huma-num.fr/nerd/service";
        String CONCEPT_DOI = "/kb/doi";

        String response = null;
        String urlNerdBase = "http://" + HOST + CONCEPT_DOI + "/";

        //original
        String urlNerd = urlNerdBase + URLEncoder.encode(doi, "utf-8");
        org.springframework.http.HttpStatus statusCode = null;
        ResponseEntity<String> forEntity = null;
        try {
            forEntity = restTemplate.getForEntity(urlNerd, String.class);
            statusCode = forEntity.getStatusCode();
        } catch (HttpClientErrorException e) {

        }


        if (!org.springframework.http.HttpStatus.OK.equals(statusCode)) {
            //lowercase
            urlNerd = urlNerdBase + URLEncoder.encode(doi.toUpperCase(), "utf-8");
            try {
                forEntity = restTemplate.getForEntity(urlNerd, String.class);
                statusCode = forEntity.getStatusCode();
            } catch (HttpClientErrorException e) {

            }


            if (!org.springframework.http.HttpStatus.OK.equals(statusCode)) {
                //uppercase
                urlNerd = urlNerdBase + URLEncoder.encode(doi.toLowerCase(), "utf-8");
                try {
                    restTemplate.getForEntity(urlNerd, String.class);
                    statusCode = forEntity.getStatusCode();
                } catch (HttpClientErrorException e) {

                }
            }
        }

        if (org.springframework.http.HttpStatus.OK.equals(statusCode)) {
            response = forEntity.toString();

            JsonParser parser = new JsonParser();
            JsonObject root = (JsonObject) parser.parse(response);
            response = root.get("wikidataID").getAsString();
        }

        return response;
    }

    public String toJson(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJsonString = gson.toJson(jsonObject);
        return prettyJsonString;
    }

}
