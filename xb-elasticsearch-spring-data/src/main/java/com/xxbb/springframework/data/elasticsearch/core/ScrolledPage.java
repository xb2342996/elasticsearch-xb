package com.xxbb.springframework.data.elasticsearch.core;

import org.springframework.data.domain.Page;

public interface ScrolledPage<T> extends Page<T> {
    String getScrollId();
}
