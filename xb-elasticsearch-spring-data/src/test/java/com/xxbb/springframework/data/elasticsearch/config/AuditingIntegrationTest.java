package com.xxbb.springframework.data.elasticsearch.config;

import com.xxbb.springframework.data.elasticsearch.core.event.BeforeConvertCallback;
import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import com.xxbb.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.annotation.*;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AuditingIntegrationTest {
    public static AuditorAware<String> auditorProvider() {
        return new AuditorAware<String>() {
            int count = 0;
            @Override
            public Optional<String> getCurrentAuditor() {
                return Optional.of("Auditor " + (++count));
            }
        };
    }

    @Autowired ApplicationContext applicationContext;
    
    @Test
    public void shouldEnableAuditingAndSetAuditingDates() throws InterruptedException{
        SimpleElasticsearchMappingContext mappingContext = applicationContext.getBean(SimpleElasticsearchMappingContext.class);
        mappingContext.getPersistentEntity(Entity.class);

        EntityCallbacks callbacks = EntityCallbacks.create(applicationContext);
        Entity entity = new Entity();
        entity.setId("1");
        entity = callbacks.callback(BeforeConvertCallback.class, entity, IndexCoordinates.of("index"));

        assertThat(entity.getCreated()).isNotNull();
        assertThat(entity.getModified()).isEqualTo(entity.created);
        assertThat(entity.getCreateBy()).isEqualTo("Auditor 1");
        assertThat(entity.getModifiedBy()).isEqualTo("Auditor 1");

        Thread.sleep(10);

        entity = callbacks.callback(BeforeConvertCallback.class, entity, IndexCoordinates.of("index"));

        assertThat(entity.getCreated()).isNotNull();
        assertThat(entity.getModified()).isNotEqualTo(entity.created);
        assertThat(entity.getCreateBy()).isEqualTo("Auditor 1");
        assertThat(entity.getModifiedBy()).isEqualTo("Auditor 2");
    }


    static class Entity implements Persistable<String> {
        @Nullable
        @Id
        private String id;
        @Nullable
        @CreatedDate
        private LocalDateTime created;
        @Nullable
        @LastModifiedDate
        private LocalDateTime modified;
        @Nullable
        @CreatedBy
        private String createBy;
        @Nullable
        @LastModifiedBy
        private String modifiedBy;

        @Override
        @Nullable
        public String getId() {
            return id;
        }

        public void setId(@Nullable String id) {
            this.id = id;
        }

        @Nullable
        public LocalDateTime getCreated() {
            return created;
        }

        public void setCreated(@Nullable LocalDateTime created) {
            this.created = created;
        }

        @Nullable
        public LocalDateTime getModified() {
            return modified;
        }

        public void setModified(@Nullable LocalDateTime modified) {
            this.modified = modified;
        }

        @Nullable
        public String getCreateBy() {
            return createBy;
        }

        public void setCreateBy(@Nullable String createBy) {
            this.createBy = createBy;
        }

        @Nullable
        public String getModifiedBy() {
            return modifiedBy;
        }

        public void setModifiedBy(@Nullable String modifiedBy) {
            this.modifiedBy = modifiedBy;
        }

        @Override
        public boolean isNew() {
            return id == null || (created == null && createBy == null);
        }
    }
}
