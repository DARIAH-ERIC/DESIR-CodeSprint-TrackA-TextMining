package org.dariah.desir.data;

import java.util.ArrayList;
import java.util.List;

public class OverlayResponse<T> {
    private Page pageDimention;
    private List<T> results = new ArrayList<>();

    public OverlayResponse(List<T> resultsA) {
        this.results = resultsA;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> resultsA) {
        this.results = resultsA;
    }

    public Page getPageDimention() {
        return pageDimention;
    }

    public void setPageDimention(Page pageDimention) {
        this.pageDimention = pageDimention;
    }
}
