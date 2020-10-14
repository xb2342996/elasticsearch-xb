package com.xxbb.springframework.data.elasticsearch.core.query;

public interface SourceFilter {
    String[] getIncludes();

    String[] getExcludes();
}
