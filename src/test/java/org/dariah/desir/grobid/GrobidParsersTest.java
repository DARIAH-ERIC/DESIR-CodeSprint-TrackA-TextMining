package org.dariah.desir.grobid;

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