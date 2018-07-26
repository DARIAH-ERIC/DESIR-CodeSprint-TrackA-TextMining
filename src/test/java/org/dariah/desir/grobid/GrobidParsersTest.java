package org.dariah.desir.grobid;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParseException;
import org.dariah.desir.service.EntityFishingService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class GrobidParsersTest {

    GrobidParsers target;

    @Before
    public void setUp() {
        target = new GrobidParsers();
    }

    @Test
    public void test1() {
        target.parseDocument(this.getClass().getResourceAsStream("39_Sylvie Ratté_Compte rendu d’expériences simples avec le PC tablette.pdf.tei.xml"));
    }

}