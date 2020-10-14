package com.xxbb.springframework.data.elasticsearch.core;

import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.data.util.Streamable;
import org.springframework.lang.Nullable;

import java.util.Iterator;
import java.util.List;

public interface SearchHits<T> extends Streamable<SearchHit<T>> {
    @Nullable
    Aggregations getAggregations();

    float getMaxScore();

    SearchHit<T> getSearchHit(int index);

    List<SearchHit<T>> getSearchHits();

    long getTotalHits();

    TotalHitsRelation getTotalHitsRelation();

    default boolean hasAggregations() {
        return getAggregations() != null;
    }

    default boolean hasSearchHits() {
        return !getSearchHits().isEmpty();
    }

    default Iterator<SearchHit<T>> iterator() {
        return getSearchHits().iterator();
    }
}
