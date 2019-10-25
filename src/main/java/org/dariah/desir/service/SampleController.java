package org.dariah.desir.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dariah.desir.data.*;
import org.dariah.desir.client.CookingClient;
import org.dariah.desir.client.GrobidClient;
import org.dariah.desir.client.GrobidParsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
public class SampleController {

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

    @Autowired
    private CookingClient authorDisambiguationClient;


    @RequestMapping(value = "/processAuthor", method = RequestMethod.POST, produces = "application/json")
    public OverlayResponse processPdf(@RequestParam(value = "file") MultipartFile pdf) {

        OverlayResponse response = null;
        try {
            InputStream input = pdf.getInputStream();

            final File tempFile = File.createTempFile("prefix", "suffix");
            tempFile.deleteOnExit();

            FileUtils.copyToFile(input, tempFile);

            System.out.println("Grobid extraction process...");
            String resultGrobid = grobidClient.processFulltextDocument(IOUtils.toBufferedInputStream(new FileInputStream(tempFile)));

            System.out.println("Author disambiguation...");
            String resultDisambiguation = authorDisambiguationClient.disambiguate(IOUtils.toInputStream(resultGrobid, StandardCharsets.UTF_8), "filename.xml");

            final List<DisambiguatedAuthor> disambiguatedAuthors = grobidParsers.processAuthorNames(IOUtils.toInputStream(resultDisambiguation, StandardCharsets.UTF_8));
            response = new OverlayResponse<DisambiguatedAuthor>(disambiguatedAuthors);

            final Page pageDimension = Page.getPageDimension(tempFile);
            response.setPageDimention(pageDimension);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @RequestMapping(value = "/processCitation", method = RequestMethod.POST, produces = "application/json")
    public OverlayResponse processCitation(@RequestParam(value = "file") MultipartFile pdf) {

        OverlayResponse response = null;
        try {
            InputStream input = pdf.getInputStream();

            final File tempFile = File.createTempFile("prefix", "suffix");
            tempFile.deleteOnExit();

            FileUtils.copyToFile(input, tempFile);

            System.out.println("Grobid extraction process...");
            String resultGrobid = grobidClient.processFulltextDocument(IOUtils.toBufferedInputStream(new FileInputStream(tempFile)));

            final List<ResolvedCitation> resolvedCitations = grobidParsers.processCitations(IOUtils.toInputStream(resultGrobid, StandardCharsets.UTF_8));
            response = new OverlayResponse<ResolvedCitation>(resolvedCitations);

            final Page pageDimension = Page.getPageDimension(tempFile);
            response.setPageDimention(pageDimension);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @RequestMapping(value = "/processAcknowledgment", method = RequestMethod.POST, produces = "application/json")
    public OverlayResponse processAcknowledment(@RequestParam(value = "file") MultipartFile pdf) {

        OverlayResponse response = null;
        try {
            InputStream input = pdf.getInputStream();

            final File tempFile = File.createTempFile("prefix", "suffix");
            tempFile.deleteOnExit();

            FileUtils.copyToFile(input, tempFile);

            System.out.println("Grobid extraction process...");
            String resultGrobid = grobidClient.processFulltextDocument(IOUtils.toBufferedInputStream(new FileInputStream(tempFile)));

            System.out.println("Acknowledgment extraction process...");
            final List<ResolvedAcknowledgment> resolvedAcknowledgments = grobidParsers.processAcknowledgments(IOUtils.toInputStream(resultGrobid, StandardCharsets.UTF_8));
             for (ResolvedAcknowledgment ack : resolvedAcknowledgments){
                System.out.println(ack.getText() + "; ");
            }
            response = new OverlayResponse<ResolvedAcknowledgment>(resolvedAcknowledgments);

            final Page pageDimension = Page.getPageDimension(tempFile);
            response.setPageDimention(pageDimension);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @RequestMapping(value = "/processNamedEntities", method = RequestMethod.POST, produces = "application/json")
    public String processNamedEntities(@RequestParam(value = "file") MultipartFile pdf) {

        String resultEntityFishing = null;
        try {
            InputStream input = pdf.getInputStream();

            final File tempFile = File.createTempFile("prefix", "suffix");
            tempFile.deleteOnExit();

            FileUtils.copyToFile(input, tempFile);
            System.out.println("Entity fishing");
            resultEntityFishing = this.entityFishingService.pdfProcessing(IOUtils.toBufferedInputStream(new FileInputStream(tempFile)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultEntityFishing;
    }
}
