package com.xxbb.springframework.data.elasticsearch.core.event;

import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import com.xxbb.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.Ordered;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuditingEntityCallbackTest {
    IsNewAwareAuditingHandler handler;
    AuditingEntityCallback callback;

    @BeforeEach
    void setup() {
        SimpleElasticsearchMappingContext context = new SimpleElasticsearchMappingContext();
        context.getPersistentEntity(Sample.class);
        handler = spy(new IsNewAwareAuditingHandler(PersistentEntities.of(context)));
        callback = new AuditingEntityCallback(() -> handler);
    }

    @Test
    public void shouldThrowExceptionOnNullFactory() {
        assertThatIllegalArgumentException().isThrownBy(() -> new AuditingEntityCallback(null));
    }

    @Test
    public void shouldHaveOrder100() {
        assertThat(callback).isInstanceOf(Ordered.class);
        assertThat(callback.getOrder()).isEqualTo(100);
    }

    @Test
    public void shouldCallhandler() {
        Sample sample = new Sample();
        sample.setId("42");
        callback.onBeforeConvert(sample, IndexCoordinates.of("index"));

        verify(handler).markAudited(eq(sample));
    }

    @Test
    public void shouldReturnObjectFromHandler() {
        Sample sample1 = new Sample();
        sample1.setId("1");
        Sample sample2 = new Sample();
        sample2.setId("2");

        doReturn(sample2).when(handler).markAudited(any());

        Sample result = (Sample) callback.onBeforeConvert(sample1, IndexCoordinates.of("index"));

        assertThat(result).isSameAs(sample2);

    }

    static class Sample {
        @Nullable
        @Id
        private String id;

        @Nullable
        @CreatedBy
        private String createdBy;

        @Nullable
        @CreatedDate
        private LocalDateTime createDate;

        @Nullable
        @LastModifiedDate
        private LocalDateTime modified;

        @Nullable
        public String getId() {
            return id;
        }

        public void setId(@Nullable String id) {
            this.id = id;
        }

        @Nullable
        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(@Nullable String createdBy) {
            this.createdBy = createdBy;
        }

        @Nullable
        public LocalDateTime getCreateDate() {
            return createDate;
        }

        public void setCreateDate(@Nullable LocalDateTime createDate) {
            this.createDate = createDate;
        }

        @Nullable
        public LocalDateTime getModified() {
            return modified;
        }

        public void setModified(@Nullable LocalDateTime modified) {
            this.modified = modified;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Sample sample = (Sample) o;

            if (id != null ? !id.equals(sample.id) : sample.id != null)
                return false;

            if (createdBy != null ? !createdBy.equals(sample.createdBy) : sample.createdBy != null)
                return false;

            if (createDate != null ? !createDate.equals(sample.createDate) : sample.createDate != null)
                return false;

            return modified != null ? !modified.equals(sample.modified) : sample.modified != null;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (createdBy != null ? createdBy.hashCode() : 0);
            result = 31 * result + (createDate != null ? createDate.hashCode() : 0);
            result = 31 * result + (modified != null ? modified.hashCode() : 0);

            return result;
        }

        @Override
        public String toString() {
            return "Sample{" +
                    "id='" + id + '\'' +
                    ", createdBy='" + createdBy + '\'' +
                    ", createDate=" + createDate +
                    ", modified=" + modified +
                    '}';
        }
    }
}
