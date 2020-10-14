package com.xxbb.springframework.data.elasticsearch.core;

import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import com.xxbb.springframework.data.elasticsearch.core.query.SeqNoPrimaryTerm;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class EntityOperations {
    private static final String ID_FIELD = "id";

    private final MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> context;

    public EntityOperations(MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> context) {
        Assert.notNull(context, "context must not be null");
        this.context = context;
    }

    interface Entity<T> {
        @Nullable
        Object getId();

        default boolean isVersionedEntity() {
            return false;
        }

        @Nullable
        Object getVersion();

        T getBean();

        boolean isNew();

        @Nullable
        ElasticsearchPersistentEntity<?> getPersistentEntity();

        default ElasticsearchPersistentEntity<?> getRequiredPersistentEntity() {
            ElasticsearchPersistentEntity<?> persistentEntity = getPersistentEntity();
            if (persistentEntity == null) {
                throw new IllegalStateException("No ElasticsearchPersistentEntity avaliable for this entity");
            }
            return persistentEntity;
        }
    }

    interface AdaptibleEntity<T> extends Entity<T> {
        @Nullable
        T populateIdIfNecessary(@Nullable Object id);

        T initializeVersionProperty();

        T incrementVersion();

        @Override
        @Nullable
        Number getVersion();

        boolean hasSeqNoPrimaryTerm();

        @Nullable
        SeqNoPrimaryTerm getSeqNoPrimaryTerm();

        @Nullable
        String getRouting();
    }
}
