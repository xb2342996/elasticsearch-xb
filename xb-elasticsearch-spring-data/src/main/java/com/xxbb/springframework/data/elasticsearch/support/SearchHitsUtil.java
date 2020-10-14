package com.xxbb.springframework.data.elasticsearch.support;


import org.elasticsearch.search.SearchHits;

public final class SearchHitsUtil {
    private SearchHitsUtil() {}

    public static long getTotalCount(SearchHits searchHits) {
        return searchHits.getTotalHits().value;
    }
}
