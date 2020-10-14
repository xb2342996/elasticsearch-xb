package com.xxbb.springframework.data.elasticsearch.repository.support;

import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import lombok.NonNull;
import org.elasticsearch.index.VersionType;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.repository.core.support.PersistentEntityInformation;

public class MappingElasticsearchEntityInformation<T, ID> extends PersistentEntityInformation<T, ID> implements ElasticsearchEntityInformation<T, ID> {

    private final ElasticsearchPersistentEntity<T> persistentEntity;

    public MappingElasticsearchEntityInformation(ElasticsearchPersistentEntity<T> persistentEntity) {
        super(persistentEntity);
        this.persistentEntity = persistentEntity;
    }

    @Override
    public String getIdAttribute() {
        return persistentEntity.getRequiredIdProperty().getFieldName();
    }

    @Override
    public IndexCoordinates getIndexCoordinates() {
        return persistentEntity.getIndexCoordinates();
    }

    @Override
    public Long getVersion(T entity) {
        ElasticsearchPersistentProperty property = persistentEntity.getVersionProperty();
        try {
            return property != null ? (Long) persistentEntity.getPropertyAccessor(entity).getProperty(property) : null;
        } catch (Exception e) {
            throw new IllegalStateException("failed to load version field", e);
        }
    }

    @Override
    public VersionType getVersionType() {
        return persistentEntity.getVersionType();
    }
}
