package com.xxbb.springframework.data.elasticsearch.repository.support;

import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.elasticsearch.index.VersionType;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.lang.Nullable;

public interface ElasticsearchEntityInformation<T, ID> extends EntityInformation<T, ID> {
    String getIdAttribute();

    IndexCoordinates getIndexCoordinates();

    @Nullable
    Long getVersion(T entity);

    @Nullable
    VersionType getVersionType();


}
