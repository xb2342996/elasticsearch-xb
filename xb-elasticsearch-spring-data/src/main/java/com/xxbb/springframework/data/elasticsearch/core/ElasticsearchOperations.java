package com.xxbb.springframework.data.elasticsearch.core;

import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Objects;

public interface ElasticsearchOperations extends DocumentOperations, SearchOperations{

    IndexOperations indexOps(Class<?> clazz);

    IndexOperations indexOps(IndexCoordinates index);

    ElasticsearchConverter getElasticsearchConverter();

    IndexCoordinates getIndexCoordinatesFor(Class<?> clazz);

    @Nullable
    String getEntityRouting(Object entity);

    @Nullable
    default String stringIdRepresentation(@Nullable Object id) {
        return Objects.toString(id, null);
    }
}
