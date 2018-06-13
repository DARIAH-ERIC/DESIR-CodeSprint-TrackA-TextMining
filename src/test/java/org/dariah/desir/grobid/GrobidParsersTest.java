package org.dariah.desir.grobid;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GrobidParsersTest {

    GrobidParsers target;
    @Before
    public void setUp() {

        target = new GrobidParsers();
    }

    @Test
    public void test1() {

        target.parseDocument(this.getClass().getResourceAsStream("/p177-constantin.pdf.tei.xml"));
    }

}