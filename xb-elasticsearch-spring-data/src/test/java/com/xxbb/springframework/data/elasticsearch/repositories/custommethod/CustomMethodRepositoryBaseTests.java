package com.xxbb.springframework.data.elasticsearch.repositories.custommethod;

import com.xxbb.springframework.data.elasticsearch.annotations.Document;
import com.xxbb.springframework.data.elasticsearch.annotations.Field;
import com.xxbb.springframework.data.elasticsearch.annotations.FieldType;
import com.xxbb.springframework.data.elasticsearch.annotations.Query;
import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.xxbb.springframework.data.elasticsearch.core.IndexOperations;
import com.xxbb.springframework.data.elasticsearch.core.SearchHit;
import com.xxbb.springframework.data.elasticsearch.core.SearchHits;
import com.xxbb.springframework.data.elasticsearch.junit.jupiter.SpringIntegrationTest;
import com.xxbb.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import com.xxbb.springframework.data.elasticsearch.utils.IndexInitializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.*;
import java.util.stream.Stream;

import static com.xxbb.springframework.data.elasticsearch.utils.IdGenerator.nextIdAsString;
import static org.assertj.core.api.Assertions.assertThat;

@SpringIntegrationTest
public abstract class CustomMethodRepositoryBaseTests {

    @Autowired
    private SampleCustomMethoRepository repository;
    @Autowired
    private SampleStreamingCustomMethodRepository streamingRepository;

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
    public void shouldExecuteCustomMethod() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("test");
        sampleEntity.setMessage("somemesage");
        repository.save(sampleEntity);

        Page<SampleEntity> page = repository.findByType("test", PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void shouldExecuteCustomMethodForNot() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("some");
        sampleEntity.setMessage("somemesage");
        repository.save(sampleEntity);

        Page<SampleEntity> page = repository.findByTypeNot("test", PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    public void shouldExecuteCustomMethodWithQuery() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("test");
        String searchTerm = "customQuery";
        sampleEntity.setMessage(searchTerm);
        repository.save(sampleEntity);

        Page<SampleEntity> page = repository.findByMessage(searchTerm.toLowerCase(), PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1L);
    }

    @Test
    public void shouldExecuteCustomMethodWithLessThan() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("test");
        sampleEntity.setRate(9);
        sampleEntity.setMessage("somemesage");
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setType("test");
        sampleEntity2.setRate(20);
        sampleEntity2.setMessage("somemesage");
        repository.save(sampleEntity2);

        Page<SampleEntity> page = repository.findByRateLessThan(10, PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    public void shouldExecuteCustomMethodWithBefore() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("test");
        sampleEntity.setRate(10);
        sampleEntity.setMessage("somemesage");
        repository.save(sampleEntity);

        Page<SampleEntity> page = repository.findByRateBefore(10, PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    public void shouldExecuteCustomMethodWithAfter() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("test");
        sampleEntity.setRate(10);
        sampleEntity.setMessage("somemesage");
        repository.save(sampleEntity);

        Page<SampleEntity> page = repository.findByRateAfter(10, PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    public void shouldExecuteCustomMethodWithLike() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("test");
        sampleEntity.setRate(10);
        sampleEntity.setMessage("foo");
        repository.save(sampleEntity);

        Page<SampleEntity> page = repository.findByMessageLike("fo", PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    public void shouldExecuteCustomMethodForStartingWith() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("test");
        sampleEntity.setRate(10);
        sampleEntity.setMessage("foo");
        repository.save(sampleEntity);

        Page<SampleEntity> page = repository.findByMessageStartingWith("fo", PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    public void shouldExecuteCustomMethodForEndingWith() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("test");
        sampleEntity.setRate(10);
        sampleEntity.setMessage("foo");
        repository.save(sampleEntity);

        Page<SampleEntity> page = repository.findByMessageEndingWith("o", PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    public void shouldExecuteCustomMethodForContains() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("test");
        sampleEntity.setRate(10);
        sampleEntity.setMessage("foo");
        repository.save(sampleEntity);

        Page<SampleEntity> page = repository.findByMessageContaining("fo", PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    public void shouldExecuteCustomMethodForIn() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("test");
        sampleEntity.setRate(10);
        sampleEntity.setMessage("foo");
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setType("test");
        sampleEntity2.setRate(10);
        sampleEntity2.setMessage("bar");
        repository.save(sampleEntity2);

        List<String> ids = Arrays.asList(documentId, documentId2);

        Page<SampleEntity> page = repository.findByIdIn(ids, PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(2L);
    }

    @Test
    public void shouldExecuteCustomMethodForNotIn() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("test");
        sampleEntity.setMessage("foo");
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setType("test");
        sampleEntity2.setMessage("bar");
        repository.save(sampleEntity2);

        List<String> ids = Collections.singletonList(documentId);

        Page<SampleEntity> page = repository.findByIdNotIn(ids, PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1L);
        assertThat(page.getContent().get(0).getId()).isEqualTo(documentId2);
    }

    @Test
    public void shouldHandleManyKeywordValuesQueryingIn() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setKeyword("foo");
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setKeyword("bar");
        repository.save(sampleEntity2);

        List<String> keywords = new ArrayList<>();
        keywords.add("foo");

        for (int i = 0; i < 1200; i++) {
            keywords.add(nextIdAsString());
        }

        List<SampleEntity> list = repository.findByKeywordIn(keywords);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(documentId);
    }

    @Test
    public void shouldHandleTextFieldQueryingNotIn() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("test");
        sampleEntity.setMessage("foo");
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setType("test");
        sampleEntity2.setMessage("bar");
        repository.save(sampleEntity2);

        List<SampleEntity> list = repository.findByMessageNotIn(Arrays.asList("Boo", "Bar"));
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(documentId);
    }

    @Test
    public void shouldExecuteCustomMethodForTrue() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("test");
        sampleEntity.setMessage("foo");
        sampleEntity.setAvailable(true);
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setType("test");
        sampleEntity2.setMessage("bar");
        sampleEntity2.setAvailable(false);
        repository.save(sampleEntity2);

        Page<SampleEntity> page = repository.findByAvailableTrue(PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    public void shouldExecuteCustomMethodFalse() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("test");
        sampleEntity.setMessage("foo");
        sampleEntity.setAvailable(true);
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setType("test");
        sampleEntity2.setMessage("bar");
        sampleEntity2.setAvailable(false);
        repository.save(sampleEntity2);

        Page<SampleEntity> page = repository.findByAvailableFalse(PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    public void shouldExecuteCustomMethodForOrderBy() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("abc");
        sampleEntity.setMessage("test");
        sampleEntity.setAvailable(true);
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setType("xyz");
        sampleEntity2.setMessage("bar");
        sampleEntity2.setAvailable(false);
        repository.save(sampleEntity2);

        String documentId3 = nextIdAsString();
        SampleEntity sampleEntity3 = new SampleEntity();
        sampleEntity3.setId(documentId3);
        sampleEntity3.setType("def");
        sampleEntity3.setMessage("foo");
        sampleEntity3.setAvailable(false);
        repository.save(sampleEntity3);

        Page<SampleEntity> page = repository.findByMessageOrderByTypeAsc("foo", PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    public void shouldExecuteCustomMethodWithBooleanParameter() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("test");
        sampleEntity.setMessage("foo");
        sampleEntity.setAvailable(true);
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setType("test");
        sampleEntity2.setMessage("bar");
        sampleEntity2.setAvailable(false);
        repository.save(sampleEntity2);

        Page<SampleEntity> page = repository.findByAvailable(false, PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1L);
    }

    @Test
    public void shouldReturnPageableInUnwrappedPageResult() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setType("test");
        sampleEntity.setMessage("foo");
        sampleEntity.setAvailable(true);
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setType("test");
        sampleEntity2.setMessage("bar");
        sampleEntity2.setAvailable(false);
        repository.save(sampleEntity2);
        PageRequest pageable = PageRequest.of(0, 10);
        Page<SampleEntity> page = repository.findByAvailable(false, pageable);

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(1L);
        assertThat(page.getPageable()).isSameAs(pageable);
    }

    @Test
    public void shouldReturnPageableResultsWithQueryAnnotationExpectedPageSize() {
        for (int i = 0; i < 30; i++) {
            String documentId = String.valueOf(i);
            SampleEntity sampleEntity = new SampleEntity();
            sampleEntity.setId(documentId);
            sampleEntity.setMessage("message");
            sampleEntity.setVersion(System.currentTimeMillis());
            repository.save(sampleEntity);
        }

        Page<SampleEntity> pageResult = repository.findByMessage("message", PageRequest.of(0, 23));

        assertThat(pageResult.getTotalElements()).isEqualTo(30);
        assertThat(pageResult.getContent().size()).isEqualTo(23);
    }

    @Test
    public void shouldReturnPageableResultsWithGivenSortingOrder() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("abc");
        sampleEntity.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("abd");
        sampleEntity2.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity2);

        String documentId3 = nextIdAsString();
        SampleEntity sampleEntity3 = new SampleEntity();
        sampleEntity3.setId(documentId3);
        sampleEntity3.setMessage("abe");
        sampleEntity3.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity3);

        Page<SampleEntity> pageResult = repository.findByMessageContaining("a", PageRequest.of(0, 23, Sort.by(Sort.Order.desc("message"))));

        assertThat(pageResult.getContent()).isNotEmpty();
        assertThat(pageResult.getContent().get(0).getMessage()).isEqualTo(sampleEntity3.getMessage());
    }

    @Test
    public void shouldReturnListForMessage() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("abc");
        sampleEntity.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("abd");
        sampleEntity2.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity2);

        String documentId3 = nextIdAsString();
        SampleEntity sampleEntity3 = new SampleEntity();
        sampleEntity3.setId(documentId3);
        sampleEntity3.setMessage("abe");
        sampleEntity3.setVersion(System.currentTimeMillis());
        repository.save(sampleEntity3);

        List<SampleEntity> sampleEntities = repository.findByMessage("abc");

        assertThat(sampleEntities).hasSize(1);
    }

    @Test
    public void shouldAllReturningJavaStreamInCustomQuery() {
        List<SampleEntity> entities = createSampleEntities("abc", 30);
        repository.saveAll(entities);

        Stream<SampleEntity> stream = streamingRepository.findByType("abc");

        assertThat(stream).isNotNull();
        assertThat(stream.count()).isEqualTo(30);
    }

    @Test
    public void shouldCountCustomMethod() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("abc");
        sampleEntity.setType("test");
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("abd");
        sampleEntity2.setType("test2");
        repository.save(sampleEntity2);

        long count = repository.countByType("test");

        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void shouldCountCustomMethodForNot() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("some message");
        sampleEntity.setType("some");
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("some message");
        sampleEntity2.setType("test");
        repository.save(sampleEntity2);

        long count = repository.countByTypeNot("test");

        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void shouldCountCustomMethodWithBooleanParameter() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("foo");
        sampleEntity.setType("test");
        sampleEntity.setAvailable(true);
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("bar");
        sampleEntity2.setType("test");
        sampleEntity2.setAvailable(false);
        repository.save(sampleEntity2);

        long count = repository.countByAvailable(false);

        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void shouldCountCustomMethodWithLessThan() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("some message");
        sampleEntity.setType("some");
        sampleEntity.setRate(9);
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("some message");
        sampleEntity2.setType("test");
        sampleEntity2.setRate(20);
        repository.save(sampleEntity2);

        long count = repository.countByRateLessThan(10);

        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void shouldCountCustomMethodWithBefore() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("some message");
        sampleEntity.setType("some");
        sampleEntity.setRate(10);
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("some message");
        sampleEntity2.setType("test");
        sampleEntity2.setRate(20);
        repository.save(sampleEntity2);

        long count = repository.countByRateBefore(10);

        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void shouldCountCustomMethodWithAfter() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("some message");
        sampleEntity.setType("some");
        sampleEntity.setRate(10);
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("some message");
        sampleEntity2.setType("test");
        sampleEntity2.setRate(0);
        repository.save(sampleEntity2);

        long count = repository.countByRateAfter(10);

        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void shouldCountCustomMethodWithLike() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("foo");
        sampleEntity.setType("some");
        sampleEntity.setRate(10);
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("some message");
        sampleEntity2.setType("test");
        sampleEntity2.setRate(10);
        repository.save(sampleEntity2);

        long count = repository.countByMessageLike("fo");

        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void shouldCountCustomMethodForStartingWith() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("foo");
        sampleEntity.setType("test");
        sampleEntity.setRate(10);
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("some message");
        sampleEntity2.setType("test");
        sampleEntity2.setRate(10);
        repository.save(sampleEntity2);

        long count = repository.countByMessageStartingWith("fo");

        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void shouldCountCustomMethodForEndingWith() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("foo");
        sampleEntity.setType("some");
        sampleEntity.setRate(10);
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("some message");
        sampleEntity2.setType("test");
        sampleEntity2.setRate(20);
        repository.save(sampleEntity2);

        long count = repository.countByMessageEndingWith("o");

        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void shouldCountCustomMethodForContains() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("foo");
        sampleEntity.setType("test");
        sampleEntity.setRate(10);
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("some message");
        sampleEntity2.setType("test");
        sampleEntity2.setRate(10);
        repository.save(sampleEntity2);

        long count = repository.countByMessageContaining("fo");

        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void shouldCountCustomMethodForIn() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("foo");
        sampleEntity.setType("test");
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("bar");
        sampleEntity2.setType("test");
        repository.save(sampleEntity2);

        List<String> ids = Arrays.asList(documentId, documentId2);

        long count = repository.countByIdIn(ids);

        assertThat(count).isEqualTo(2L);
    }

    @Test
    public void shouldCountCustomMethodForNotIn() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("foo");
        sampleEntity.setType("test");
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("bar");
        sampleEntity2.setType("test");
        repository.save(sampleEntity2);

        List<String> ids = Collections.singletonList(documentId);

        long count = repository.countByIdNotIn(ids);

        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void shouldCountCustomMethodTrue() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("foo");
        sampleEntity.setType("test");
        sampleEntity.setAvailable(true);
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("bar");
        sampleEntity2.setType("test");
        sampleEntity2.setAvailable(false);
        repository.save(sampleEntity2);

        long count = repository.countByAvailableTrue();

        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void shouldCountCustomMethodFalse() {
        String documentId = nextIdAsString();
        SampleEntity sampleEntity = new SampleEntity();
        sampleEntity.setId(documentId);
        sampleEntity.setMessage("foo");
        sampleEntity.setType("test");
        sampleEntity.setAvailable(true);
        repository.save(sampleEntity);

        String documentId2 = nextIdAsString();
        SampleEntity sampleEntity2 = new SampleEntity();
        sampleEntity2.setId(documentId2);
        sampleEntity2.setMessage("bar");
        sampleEntity2.setType("test");
        sampleEntity2.setAvailable(false);
        repository.save(sampleEntity2);

        long count = repository.countByAvailableFalse();

        assertThat(count).isEqualTo(1L);
    }

    @Test
    public void streamMethodsCanHandlePageable() {
        List<SampleEntity> entities = createSampleEntities("abc", 10);
        repository.saveAll(entities);

        Stream<SampleEntity> stream = streamingRepository.findByType("abc", PageRequest.of(0, 2));

        assertThat(stream).isNotNull();
        assertThat(stream.count()).isEqualTo(10L);
    }

    @Test
    public void streamMethodsShouldNotReturnSearchHits() {
        List<SampleEntity> entities = createSampleEntities("abc", 2);
        repository.saveAll(entities);

        Stream<SampleEntity> stream = streamingRepository.findByType("abc");

        assertThat(stream).isNotNull();
        stream.forEach(o -> assertThat(o).isInstanceOf(SampleEntity.class));
    }


    @Test
    public void shouldStreamEntitiesWithQueryAnnotatedMethod() {
        List<SampleEntity> entities = createSampleEntities("abc", 20);

        repository.saveAll(entities);

        Stream<SampleEntity> stream = streamingRepository.streamEntitiesByType("abc");

        long count = stream.peek(sampleEntity -> assertThat(sampleEntity).isInstanceOf(SampleEntity.class)).count();
        assertThat(count).isEqualTo(20);
    }


    @Test
    public void shouldStreamSearchHitsWithQueryAnnotatedMethod() {
        List<SampleEntity> entities = createSampleEntities("abc", 20);

        repository.saveAll(entities);

        Stream<SearchHit<SampleEntity>> stream = streamingRepository.streamSearchHitsByType("abc");

        long count = stream.peek(sampleEntity -> assertThat(sampleEntity).isInstanceOf(SearchHit.class)).count();
        assertThat(count).isEqualTo(20);
    }



    private List<SampleEntity> createSampleEntities(String type, int numberOfEntities) {
        List<SampleEntity> lists = new ArrayList<>();
        for (int i = 0; i < numberOfEntities; i++) {
            SampleEntity entity = new SampleEntity();
            entity.setId(UUID.randomUUID().toString());
            entity.setAvailable(true);
            entity.setMessage("message");
            entity.setType(type);
            lists.add(entity);
        }
        return lists;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Document(indexName = "test-index-sample-repositories-custom-method", replicas = 0, refreshInterval = "-1")
    static class SampleEntity {
        @Id
        private String id;
        @Field(type = FieldType.Text, store = true, fielddata = true)
        private String type;
        @Field(type = FieldType.Text, store = true, fielddata = true)
        private String message;
        @Field(type = FieldType.Keyword)
        private String keyword;
        private int rate;
        private boolean available;
        @Version
        private Long version;
    }

    public interface SampleCustomMethoRepository extends ElasticsearchRepository<SampleEntity, String> {
        Page<SampleEntity> findByType(String type, Pageable pageable);

        Page<SampleEntity> findByTypeNot(String type, Pageable pageable);

        @Query("{\"bool\": {\"must\":{\"term\": {\"message\": \"?0\"}}}}")
        Page<SampleEntity> findByMessage(String message, Pageable pageable);

        @Query("{\"bool\": {\"must\":{\"term\": {\"message\": \"?0\"}}}}")
        List<SampleEntity> findByMessage(String message);

        Page<SampleEntity> findByAvailable(boolean available, Pageable pageable);

        Page<SampleEntity> findByRateLessThan(int rate, Pageable pageable);

        Page<SampleEntity> findByRateBefore(int rate, Pageable pageable);

        Page<SampleEntity> findByRateAfter(int rate, Pageable pageable);

        Page<SampleEntity> findByMessageLike(String message, Pageable pageable);

        Page<SampleEntity> findByMessageStartingWith(String message, Pageable pageable);

        Page<SampleEntity> findByMessageEndingWith(String message, Pageable pageable);

        Page<SampleEntity> findByMessageContaining(String message, Pageable pageable);

        Page<SampleEntity> findByIdIn(List<String> ids, Pageable pageable);

        List<SampleEntity> findByKeywordIn(List<String> keywords);

        List<SampleEntity> findByKeywordNotIn(List<String> keywords);

        List<SampleEntity> findByMessageIn(List<String> keywords);

        List<SampleEntity> findByMessageNotIn(List<String> keywords);

        Page<SampleEntity> findByIdNotIn(List<String> ids, Pageable pageable);

        Page<SampleEntity> findByAvailableTrue(Pageable pageable);

        Page<SampleEntity> findByAvailableFalse(Pageable pageable);

        Page<SampleEntity> findByMessageOrderByTypeAsc(String message, Pageable pageable);

        long countByType(String type);

        long countByTypeNot(String type);

        long countByAvailable(boolean available);

        long countByRateLessThan(int rate);

        long countByRateBefore(int rate);

        long countByRateAfter(int rate);

        long countByMessageLike(String message);

        long countByMessageStartingWith(String message);

        long countByMessageEndingWith(String message);

        long countByMessageContaining(String message);

        long countByIdIn(List<String> ids);

        long countByIdNotIn(List<String> ids);

        long countByAvailableTrue();

        long countByAvailableFalse();

    }

    public interface SampleStreamingCustomMethodRepository extends ElasticsearchRepository<SampleEntity, String> {
        Stream<SampleEntity> findByType(String type);

        Stream<SampleEntity> findByType(String type, Pageable pageable);

        @Query("{\"bool\": {\"must\":[{\"term\": {\"type\": \"?0\"}}]}}")
        Stream<SampleEntity> streamEntitiesByType(String type);

        @Query("{\"bool\": {\"must\":[{\"term\": {\"type\": \"?0\"}}]}}")
        Stream<SearchHit<SampleEntity>> streamSearchHitsByType(String type);

    }

}
