package com.xxbb.springframework.data.elasticsearch.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.type.AnnotationMetadata;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


@ExtendWith(MockitoExtension.class)
public class ElasticsearchAuditingRegistrarTest {
    ElasticsearchAuditingRegistrar register = new ElasticsearchAuditingRegistrar();

    @Mock
    AnnotationMetadata metadata;
    @Mock
    BeanDefinitionRegistry registry;

    @Test
    public void rejectsNullAnnotationMetadata() {
        assertThatIllegalArgumentException().isThrownBy(() -> register.registerBeanDefinitions(null, registry));
    }

    @Test
    public void rejectsNullBeanDefinitionRegistry() {
        assertThatIllegalArgumentException().isThrownBy(() -> register.registerBeanDefinitions(metadata, null));
    }
}