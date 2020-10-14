package com.xxbb.springframework.data.elasticsearch.core.mapping;

import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

public class SimpleElasticsearchMappingContext extends AbstractMappingContext<SimpleElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> {
    @Override
    protected <T> SimpleElasticsearchPersistentEntity<?> createPersistentEntity(TypeInformation<T> typeInformation) {
        return new SimpleElasticsearchPersistentEntity<>(typeInformation);
    }

    @Override
    protected ElasticsearchPersistentProperty createPersistentProperty(Property property, SimpleElasticsearchPersistentEntity<?> elasticsearchPersistentProperties, SimpleTypeHolder simpleTypeHolder) {
        return new SimpleElasticsearchPersistentProperty(property, elasticsearchPersistentProperties, simpleTypeHolder);
    }
}
