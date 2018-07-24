package org.dariah.desir.service;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class SampleController {

    private static final String template = "Hello, %s!";

    @RequestMapping("/version")
    public String getVersion() {
        return "0.1.0";
    }


    @RequestMapping(value = "/process", method = RequestMethod.POST)
    public String processPdf(@RequestParam(value = "file") MultipartFile pdf) {
        //integration

        return "{'result': 'bao'}";
    }
}
