package com.xxbb.springframework.data.elasticsearch.core;

public interface SearchScrollHits<T> extends SearchHits<T> {
    String getScrollId();
}
