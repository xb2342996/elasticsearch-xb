package com.xxbb.springframework.data.elasticsearch.core.query;

import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

public class HighlightQuery {
    private final HighlightBuilder highlightBuilder;

    public HighlightQuery(HighlightBuilder highlightBuilder) {
        this.highlightBuilder = highlightBuilder;
    }

    public HighlightBuilder getHighlightBuilder() {
        return highlightBuilder;
    }
}
