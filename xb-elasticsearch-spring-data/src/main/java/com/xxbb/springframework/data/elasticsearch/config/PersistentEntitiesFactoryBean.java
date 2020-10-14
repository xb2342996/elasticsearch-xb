package com.xxbb.springframework.data.elasticsearch.config;

import com.xxbb.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.mapping.context.PersistentEntities;

public class PersistentEntitiesFactoryBean implements FactoryBean<PersistentEntities> {

    private final MappingElasticsearchConverter converter;

    public PersistentEntitiesFactoryBean(MappingElasticsearchConverter converter) {
        this.converter = converter;
    }

    @Override
    public PersistentEntities getObject() throws Exception {
        return PersistentEntities.of(converter.getMappingContext());
    }

    @Override
    public Class<?> getObjectType() {
        return PersistentEntities.class;
    }
}
