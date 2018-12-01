package org.dariah.desir.client;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;

public class AuthorDisambiguationClientTest {

    CookingClient target;

    @Before
    public void setUp() throws Exception {

        target = new CookingClient();

    }

    @Test
    public void test1() throws Exception {
        final InputStream resourceAsStream = this.getClass().getResourceAsStream("/lopez2010experiments.pdf");
        final String disambiguate = target.disambiguate(resourceAsStream, "lopez2010experiments.pdf");

        System.out.println(disambiguate);
    }
}