package com.xxbb.springframework.data.elasticsearch.core.aggregation.impl;

import com.xxbb.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import com.xxbb.springframework.data.elasticsearch.core.document.SearchDocumentResponse;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public class AggregatedPageImpl<T> extends PageImpl<T> implements AggregatedPage<T> {

    @Nullable private Aggregations aggregations;
    @Nullable private String scrollId;
    private float maxScore;

    private static Pageable pageableOrUnpagged(@Nullable Pageable pageable) {
        return Optional.ofNullable(pageable).orElse(Pageable.unpaged());
    }

    public AggregatedPageImpl(List<T> content) {
        super(content);
    }

    public AggregatedPageImpl(List<T> content, float maxScore) {
        super(content);
        this.maxScore = maxScore;
    }

    public AggregatedPageImpl(List<T> content, String scrollId) {
        super(content);
        this.scrollId = scrollId;
    }

    public AggregatedPageImpl(List<T> content, String scrollId, float maxScore) {
        this(content, scrollId);
        this.maxScore = maxScore;
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, long total) {
        super(content, pageableOrUnpagged(pageable), total);
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, long total, String scrollId) {
        super(content, pageableOrUnpagged(pageable), total);
        this.scrollId = scrollId;
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, long total, float maxScore) {
        super(content, pageableOrUnpagged(pageable), total);
        this.maxScore = maxScore;
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, long total, String scrollId, float maxScore) {
        this(content, pageableOrUnpagged(pageable), total, scrollId);
        this.maxScore = maxScore;
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, long total, @Nullable Aggregations aggregations) {
        super(content, pageableOrUnpagged(pageable), total);
        this.aggregations = aggregations;
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, long total, @Nullable Aggregations aggregations, String scrollId) {
        this(content, pageableOrUnpagged(pageable), total, aggregations);
        this.scrollId = scrollId;
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, long total, @Nullable Aggregations aggregations, float maxScore) {
        this(content, pageableOrUnpagged(pageable), total, aggregations);
        this.maxScore = maxScore;
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, long total, @Nullable Aggregations aggregations, String scrollId, float maxScore) {
        this(content, pageableOrUnpagged(pageable), total, aggregations, scrollId);
        this.maxScore = maxScore;
    }

    public AggregatedPageImpl(List<T> content, Pageable pageable, SearchDocumentResponse response) {
        this(content, pageableOrUnpagged(pageable), response.getTotalHits(), response.getAggregations(), response.getScrollId(), response.getMaxScore());
    }

    @Override
    public boolean hasAggregations() {
        return aggregations != null;
    }

    @Override
    @Nullable
    public Aggregations getAggregations() {
        return aggregations;
    }

    @Override
    public Aggregation getAggregation(String name) {
        return aggregations != null ? aggregations.get(name) : null;
    }

    @Override
    public float getMaxScore() {
        return maxScore;
    }

    @Nullable
    @Override
    public String getScrollId() {
        return scrollId;
    }
}
