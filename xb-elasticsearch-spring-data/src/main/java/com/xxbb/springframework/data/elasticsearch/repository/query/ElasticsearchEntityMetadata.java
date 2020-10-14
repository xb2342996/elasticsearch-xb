package com.xxbb.springframework.data.elasticsearch.repository.query;

import org.springframework.data.repository.core.EntityMetadata;

public interface ElasticsearchEntityMetadata<T> extends EntityMetadata<T> {
    String getIndexName();


}
