package com.xxbb.springframework.data.elasticsearch.core.document;

import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SearchDocumentResponse {
    private final long totalHits;
    private final String totalHitsRelation;
    private final float maxScore;
    private final String scrollId;
    private final List<SearchDocument> searchDocuments;
    private final Aggregations aggregations;

    public SearchDocumentResponse(long totalHits, String totalHitsRelation, float maxScore, String scrollId, List<SearchDocument> searchDocuments, Aggregations aggregations) {
        this.totalHits = totalHits;
        this.totalHitsRelation = totalHitsRelation;
        this.maxScore = maxScore;
        this.scrollId = scrollId;
        this.searchDocuments = searchDocuments;
        this.aggregations = aggregations;
    }

    public long getTotalHits() {
        return totalHits;
    }

    public String getTotalHitsRelation() {
        return totalHitsRelation;
    }

    public float getMaxScore() {
        return maxScore;
    }

    public String getScrollId() {
        return scrollId;
    }

    public List<SearchDocument> getSearchDocuments() {
        return searchDocuments;
    }

    public Aggregations getAggregations() {
        return aggregations;
    }

    public static SearchDocumentResponse from(SearchResponse searchResponse) {
        Assert.notNull(searchResponse, "searchResponse must not be null");

        Aggregations aggregations = searchResponse.getAggregations();
        String scrollId = searchResponse.getScrollId();
        SearchHits searchHits = searchResponse.getHits();

        SearchDocumentResponse searchDocumentResponse = from(searchHits, scrollId, aggregations);
        return searchDocumentResponse;
    }

    public static SearchDocumentResponse from(SearchHits searchHits, @Nullable String scrollId, @Nullable Aggregations aggregations) {
        TotalHits responseTotalHits = searchHits.getTotalHits();

        long totalHits;
        String totalHitsRelation;

        if (responseTotalHits != null) {
            totalHits = responseTotalHits.value;
            totalHitsRelation = responseTotalHits.relation.name();
        } else {
            totalHits = searchHits.getHits().length;
            totalHitsRelation = "OFF";
        }

        float maxScore = searchHits.getMaxScore();

        List<SearchDocument> searchDocuments = StreamSupport.stream(searchHits.spliterator(), false)
                .filter(Objects::nonNull)
                .map(DocumentAdapters::from)
                .collect(Collectors.toList());

        return new SearchDocumentResponse(totalHits, totalHitsRelation, maxScore, scrollId, searchDocuments, aggregations);
    }
}
