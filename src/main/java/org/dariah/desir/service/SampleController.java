package org.dariah.desir.service;

import com.google.gson.JsonObject;
import org.dariah.desir.grobid.GrobidClient;
import org.dariah.desir.grobid.GrobidParsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.IOUtils;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class SampleController {

    private static final String template = "Hello, %s!";

    @RequestMapping("/version")
    public String getVersion() {
        return "0.1.0";
    }
    
    @Autowired
    private GrobidParsers grobidParsers;
    
    @Autowired
    private GrobidClient grobidClient;

    @Autowired
    private EntityFishingService entityFishingService;


    @RequestMapping(value = "/process", method = RequestMethod.POST, produces = "application/json")
    public String processPdf(@RequestParam(value = "file") MultipartFile pdf) {
        String result = null;
        try {
            result = this.entityFishingService.pdfProcessing(pdf);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        return result;
    }

    @RequestMapping(value = "/entity_fishing", method = RequestMethod.POST, produces = "application/json")
    public String getEntityFishingResult(@RequestParam(value = "file") MultipartFile pdf) {

        String result = null;
        try {

            result = this.grobidClient.processFulltextDocument(pdf.getInputStream());
            result = this.grobidParsers.processAbstract(new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8)));
            result = this.entityFishingService.rawAbstractProcessing(result);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "{\"result\":" + result+ "}";
    }
}
