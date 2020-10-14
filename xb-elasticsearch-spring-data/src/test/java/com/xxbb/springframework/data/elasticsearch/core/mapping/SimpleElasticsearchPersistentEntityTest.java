package com.xxbb.springframework.data.elasticsearch.core.mapping;

import com.xxbb.springframework.data.elasticsearch.annotations.Field;
import com.xxbb.springframework.data.elasticsearch.annotations.Score;
import com.xxbb.springframework.data.elasticsearch.core.query.SeqNoPrimaryTerm;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class SimpleElasticsearchPersistentEntityTest {

    @Test
    public void shouldThrowExceptionGivenVersionPropertyIsNotLong() {

        TypeInformation typeInformation = ClassTypeInformation.from(EntityWithWrongVersionType.class);
        SimpleElasticsearchPersistentEntity<EntityWithWrongVersionType> entity = new SimpleElasticsearchPersistentEntity<>(typeInformation);

        assertThatThrownBy(() -> {
            SimpleElasticsearchPersistentProperty property = createProperty(entity, "version");
        }).isInstanceOf(MappingException.class);
    }

    @Test
    public void shouldThrowExceptionGivenMultipleVersionPropertyAsPresent() {
        TypeInformation typeInformation = ClassTypeInformation.from(EntityWithMultipleVersionField.class);
        SimpleElasticsearchPersistentEntity<EntityWithMultipleVersionField> entity = new SimpleElasticsearchPersistentEntity<>(typeInformation);

        SimpleElasticsearchPersistentProperty property1 = createProperty(entity, "version1");
        SimpleElasticsearchPersistentProperty property2 = createProperty(entity, "version2");

        entity.addPersistentProperty(property1);

        assertThatThrownBy(() -> {
            entity.addPersistentProperty(property2);
        }).isInstanceOf(MappingException.class);
    }

    @Test
    public void shouldFindProperiesByMappedName() {
        SimpleElasticsearchMappingContext mappingContext = new SimpleElasticsearchMappingContext();
        SimpleElasticsearchPersistentEntity<?> persistentEntity = mappingContext.getRequiredPersistentEntity(FieldNameEntity.class);

        ElasticsearchPersistentProperty property = persistentEntity.getPersistentPropertyWithFieldName("renamed-field");

        assertThat(property).isNotNull();
        assertThat(property.getName()).isEqualTo("renameField");
        assertThat(property.getFieldName()).isEqualTo("renamed-field");
    }

    @Test
    public void shouldReportThatThereisNoSeqNoPrimaryTermReportyWhenNoSuchProperty() {
        TypeInformation typeInformation = ClassTypeInformation.from(EntityWithoutSeqNoPrimaryTerm.class);
        SimpleElasticsearchPersistentEntity<EntityWithoutSeqNoPrimaryTerm> entity = new SimpleElasticsearchPersistentEntity<>(typeInformation);
        assertThat(entity.hasSeqNoPrimaryTermProperty()).isFalse();
    }

    @Test
    public void shouldReportThatThereIsSeqNoPrimaryTermPropertyWhenThereIsSuchProperty() {
        TypeInformation typeInformation = ClassTypeInformation.from(EntityWithSeqNoPrimaryTerm.class);
        SimpleElasticsearchPersistentEntity<EntityWithSeqNoPrimaryTerm> entity = new SimpleElasticsearchPersistentEntity<>(typeInformation);

        entity.addPersistentProperty(createProperty(entity, "seqNoPrimaryTerm"));
        assertThat(entity.hasSeqNoPrimaryTermProperty()).isTrue();
    }

    @Test
    public void shouldReturnSeqNoPrimaryTermPropertyWhenThereIsSuchProperty() {
        TypeInformation typeInformation = ClassTypeInformation.from(EntityWithSeqNoPrimaryTerm.class);
        SimpleElasticsearchPersistentEntity<EntityWithSeqNoPrimaryTerm> entity = new SimpleElasticsearchPersistentEntity<>(typeInformation);
        entity.addPersistentProperty(createProperty(entity, "seqNoPrimaryTerm"));
        EntityWithSeqNoPrimaryTerm instance = new EntityWithSeqNoPrimaryTerm();
        SeqNoPrimaryTerm seqNoPrimaryTerm = new SeqNoPrimaryTerm(1, 2);
        ElasticsearchPersistentProperty property = entity.getSeqNoPrimaryTermProperty();
        entity.getPropertyAccessor(instance).setProperty(property, seqNoPrimaryTerm);

        assertThat(instance.seqNoPrimaryTerm).isSameAs(seqNoPrimaryTerm);
    }

    @Test
    public void shouldNotAllowMoreThanOneSeqNoPrimaryTermProperties() {
        TypeInformation typeInformation = ClassTypeInformation.from(EntityWithSeqNoPrimaryTerm.class);
        SimpleElasticsearchPersistentEntity<EntityWithSeqNoPrimaryTerm> entity = new SimpleElasticsearchPersistentEntity<>(typeInformation);
        entity.addPersistentProperty(createProperty(entity, "seqNoPrimaryTerm"));

        assertThatThrownBy(()->
            entity.addPersistentProperty(createProperty(entity, "SeqNoPrimaryTerm2")))
                .isInstanceOf(MappingException.class);
    }

    private static SimpleElasticsearchPersistentProperty createProperty(SimpleElasticsearchPersistentEntity<?> entity, String field) {
        TypeInformation<?> typeInformation = entity.getTypeInformation();
        Property property = Property.of(typeInformation, ReflectionUtils.findField(entity.getType(), field));
        return new SimpleElasticsearchPersistentProperty(property, entity, SimpleTypeHolder.DEFAULT);
    }

    private static class EntityWithWrongVersionType {
        @Nullable @Version private String version;

        @Nullable
        public String getVersion() {
            return version;
        }

        public void setVersion(@Nullable String version) {
            this.version = version;
        }
    }

    private static  class EntityWithMultipleVersionField {
        @Nullable @Version private Long version1;
        @Nullable @Version private Long version2;

        @Nullable
        public Long getVersion1() {
            return version1;
        }

        public void setVersion1(@Nullable Long version1) {
            this.version1 = version1;
        }

        @Nullable
        public Long getVersion2() {
            return version2;
        }

        public void setVersion2(@Nullable Long version2) {
            this.version2 = version2;
        }
    }

    static class TwoScoreProperty {
        @Score
        float first;
        @Score
        float second;
    }

    private static class FieldNameEntity {
        @Nullable
        @Id
        String id;
        @Nullable
        @Field(name = "renamed-field")
        private String renameField;
    }

    private static class EntityWithoutSeqNoPrimaryTerm {}

    private static class EntityWithSeqNoPrimaryTerm {
        private SeqNoPrimaryTerm seqNoPrimaryTerm;
        private SeqNoPrimaryTerm SeqNoPrimaryTerm2;
    }
}