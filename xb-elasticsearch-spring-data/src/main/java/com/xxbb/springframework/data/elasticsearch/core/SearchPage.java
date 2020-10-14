package com.xxbb.springframework.data.elasticsearch.core;

import org.springframework.data.domain.Page;

public interface SearchPage<T> extends Page<SearchHit<T>> {
    SearchHits<T> getSearchHits();
}
