package org.dariah.desir.service;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    private static final String template = "Hello, %s!";

    @RequestMapping("/version")
    public String getVersion() {
        return "0.1.0";
    }
}
