package org.dariah.desir.data;

/** Resolved citation with wikidata id */
public class ResolvedCitation {
    private String wikidataID;
    private String doi;
    private String coordinates;
    private String title;


    public String getWikidataID() {
        return wikidataID;
    }

    public void setWikidataID(String wikidataID) {
        this.wikidataID = wikidataID;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
