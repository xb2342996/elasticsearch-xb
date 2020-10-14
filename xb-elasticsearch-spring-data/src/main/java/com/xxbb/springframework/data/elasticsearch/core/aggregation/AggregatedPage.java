package com.xxbb.springframework.data.elasticsearch.core.aggregation;

import com.xxbb.springframework.data.elasticsearch.core.ScoredPage;
import com.xxbb.springframework.data.elasticsearch.core.ScrolledPage;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.lang.Nullable;

public interface AggregatedPage<T> extends ScrolledPage<T>, ScoredPage<T> {
    boolean hasAggregations();

    @Nullable
    Aggregations getAggregations();

    @Nullable
    Aggregation getAggregation(String name);
}
