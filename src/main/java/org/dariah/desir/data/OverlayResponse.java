package org.dariah.desir.data;

import java.util.ArrayList;
import java.util.List;

public class OverlayResponse {
    private List<DisambiguatedAuthor> authors = new ArrayList<>();
    private List<ResolvedCitation> citations = new ArrayList<>();

    public List<DisambiguatedAuthor> getAuthors() {
        return authors;
    }

    public void setAuthors(List<DisambiguatedAuthor> authors) {
        this.authors = authors;
    }

    public List<ResolvedCitation> getCitations() {
        return citations;
    }

    public void setCitations(List<ResolvedCitation> citations) {
        this.citations = citations;
    }
}
