package org.dariah.desir.grobid;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.*;
import java.util.Arrays;

/**
 * Created by lfoppiano on 17/08/16.
 */

@Service
public class AuthorDisambiguationClient {

//    private String apiUrL = "http://traces1.inria.fr/cooking";
    private String apiUrL = "http://localhost:8091";


    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorDisambiguationClient.class);

   /* public GrobidClient(Configuration configuration) {
        this.configuration = configuration;
    }*/


    private RestTemplate restTemplate = new RestTemplate();

    public void ping() throws RuntimeException {
        URL url = null;
        HttpURLConnection conn = null;

        try {
            url = new URL(this.apiUrL + "/version");
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Error while connecting to GROBID service. HTTP error: " + conn.getResponseCode());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while connecting to GROBID service", e);
        }
    }

    public String disambiguate(InputStream inputStream) {

        String result = null;

        MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("file", inputStream);

        ByteArrayHttpMessageConverter byteArrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
        restTemplate.getMessageConverters().add(byteArrayHttpMessageConverter);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        org.springframework.http.HttpEntity<MultiValueMap<String, Object>> entity =
                new org.springframework.http.HttpEntity<>(bodyMap, headers);

        final HttpMessageConverterExtractor<String> responseExtractor =
                new HttpMessageConverterExtractor<String>(String.class, restTemplate.getMessageConverters());

        ResponseEntity<String> response = this.restTemplate.exchange(apiUrL + "/disambiguate",
                HttpMethod.POST, entity, String.class);



        if (response.getStatusCode().is2xxSuccessful()) {
            result = response.getBody().toString();
        }


        return result;
    }
}
