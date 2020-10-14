package com.xxbb.springframework.data.elasticsearch.core;

import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;


import java.util.Collections;
import java.util.List;

public class SearchHitsImpl<T> implements SearchScrollHits<T> {
    private final long totalHits;
    private final TotalHitsRelation totalHitsRelation;
    private final float maxScore;
    private final String scrollId;
    private final List<? extends SearchHit<T>> searchHits;
    private final Aggregations aggregations;

    public SearchHitsImpl(long totalHits, TotalHitsRelation totalHitsRelation, float maxScore, @Nullable String scrollId,
                          List<? extends SearchHit<T>> searchHits, @Nullable Aggregations aggregations) {
        Assert.notNull(searchHits, "searchHits must not be null");
        this.totalHits = totalHits;
        this.totalHitsRelation = totalHitsRelation;
        this.maxScore = maxScore;
        this.scrollId = scrollId;
        this.searchHits = searchHits;
        this.aggregations = aggregations;
    }

    @Override
    public String getScrollId() {
        return scrollId;
    }

    @Override
    public Aggregations getAggregations() {
        return aggregations;
    }

    @Override
    public float getMaxScore() {
        return maxScore;
    }

    @Override
    public SearchHit<T> getSearchHit(int index) {
        return searchHits.get(index);
    }

    @Override
    public List<SearchHit<T>> getSearchHits() {
        return Collections.unmodifiableList(searchHits);
    }

    @Override
    public long getTotalHits() {
        return totalHits;
    }

    @Override
    public TotalHitsRelation getTotalHitsRelation() {
        return totalHitsRelation;
    }

    @Override
    public String toString() {
        return "SearchHitsImpl{" +
                "totalHits=" + totalHits +
                ", totalHitsRelation=" + totalHitsRelation +
                ", maxScore=" + maxScore +
                ", scrollId='" + scrollId + '\'' +
                ", searchHits=" + searchHits +
                ", aggregations=" + aggregations +
                '}';
    }
}
