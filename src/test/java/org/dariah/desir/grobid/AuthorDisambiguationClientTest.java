package org.dariah.desir.grobid;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class AuthorDisambiguationClientTest {

    AuthorDisambiguationClient target;

    @Before
    public void setUp() throws Exception {

        target = new AuthorDisambiguationClient();

    }

    @Test
    public void test1() throws Exception {
        final InputStream resourceAsStream = this.getClass().getResourceAsStream("/lopez2010experiments.pdf");
        //final String disambiguate = target.disambiguate(IOUtils.toByteArray(resourceAsStream));

        //System.out.println(disambiguate);
    }
}