package com.xxbb.springframework.data.elasticsearch.core.mapping;

import com.xxbb.springframework.data.elasticsearch.core.document.Document;
import org.elasticsearch.index.VersionType;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.lang.Nullable;

public interface ElasticsearchPersistentEntity<T> extends PersistentEntity<T, ElasticsearchPersistentProperty> {
    IndexCoordinates getIndexCoordinates();

    short getShards();

    short getReplicas();

    boolean isUseServerConfiguration();

    @Nullable
    String getRefreshInterval();

    @Nullable
    String getIndexStoreType();

    @Override
    ElasticsearchPersistentProperty getVersionProperty();

    @Nullable
    String getParentType();

    @Nullable
    String settingPath();

    @Nullable
    VersionType getVersionType();

    boolean isCreateIndexAndMapping();

    boolean hasScoreProperty();

    @Nullable
    ElasticsearchPersistentProperty getScoreProperty();

    @Nullable
    ElasticsearchPersistentProperty getPersistentPropertyWithFieldName(String fieldName);

    boolean hasSeqNoPrimaryTermProperty();

    boolean hasJoinFieldProperty();

    @Nullable
    ElasticsearchPersistentProperty getSeqNoPrimaryTermProperty();

    @Nullable
    ElasticsearchPersistentProperty getJoinFieldPorperty();

    default ElasticsearchPersistentProperty getRequiredSeqNoPrimaryTermProperty() {
        ElasticsearchPersistentProperty property = this.getSeqNoPrimaryTermProperty();
        if (property != null) {
            return property;
        } else {
            throw new IllegalStateException(String.format("Required SeqNoPrimaryTerm property not found for %s", this.getType()));
        }
    }
    Document getDefaultSettings();
}
