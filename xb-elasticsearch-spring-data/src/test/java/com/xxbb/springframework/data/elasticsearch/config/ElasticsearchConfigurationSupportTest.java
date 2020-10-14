package com.xxbb.springframework.data.elasticsearch.config;

import com.xxbb.springframework.data.elasticsearch.annotations.Document;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.ClassUtils;

import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ElasticsearchConfigurationSupportTest {

    @Test
    public void usesConfigClassPackageAsBaseMappingPackage() {
        ElasticsearchConfigurationSupport configuration = new StubConfig();
        assertThat(configuration.getMappingBasePackages().contains(ClassUtils.getPackageName(StubConfig.class)));
        assertThat(configuration.getInitialEntitySet()).contains(Entity.class);
    }

    @Test
    public void doesNotScanOnEmptyBasePackage() {
        ElasticsearchConfigurationSupport configuration = new StubConfig() {
            @Override
            protected Collection<String> getMappingBasePackages() {
                return Collections.emptySet();
            }
        };

        assertThat(configuration.getInitialEntitySet()).isEmpty();
    }

    @Test
    public void containsMappingContext() {
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(StubConfig.class);
        assertThat(context.getBean(SimpleElasticsearchMappingContext.class)).isNotNull();
    }

    @Test
    public void containsElasticsearchConverter() {
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(StubConfig.class);
        assertThat(context.getBean(ElasticsearchConverter.class)).isNotNull();
    }

    @Test
    public void restConfigContainsElasticsearchTemplate() {
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(RestConfig.class);
        assertThat(context.getBean(ElasticsearchRestTemplate.class)).isNotNull();
    }

    @Test
    public void restConfigContainsElasticsearchOperationsByNameAndAlias() {
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(RestConfig.class);
        assertThat(context.getBean("elasticsearchOperations")).isNotNull();
        assertThat(context.getBean("elasticsearchTemplate")).isNotNull();
    }

    @Configuration
    static class StubConfig extends ElasticsearchConfigurationSupport{ }

    @Configuration
    static class RestConfig extends AbstractElasticsearchConfiguration {

        @Override
        public RestHighLevelClient elasticsearchClient() {
            return mock(RestHighLevelClient.class);
        }
    }

    @Configuration
    static class EntityMappingConfig extends ElasticsearchConfigurationSupport {
    }

    @Document(indexName = "config-support-test")
    static class Entity {}
}