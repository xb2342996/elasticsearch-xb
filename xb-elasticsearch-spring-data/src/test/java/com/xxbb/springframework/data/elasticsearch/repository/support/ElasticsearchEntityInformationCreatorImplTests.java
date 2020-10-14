package com.xxbb.springframework.data.elasticsearch.repository.support;

import com.xxbb.springframework.data.elasticsearch.annotations.Document;
import com.xxbb.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import com.xxbb.springframework.data.elasticsearch.core.query.StringQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mapping.MappingException;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ElasticsearchEntityInformationCreatorImplTests {
    ElasticsearchEntityInformationCreatorImpl entityInformationCreator;

    @BeforeEach
    public void before() {
        SimpleElasticsearchMappingContext context = new SimpleElasticsearchMappingContext();
        Set<Class<?>> entities = new HashSet<>();
        entities.add(EntityNoId.class);
        context.setInitialEntitySet(entities);
        entityInformationCreator = new ElasticsearchEntityInformationCreatorImpl(context);
    }

    @Test
    public void shouldThrowMappingExceptionOnMissingEntity() {
        assertThatThrownBy(() -> entityInformationCreator.getEntityInformation(String.class)).isInstanceOf(MappingException.class);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionOnMissingIdAnnotation() {
        assertThatThrownBy(() -> entityInformationCreator.getEntityInformation(EntityNoId.class)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("No Id property found");
    }

    @Document(indexName = "whatever")
    static class EntityNoId {

    }
}
