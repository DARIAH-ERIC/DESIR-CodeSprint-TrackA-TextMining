package org.dariah.desir.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;


/**
 * Examples of accessing entity-fishing's rest API
 */
@Service
public class EntityFishingService {
    private String HOST = "nerd.huma-num.fr/nerd/service";
    //    private String HOST = "localhost:8090/service";
    private String DISAMBIGUATE_SERVICE = "/disambiguate";
    private String CONCEPT_SERVICE = "/kb/concept";
    private String CONCEPT_DOI = "/kb/doi";
    private JsonParser jsonParser = new JsonParser();

    private int PORT = -1;

    private RestTemplate restTemplate = new RestTemplate();

    private static org.springframework.http.HttpEntity<String> entity;

    public EntityFishingService() {

    }

    public EntityFishingService(String host) {
        HOST = host;
    }

    public EntityFishingService(String host, int port) {
        HOST = host;
        PORT = PORT;
    }

    public String termDisambiguate(Map<String, Double> listOfTerm, String language) {

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

            for (Map.Entry<String, Double> list : listOfTerm.entrySet()) {
                term = list.getKey();
                score = list.getValue();
                ObjectNode termNode = mapper.createObjectNode();
                termNode.put("term", term);
                termNode.put("score", score);
                termsNode.add(termNode);
            }

            node.set("termVector", termsNode);

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
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String textDisambiguate(String text, String language) {
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
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public String rawAbstractProcessing(String text) {

        /*
        curl 'http://cloud.science-miner.com/nerd/service/disambiguate'
        -X POST -F
        "query={ 'text': 'The text is here .', 'processSentence': [  ], 'sentences': [ ], 'entities': [  ]'language': {'lang':'en'} }"
        * */
        String result = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_UTF8);
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("text", text);
        entity = new org.springframework.http.HttpEntity<String>(requestBody.toString(), headers);
        ResponseEntity<String> response = this.restTemplate.exchange(this.HOST + DISAMBIGUATE_SERVICE, HttpMethod.POST, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            result = response.getBody().toString();
        }

        return result;
    }


    public String pdfProcessing(InputStream pdf) {
        /*
         * curl 'http://cloud.science-miner.com/nerd/service/disambiguate'
         * -X POST
         * -F
         * "query={ 'entities': [], 'nbest': false, 'sentence': false, 'customisation': 'generic'}"
         * -F"file=@11_Anne FOCKE_The influence of catch trials on the consolidation of motor memory in force field adaptation tasks.pdf"
         *
         * */
        String result = null;

        MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();

        try {
            bodyMap.add("file", IOUtils.toByteArray(pdf));
            bodyMap.add("query", this.jsonParser.parse("{ 'entities': [], 'nbest': false, 'sentence': false, 'customisation': 'generic'}").getAsJsonObject().toString());


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
            org.springframework.http.HttpEntity<MultiValueMap<String, Object>> entity = new org.springframework.http.HttpEntity<>(bodyMap, headers);
            ResponseEntity<String> response = this.restTemplate.exchange(this.HOST + DISAMBIGUATE_SERVICE, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                result = response.getBody().toString();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }


    public String getConcept(String id) {
        String response = null;
        String urlNerd = "http://" + this.HOST + CONCEPT_SERVICE + "/" + id;
        if ((id != null) || (id.startsWith("Q") || (id.startsWith("P")))) {
            try {
                HttpClient client = HttpClientBuilder.create().build();

                HttpGet request = new HttpGet(urlNerd);
                HttpResponse httpResponse = client.execute(request);
                HttpEntity entity = httpResponse.getEntity();

                int responseId = httpResponse.getStatusLine().getStatusCode();
                if (responseId == HttpStatus.SC_OK) {
                    response = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
                    return response;
                } else {
                    return response;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    public String lookupWikidataByDoi(String doi) throws UnsupportedEncodingException {
        String response = null;
        String urlNerdBase = "http://" + this.HOST + CONCEPT_DOI + "/";

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
            response = entity.toString();

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

    public void saveToFile(String resultToSave) {
        String outputFile = "data/json/resultEntityFishing.json";
        try {
            File fl = new File(outputFile);

            BufferedWriter result = new BufferedWriter(new FileWriter(fl));

            result.write(resultToSave);
            result.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*    public static void main(String[] args) throws Exception{
        String result, text = null;
        String lang = "en";
        String fileInputXML = "11_Anne FOCKE_The influence of catch trials on the consolidation of motor memory in force field adaptation tasks.pdf.tei.xml";
        String fileInputPdf = "src/main/resources/11_Anne FOCKE_The influence of catch trials on the consolidation of motor memory in force field adaptation tasks.pdf";
        GrobidParsers grobidParsers = new GrobidParsers();
        try {
            EntityFishingService entityFishingService = new EntityFishingService("http://cloud.science-miner.com/nerd/service");
            ClassPathResource resource = new ClassPathResource(fileInputXML);
            InputStream inputStream =resource.getInputStream();

            // text disambiguation
            text = grobidParsers.processAbstract(inputStream);
           //result = entityFishingService.textDisambiguate(text, lang);

            // term disambiguation
            //keywordList = grobidParsers.processKeyword(inputStream);
            //result = entityFishingService.termDisambiguate(keywordList, lang);

            //pdf processing
//            File file = new File(fileInputPdf);
//            Path path = Paths.get(file.getAbsolutePath());
//            String name = file.getName();
//            String originalFileName = file.getName();
//            String contentType = "text/plain";
//            byte[] content = Files.readAllBytes(path);
//
//            MultipartFile multipartFile = new MockMultipartFile(name, originalFileName, contentType, content);
            InputStream stream = new ByteArrayInputStream(fileInputPdf.getBytes(StandardCharsets.UTF_8));
            result = entityFishingService.pdfProcessing(stream);

            //kb concept
            //result = entityFishingService.getConcept("Q1");

            // saving the result
            String resultInJson = entityFishingService.toJson(result);
            entityFishingService.saveToFile(resultInJson);

            System.out.println(result);
        } catch (IOException e){
            e.printStackTrace();
        }
    }*/
}
