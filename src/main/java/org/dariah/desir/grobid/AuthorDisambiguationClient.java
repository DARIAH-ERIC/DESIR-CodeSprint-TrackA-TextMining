package org.dariah.desir.grobid;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by lfoppiano on 17/08/16.
 */

@Service
public class AuthorDisambiguationClient {

        private String apiUrL = "https://traces1.inria.fr/cooking";
//    private String apiUrL = "http://localhost:8091";


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

    public String disambiguate(InputStream inputStream, String filename) throws Exception {


        ByteArrayResource contentsAsResource = new ByteArrayResource(IOUtils.toByteArray(inputStream)) {
            @Override
            public String getFilename() {
                return filename; // Filename has to be returned in order to be able to post.
            }
        };

        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", contentsAsResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        String response = restTemplate.postForObject(apiUrL + "/disambiguate", requestEntity, String.class);

        return response;
    }
}
