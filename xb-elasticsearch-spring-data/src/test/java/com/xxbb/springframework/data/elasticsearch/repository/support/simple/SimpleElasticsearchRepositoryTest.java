package com.xxbb.springframework.data.elasticsearch.repository.support.simple;

import static com.xxbb.springframework.data.elasticsearch.utils.IdGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import com.xxbb.springframework.data.elasticsearch.annotations.Document;
import com.xxbb.springframework.data.elasticsearch.annotations.Field;
import com.xxbb.springframework.data.elasticsearch.annotations.FieldType;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.xxbb.springframework.data.elasticsearch.core.IndexOperations;
import com.xxbb.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import com.xxbb.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.ElasticsearchRestTemplateConfiguration;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.SpringIntegrationTest;
import com.xxbb.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import com.xxbb.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import com.xxbb.springframework.data.elasticsearch.utils.IndexInitializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.StreamUtils;
import org.springframework.test.context.ContextConfiguration;

import java.util.*;
import java.util.stream.Collectors;

@SpringIntegrationTest
@ContextConfiguration(classes = { SimpleElasticsearchRepositoryTest.Config.class})
public class SimpleElasticsearchRepositoryTest {
    @Configuration
    @Import({ElasticsearchRestTemplateConfiguration.class})
    @EnableElasticsearchRepositories(
            basePackages = {"com.xxbb.springframework.data.elasticsearch.repository.support.simple"},
            considerNestedRepositories = true)
    static class Config {}

    @Autowired
    private SampleElasticsearchRepository repository;
    @Autowired
    private ElasticsearchOperations operations;
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
    public void shouldDoBulkIndexDocument() {
        String document1 = nextIdAsString();
        SampleEntity entity1 = new SampleEntity();
        entity1.setId(document1);
        entity1.setMessage("some messages");
        entity1.setVersion(System.currentTimeMillis());

        String document2 = nextIdAsString();
        SampleEntity entity2 = new SampleEntity();
        entity2.setId(document2);
        entity2.setMessage("some messages");
        entity2.setVersion(System.currentTimeMillis());

        repository.saveAll(Arrays.asList(entity1, entity2));


        Optional<SampleEntity> entity1FromElasticsearch = repository.findById(document1);
        assertThat(entity1FromElasticsearch.isPresent()).isTrue();

        Optional<SampleEntity> entity2FromElasticsearch = repository.findById(document2);
        assertThat(entity2FromElasticsearch.isPresent()).isTrue();
    }

    @Test
    public void shouldSaveDocument() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("some message");
        sampleEntity.setVersion(System.currentTimeMillis());

        repository.save(sampleEntity);

        Optional<SampleEntity> entityFromES = repository.findById(documentId);
        assertThat(entityFromES).isPresent();
    }

    @Test
    public void throwExceptionWhenTryingToInsertWithVersionButWithoutId() {
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setMessage("some message");
        sampleEntity.setVersion(System.currentTimeMillis());

        assertThatThrownBy(() -> repository.save(sampleEntity)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void shouldFindDocumentById() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("some message");
        sampleEntity.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity);

        Optional<SampleEntity> entity = repository.findById(documentId);

        assertThat(entity).isPresent();
        assertThat(entity.get()).isEqualTo(sampleEntity);
    }

    @Test
    public void shouldReturnCountOfDocuments() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("some message");
        sampleEntity.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity);

        Long count = repository.count();
        assertThat(count).isGreaterThanOrEqualTo(1L);
    }

    @Test
    public void shouldFindAllDocuments() {
        Iterable<SampleEntity> results = repository.findAll();
        assertThat(results).isNotNull();
    }

    @Test
    public void shouldDeleteDocument() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("some message");
        sampleEntity.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity);

        repository.deleteById(documentId);

        Optional<SampleEntity> entity = repository.findById(documentId);
        assertThat(entity).isNotPresent();
    }

    @Test
    public void shouldSearchDocumentsGivenSearchQuery() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("some test message");
        sampleEntity.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity);

        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.termQuery("message", "test")).build();

        Page<SampleEntity> page = repository.search(query);

        assertThat(page).isNotNull();
        assertThat(page.getNumberOfElements()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void shouldSearchDocumentsGivenELasticsearchQuery() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("hello world");
        sampleEntity.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity);

        Page<SampleEntity> page = repository.search(QueryBuilders.termQuery("message", "world"), PageRequest.of(0, 50));

        assertThat(page).isNotNull();
        assertThat(page.getNumberOfElements()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void shouldFindAllByIdQuery() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("hello world");
        sampleEntity.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("hello world");
        sampleEntity2.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity2);

        Iterable<SampleEntity> entities = repository.findAllById(Arrays.asList(documentId, documentId2));

        assertThat(entities).isNotNull().hasSize(2);
    }

    @Test
    public void shouldSaveIterableEntities() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("hello world");
        sampleEntity.setVersion(System.currentTimeMillis());


        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("hello world");
        sampleEntity2.setVersion(System.currentTimeMillis());

        Iterable<SampleEntity> sampleEntities = Arrays.asList(sampleEntity, sampleEntity2);
        repository.saveAll(sampleEntities);

        Page<SampleEntity> entities = repository.search(QueryBuilders.termQuery("id", documentId), PageRequest.of(0, 50));
        assertThat(entities).isNotNull();
    }

    @Test
    public void shouldRetrunTrueGivenDocumentWithIdExists() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("hello world");
        sampleEntity.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity);

        boolean exist = repository.existsById(documentId);
        assertThat(exist).isTrue();
    }

    @Test
    public void shouldReturnFalseGivenDocumentWithIdDoesNotExist() {
        String documentId = nextIdAsString();

        boolean exist = repository.existsById(documentId);

        assertThat(exist).isFalse();
    }

    @Test
    public void shouldReturnResultsForGivenSearchQuery() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("hello world");
        sampleEntity.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity);

       NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.termQuery("id", documentId)).build();
       Page<SampleEntity> entities = repository.search(searchQuery);

       assertThat(entities.getTotalElements()).isEqualTo(1L);
    }

    @Test
    public void shouldDeleteAll() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("hello world");
        sampleEntity.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity);

        repository.deleteAll();

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery()).build();
        Page<SampleEntity> sampleEntities = repository.search(searchQuery);
        assertThat(sampleEntities.getTotalElements()).isEqualTo(0L);
    }

    @Test
    public void shouldDeleteById() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("hello world");
        sampleEntity.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity);

        long result = repository.deleteSampleEntityById(documentId);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.termQuery("id", documentId)).build();
        Page<SampleEntity> entities = repository.search(searchQuery);
        assertThat(entities.getTotalElements()).isEqualTo(0L);
        assertThat(result).isEqualTo(1L);
    }

    @Test
    public void shouldDeleteByMessageAndReturnList() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("hello world");
        sampleEntity.setVersion(System.currentTimeMillis());
        sampleEntity.setAvailable(true);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("hello world");
        sampleEntity2.setVersion(System.currentTimeMillis());
        sampleEntity2.setAvailable(true);


        String documentId3 = nextIdAsString();
        SampleEntity sampleEntity3 = new SampleEntity();
        sampleEntity3.setId(documentId3);
        sampleEntity3.setMessage("hello world");
        sampleEntity3.setAvailable(false);
        sampleEntity3.setVersion(System.currentTimeMillis());
        repository.saveAll(Arrays.asList(sampleEntity, sampleEntity2, sampleEntity3));

        List<SampleEntity> result = repository.deleteByAvailable(true);

        assertThat(result).hasSize(2);
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery()).build();
        Page<SampleEntity> sampleEntities = repository.search(searchQuery);
        assertThat(sampleEntities.getTotalElements()).isEqualTo(1L);
    }

    @Test
    public void shouldDeleteByListForMessage() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("hello world 1");
        sampleEntity.setVersion(System.currentTimeMillis());

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("hello world 2");
        sampleEntity2.setVersion(System.currentTimeMillis());


        String documentId3 = nextIdAsString();
        SampleEntity sampleEntity3 = new SampleEntity();
        sampleEntity3.setId(documentId3);
        sampleEntity3.setMessage("hello world 3");
        sampleEntity3.setVersion(System.currentTimeMillis());
        repository.saveAll(Arrays.asList(sampleEntity, sampleEntity2, sampleEntity3));

        List<SampleEntity> result = repository.deleteByMessage("hello world 3");

        assertThat(result).hasSize(1);
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery()).build();
        Page<SampleEntity> sampleEntities = repository.search(searchQuery);
        assertThat(sampleEntities.getTotalElements()).isEqualTo(2L);
    }

    @Test
    public void shouldDeleteByType() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("book");
        sampleEntity.setVersion(System.currentTimeMillis());

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setType("article");
        sampleEntity2.setVersion(System.currentTimeMillis());


        String documentId3 = nextIdAsString();
        SampleEntity sampleEntity3 = new SampleEntity();
        sampleEntity3.setId(documentId3);
        sampleEntity3.setType("image");
        sampleEntity3.setVersion(System.currentTimeMillis());
        repository.saveAll(Arrays.asList(sampleEntity, sampleEntity2, sampleEntity3));

        repository.deleteByType("article");

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery()).build();
        Page<SampleEntity> sampleEntities = repository.search(searchQuery);
        assertThat(sampleEntities.getTotalElements()).isEqualTo(2L);
    }

    @Test
    public void shouldDeleteEntity() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("hello world");
        sampleEntity.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity);

        repository.delete(sampleEntity);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(termQuery("id", documentId)).build();
        Page<SampleEntity> sampleEntities = repository.search(searchQuery);
        assertThat(sampleEntities.getTotalElements()).isEqualTo(0L);
    }

    @Test
    public void shouldReturnIterableEntities() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("hello world");
        sampleEntity.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("hello world");
        sampleEntity2.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity2);

        Iterable<SampleEntity> sampleEntities = repository.search(termQuery("id", documentId));

        assertThat(sampleEntities).isNotNull();
    }

    @Test
    public void shouldDeleteIterableEntities() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("hello world");
        sampleEntity.setVersion(System.currentTimeMillis());

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("hello world");
        sampleEntity2.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity2);

        Iterable<SampleEntity> sampleEntities = Arrays.asList(sampleEntity2, sampleEntity2);

        repository.deleteAll(sampleEntities);

        assertThat(repository.findById(documentId)).isNotPresent();
        assertThat(repository.findById(documentId2)).isNotPresent();
    }

    @Test
    public void shouldIndexEntity() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("hello world");
        sampleEntity.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity);

        Page<SampleEntity> entities = repository.search(termQuery("id", documentId), PageRequest.of(0, 50));
        assertThat(entities.getTotalElements()).isEqualTo(1L);
    }


    @Test
    public void shouldSortByGivenField() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("world");
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("hello");
        repository.save(sampleEntity2);

        Iterable<SampleEntity> sampleEntities = repository.findAll(Sort.by("message"));

        assertThat(sampleEntities).isNotNull();
    }

    @Test
    public void shouldIndexNotEmptyList() {
        List<SampleEntity> list = new ArrayList<>();
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("hello world");
        sampleEntity.setVersion(System.currentTimeMillis());
        list.add(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("hello world");
        sampleEntity2.setVersion(System.currentTimeMillis());
        list.add(sampleEntity2);

        Iterable<SampleEntity> savedEntities = repository.saveAll(list);
        assertThat(savedEntities).containsExactlyElementsOf(list);
    }

    @Test
    public void shouldNotFailOnIndexingEmptyList() {
        Iterable<SampleEntity> savedEntities = repository.saveAll(Collections.emptyList());

        assertThat(savedEntities).hasSize(0);
    }

    @Test
    public void shouldNotReturnNullValuesInFindAllById() {
        String documentId = "id-one";
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        repository.save(sampleEntity);

        String documentId2 = "id-two";
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        repository.save(sampleEntity2);

        String documentId3 = "id-three";
        SampleEntity sampleEntity3 = new SampleEntity();
        sampleEntity3.setId(documentId3);
        repository.save(sampleEntity3);

        Iterable<SampleEntity> allById = repository.findAllById(Arrays.asList("id-one", "does-not-exist", "id-two", "where-am-i", "id-three"));
        List<SampleEntity> results = StreamUtils.createStreamFromIterator(allById.iterator()).collect(Collectors.toList());

        assertThat(results).hasSize(3);
        assertThat(results.stream().map(SampleEntity::getId).collect(Collectors.toList())).containsExactlyInAnyOrder("id-one", "id-two", "id-three");
    }

    private static List<SampleEntity> createSampleEntitiesWithMessage(String message, int numberOfEntities) {
        List<SampleEntity> sampleEntities = new ArrayList<>();
        long idBase = (long) (Math.random() * 100);
        long versionBase = System.currentTimeMillis();

        for (int i = 0; i < numberOfEntities; i++) {
            String documentId = String.valueOf(idBase + i);
            SampleEntity entity = new SampleEntity();
            entity.setId(documentId);
            entity.setMessage(message);
            entity.setRate(2);
            entity.setVersion(versionBase + i);
            sampleEntities.add(entity);
        }
        return sampleEntities;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Document(indexName = "test-index-sample-repository", replicas = 0, refreshInterval = "-1")
    static class SampleEntity {
        @Id
        private String id;
        @Field(type = FieldType.Text, store = true, fielddata = true)
        private String type;
        @Field(type = FieldType.Text, store = true, fielddata = true)
        private String message;
        private int rate;
        private boolean available;
        @Version
        private Long version;
    }

    interface SampleElasticsearchRepository extends ElasticsearchRepository<SampleEntity, String> {
        long deleteSampleEntityById(String id);

        List<SampleEntity> deleteByAvailable(boolean available);
        List<SampleEntity> deleteByMessage(String message);

        void deleteByType(String type);
    }
}
