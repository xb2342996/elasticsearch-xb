package com.xxbb.springframework.data.elasticsearch.repositories.complex.custommethod.autowiring;

import com.xxbb.springframework.data.elasticsearch.annotations.Document;
import com.xxbb.springframework.data.elasticsearch.annotations.Field;
import com.xxbb.springframework.data.elasticsearch.annotations.FieldType;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.xxbb.springframework.data.elasticsearch.core.IndexOperations;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.ElasticsearchRestTemplateConfiguration;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.SpringIntegrationTest;
import com.xxbb.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import com.xxbb.springframework.data.elasticsearch.utils.IndexInitializer;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.Id;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringIntegrationTest
@ContextConfiguration(classes = {ComplexCustomMethodRepositoryTests.Config.class})
public class ComplexCustomMethodRepositoryTests {
    @Configuration
    @Import({ElasticsearchRestTemplateConfiguration.class })
    @EnableElasticsearchRepositories(considerNestedRepositories = true)
    static class Config{}

    @Autowired
    private ComplexElasticsearchRepository complexRepository;

    @Autowired
    ElasticsearchOperations operations;

    private IndexOperations indexOperations;

    @BeforeEach
    public void before() {
        indexOperations = operations.indexOps(SampleEntity.class);
        IndexInitializer.init(indexOperations);
    }

    @AfterEach
    public void after() {
        indexOperations.delete();
    }

    @Test
    public void shouldExecuteComplexCustomMethod() {
        String result = complexRepository.doSomethingSpecial();

        assertThat(result).isEqualTo("1 == 1");
    }

    @Data
    @Document(indexName = "test-index-sample-repositories-complex-custommethod-autowring", replicas = 0, refreshInterval = "-1")
    static class SampleEntity {
        @Id
        private String id;
        @Field(type = FieldType.Text, store = true, fielddata = true)
        private String type;
        @Field(type = FieldType.Text, store = true, fielddata = true)
        private String message;
    }
}
