package com.xxbb.springframework.data.elasticsearch.core;

import org.springframework.data.domain.Page;

public interface ScoredPage<T> extends Page<T> {
    float getMaxScore();
}
