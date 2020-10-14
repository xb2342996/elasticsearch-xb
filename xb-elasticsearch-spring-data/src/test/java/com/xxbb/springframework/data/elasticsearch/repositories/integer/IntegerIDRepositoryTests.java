package com.xxbb.springframework.data.elasticsearch.repositories.integer;

import com.xxbb.springframework.data.elasticsearch.annotations.Document;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.xxbb.springframework.data.elasticsearch.core.IndexOperations;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.ElasticsearchRestTemplateConfiguration;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.SpringIntegrationTest;
import com.xxbb.springframework.data.elasticsearch.repositories.doubleid.DoubleIDRepositoryTests;
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

import java.util.Arrays;
import java.util.Optional;

import static com.xxbb.springframework.data.elasticsearch.utils.IdGenerator.nextIdAsInt;
import static org.assertj.core.api.Assertions.assertThat;

@SpringIntegrationTest
@ContextConfiguration(classes = { IntegerIDRepositoryTests.Config.class })
public class IntegerIDRepositoryTests {
    @Configuration
    @Import({ElasticsearchRestTemplateConfiguration.class})
    @EnableElasticsearchRepositories(considerNestedRepositories = true)
    static class Config {}

    @Autowired
    private IntegerIDRepository repository;

    @Autowired
    ElasticsearchOperations operations;
    private IndexOperations indexOperations;


    @BeforeEach
    public void before() {
        indexOperations = operations.indexOps(IntegerEntity.class);
        IndexInitializer.init(indexOperations);
    }

    @AfterEach
    public void after() {
        indexOperations.delete();
    }

    @Test
    public void shouldDoBulkIndexDocument() {

        Integer documentId = nextIdAsInt();
        IntegerEntity sampleEntity1 = new IntegerEntity();
        sampleEntity1.setId(documentId);
        sampleEntity1.setMessage("some message");
        sampleEntity1.setVersion(System.currentTimeMillis());

        Integer documentId2 = nextIdAsInt();
        IntegerEntity sampleEntity2 = new IntegerEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("some message");
        sampleEntity2.setVersion(System.currentTimeMillis());

        repository.saveAll(Arrays.asList(sampleEntity1, sampleEntity2));

        Optional<IntegerEntity> entity1FromEs = repository.findById(documentId);
        assertThat(entity1FromEs).isPresent();

        Optional<IntegerEntity> entity1FromEs2 = repository.findById(documentId2);
        assertThat(entity1FromEs2).isPresent();
    }

    @Test
    public void shouldSaveDocument() {
        Integer documentId = nextIdAsInt();
        IntegerEntity sampleEntity1 = new IntegerEntity();
        sampleEntity1.setId(documentId);
        sampleEntity1.setMessage("some message");
        sampleEntity1.setVersion(System.currentTimeMillis());

        repository.save(sampleEntity1);

        Optional<IntegerEntity> entityFromEs = repository.findById(documentId);
        assertThat(entityFromEs).isPresent();
    }

    @Data
    @Document(indexName = "test-index-double-keyed-entity", replicas = 0, refreshInterval = "-1")
    static class IntegerEntity {
        @Id
        private Integer id;
        private String name;
        private String message;
        @Version
        private Long version;
    }

    interface IntegerIDRepository extends ElasticsearchRepository<IntegerEntity, Integer> {}
}
