package com.xxbb.springframework.data.elasticsearch.repository.query;

import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import org.springframework.util.Assert;

public class SimpleElasticsearchEntityMetadata<T> implements ElasticsearchEntityMetadata<T>{

    private final Class<T> type;
    private final ElasticsearchPersistentEntity<?> entity;

    public SimpleElasticsearchEntityMetadata(Class<T> type, ElasticsearchPersistentEntity<?> entity) {
        Assert.notNull(type, "Type must not be null");
        Assert.notNull(entity, "Entity must not be null");
        this.type = type;
        this.entity = entity;
    }

    @Override
    public String getIndexName() {
        return entity.getIndexCoordinates().getIndexName();
    }

    @Override
    public Class<T> getJavaType() {
        return type;
    }
}
