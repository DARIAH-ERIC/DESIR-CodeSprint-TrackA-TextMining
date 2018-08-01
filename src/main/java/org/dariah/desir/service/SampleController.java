package org.dariah.desir.service;

import org.dariah.desir.grobid.GrobidClient;
import org.dariah.desir.grobid.GrobidParsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;

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


    @RequestMapping(value = "/process", method = RequestMethod.POST)
    public String processPdf(@RequestParam(value = "file") MultipartFile pdf) {
       
        String result = null;
        try {
            
            result = this.grobidClient.getHeader(pdf.getInputStream());
            System.out.println(result);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        return "{\"result\":" + result+ "}";
    }
}
