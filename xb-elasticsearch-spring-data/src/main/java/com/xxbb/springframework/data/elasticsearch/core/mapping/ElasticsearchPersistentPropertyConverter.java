package com.xxbb.springframework.data.elasticsearch.core.mapping;

public interface ElasticsearchPersistentPropertyConverter {
    String write(Object property);

    Object read(String s);
}
