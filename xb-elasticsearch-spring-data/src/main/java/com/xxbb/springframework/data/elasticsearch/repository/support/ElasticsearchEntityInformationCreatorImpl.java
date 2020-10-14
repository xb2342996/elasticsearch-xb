package com.xxbb.springframework.data.elasticsearch.repository.support;

import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.util.Assert;

public class ElasticsearchEntityInformationCreatorImpl implements ElasticsearchEntityInformationCreator{

    private final MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;

    public ElasticsearchEntityInformationCreatorImpl(MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {

        Assert.notNull(mappingContext, "mappingContext must not be null!");
        this.mappingContext = mappingContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, ID> ElasticsearchEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
        ElasticsearchPersistentEntity<T> persistentEntity = (ElasticsearchPersistentEntity<T>) mappingContext.getRequiredPersistentEntity(domainClass);

        Assert.notNull(persistentEntity, String.format("Unable to obtain mapping metadata for %s", domainClass));
        Assert.notNull(persistentEntity.getIdProperty(), String.format("No Id property found for %s", domainClass));
        return new MappingElasticsearchEntityInformation<>(persistentEntity);
    }
}
