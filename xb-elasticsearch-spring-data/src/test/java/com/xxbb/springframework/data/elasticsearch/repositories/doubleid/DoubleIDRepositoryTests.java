package com.xxbb.springframework.data.elasticsearch.repositories.doubleid;

import static com.xxbb.springframework.data.elasticsearch.utils.IdGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.xxbb.springframework.data.elasticsearch.annotations.Document;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.xxbb.springframework.data.elasticsearch.core.IndexOperations;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.ElasticsearchRestTemplateConfiguration;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.SpringIntegrationTest;
import com.xxbb.springframework.data.elasticsearch.repository.ElasticsearchRepository;
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
import org.springframework.data.annotation.Version;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@SpringIntegrationTest
@ContextConfiguration(classes = { DoubleIDRepositoryTests.Config.class })
public class DoubleIDRepositoryTests {

    @Configuration
    @Import({ElasticsearchRestTemplateConfiguration.class})
    @EnableElasticsearchRepositories(considerNestedRepositories = true)
    static class Config {}

    @Autowired
    private DoubleIDRepository repository;

    @Autowired
    ElasticsearchOperations operations;
    private IndexOperations indexOperations;


    @BeforeEach
    public void before() {
        indexOperations = operations.indexOps(DoubleEntity.class);
        IndexInitializer.init(indexOperations);
    }

    @AfterEach
    public void after() {
        indexOperations.delete();
    }

    @Test
    public void shouldDoBulkIndexDocument() {

        Double documentId = nextIdAsDouble();
        DoubleEntity sampleEntity1 = new DoubleEntity();
        sampleEntity1.setId(documentId);
        sampleEntity1.setMessage("some message");
        sampleEntity1.setVersion(System.currentTimeMillis());

        Double documentId2 = nextIdAsDouble();
        DoubleEntity sampleEntity2 = new DoubleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("some message");
        sampleEntity2.setVersion(System.currentTimeMillis());

        repository.saveAll(Arrays.asList(sampleEntity1, sampleEntity2));

        Optional<DoubleEntity> entity1FromEs = repository.findById(documentId);
        assertThat(entity1FromEs).isPresent();

        Optional<DoubleEntity> entity1FromEs2 = repository.findById(documentId2);
        assertThat(entity1FromEs2).isPresent();
    }

    @Test
    public void shouldSaveDocument() {
        Double documentId = nextIdAsDouble();
        DoubleEntity sampleEntity1 = new DoubleEntity();
        sampleEntity1.setId(documentId);
        sampleEntity1.setMessage("some message");
        sampleEntity1.setVersion(System.currentTimeMillis());

        repository.save(sampleEntity1);

        Optional<DoubleEntity> entityFromEs = repository.findById(documentId);
        assertThat(entityFromEs).isPresent();
    }

    @Data
    @Document(indexName = "test-index-double-keyed-entity", replicas = 0, refreshInterval = "-1")
    static class DoubleEntity {
        @Id
        private Double id;
        private String name;
        private String message;
        @Version
        private Long version;
    }

    interface DoubleIDRepository extends ElasticsearchRepository<DoubleEntity, Double> {}
}
