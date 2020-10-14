package com.xxbb.springframework.data.elasticsearch.core;

import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.data.util.CloseableIterator;
import org.springframework.lang.Nullable;

public interface SearchHitsIterator<T> extends CloseableIterator<SearchHit<T>> {

    @Nullable
    Aggregations getAggregations();

    float getMaxScore();

    long getTotalHits();

    TotalHitsRelation getTotalHitsRelation();

    default boolean hasAggregations() {
        return getAggregations() != null;
    }
}
