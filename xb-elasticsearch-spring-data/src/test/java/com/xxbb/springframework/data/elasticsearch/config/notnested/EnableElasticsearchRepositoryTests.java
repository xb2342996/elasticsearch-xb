package com.xxbb.springframework.data.elasticsearch.config.notnested;

import com.xxbb.springframework.data.elasticsearch.annotations.Document;
import com.xxbb.springframework.data.elasticsearch.annotations.Field;
import com.xxbb.springframework.data.elasticsearch.annotations.FieldType;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.xxbb.springframework.data.elasticsearch.core.IndexOperations;
import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.ElasticsearchRestTemplateConfiguration;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.SpringIntegrationTest;
import com.xxbb.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import com.xxbb.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import com.xxbb.springframework.data.elasticsearch.utils.IndexInitializer;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.repository.Repository;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringIntegrationTest
@ContextConfiguration(classes = { EnableElasticsearchRepositoryTests.Config.class })
public class EnableElasticsearchRepositoryTests implements ApplicationContextAware {
    @Nullable
    ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Configuration
    @Import({ElasticsearchRestTemplateConfiguration.class })
    @EnableElasticsearchRepositories
    static class Config{}

    @Autowired
    ElasticsearchOperations operations;
    private IndexOperations indexOperations;

    @Autowired private SampleElasticsearchRepository repository;
    @Autowired(required = false)
    private SampleRepository nestedRepository;

    interface SampleRepository extends Repository<SampleEntity, Long> {}

    @BeforeEach
    public void before() {
        indexOperations = operations.indexOps(SampleEntity.class);
        IndexInitializer.init(indexOperations);
    }

    @AfterEach
    public void after() {
        operations.indexOps(IndexCoordinates.of("text-index-sample-config-not-nested")).delete();
        operations.indexOps(IndexCoordinates.of("text-index-uuid-keyed-config-not-nested")).delete();
    }

    @Test
    public void bootstrapsRepository() {
        assertThat(repository).isNotNull();
    }

    @Test
    public void shouldScanSeletedPackage() {
        String[] beanNamesForType = context.getBeanNamesForType(ElasticsearchRepository.class);

        assertThat(beanNamesForType).containsExactlyInAnyOrder("sampleElasticsearchRepository");
    }

    @Test
    public void hasNotNestedRepository() {
        assertThat(nestedRepository).isNull();
    }

    @Data
    @Document(indexName = "test-index-sample-config-not-nested", replicas = 0, refreshInterval = "-1")
    static class SampleEntity {
        @Id
        private String id;
        @Field(type = FieldType.Text, store = true, fielddata = true)
        private String type;
        @Field(type = FieldType.Text, store = true, fielddata = true)
        private String message;
        private int rate;

        private boolean available;
        private String highlightedMessage;
        @Version
        private Long version;
    }

}
