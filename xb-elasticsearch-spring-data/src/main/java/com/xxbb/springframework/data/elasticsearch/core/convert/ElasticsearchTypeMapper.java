package com.xxbb.springframework.data.elasticsearch.core.convert;

import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.convert.TypeMapper;
import org.springframework.data.mapping.context.MappingContext;

import java.util.Map;

public interface ElasticsearchTypeMapper extends TypeMapper<Map<String, Object>> {
    String DEFAULT_TYPE_KEY = "_class";

    boolean isTypeKey(String key);

    default boolean containsTypeInformation(Map<String, Object> source) {
        return readType(source) != null;
    }

    static ElasticsearchTypeMapper create(MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {
        return new DefaultElasticsearchTypeMapper(DEFAULT_TYPE_KEY, mappingContext);
    }
}
