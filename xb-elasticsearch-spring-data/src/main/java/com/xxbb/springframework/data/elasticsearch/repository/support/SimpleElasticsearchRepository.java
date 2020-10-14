package com.xxbb.springframework.data.elasticsearch.repository.support;

import com.xxbb.springframework.data.elasticsearch.core.*;
import com.xxbb.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import com.xxbb.springframework.data.elasticsearch.core.query.MoreLikeThisQuery;
import com.xxbb.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import com.xxbb.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import com.xxbb.springframework.data.elasticsearch.core.query.Query;
import com.xxbb.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.util.StreamUtils;
import org.springframework.data.util.Streamable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.idsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

public class SimpleElasticsearchRepository<T, ID> implements ElasticsearchRepository<T, ID> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleElasticsearchRepository.class);

    protected ElasticsearchOperations operations;
    protected IndexOperations indexOperations;

    protected Class<T> entityClass;
    protected ElasticsearchEntityInformation<T, ID> entityInformation;

    public SimpleElasticsearchRepository(ElasticsearchEntityInformation<T, ID> metadata, ElasticsearchOperations operations) {
        this.operations = operations;
        Assert.notNull(metadata, "ElasticsearchEntityInformation must not be null!");
        this.entityInformation = metadata;
        this.entityClass = this.entityInformation.getJavaType();
        this.indexOperations = operations.indexOps(this.entityClass);

        try {
            if (shouldCreateIndexAndMapping() && !indexOperations.exists()) {
                indexOperations.create();
                indexOperations.putMapping(entityClass);
            }
        } catch (Exception e) {
            logger.warn("Cannot create index: {}", e.getMessage());
        }
    }

    private boolean shouldCreateIndexAndMapping() {
        final ElasticsearchPersistentEntity<?> entity = operations.getElasticsearchConverter().getMappingContext().getRequiredPersistentEntity(entityClass);
        return entity.isCreateIndexAndMapping();
    }

    @Override
    public Iterable<T> search(QueryBuilder query) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query).build();
        long count = execute(operations -> operations.count(searchQuery, entityClass, getIndexcoordinates()));

        if (count == 0) {
            return new PageImpl<>(Collections.emptyList());
        }
        searchQuery.setPageable(PageRequest.of(0, (int) count));
        SearchHits<T> searchHits = execute(operations -> operations.search(searchQuery, entityClass, getIndexcoordinates()));
        AggregatedPage<SearchHit<T>> page = SearchHitSupport.page(searchHits, searchQuery.getPageable());
        return (Page<T>) SearchHitSupport.unwrapSearchHits(page);
    }

    @Override
    public Page<T> search(QueryBuilder query, Pageable pageable) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query).withPageable(pageable).build();
        SearchHits<T> searchHits = execute(operations -> operations.search(searchQuery, entityClass, getIndexcoordinates()));
        AggregatedPage<SearchHit<T>> page = SearchHitSupport.page(searchHits, searchQuery.getPageable());
        return (Page<T>) SearchHitSupport.unwrapSearchHits(page);
    }

    @Override
    public Page<T> search(Query searchQuery) {
        SearchHits<T> searchHits = execute(operations -> operations.search(searchQuery, entityClass, getIndexcoordinates()));
        AggregatedPage<SearchHit<T>> page = SearchHitSupport.page(searchHits, searchQuery.getPageable());
        return (Page<T>) SearchHitSupport.unwrapSearchHits(page);
    }

    @Override
    public Page<T> searchSimilar(T entity, String[] fields, Pageable pageable) {
        Assert.notNull(entity, "Cannot search similar record for 'null'");
        Assert.notNull(pageable, "Pageable cannot be null");
        MoreLikeThisQuery query = new MoreLikeThisQuery();
        query.setId(stringIdRepresentation(extractIdFromBean(entity)));
        query.setPageable(pageable);

        if (fields != null) {
            query.addFields(fields);
        }

        SearchHits<T> searchHits = execute(operations -> operations.search(query, entityClass, getIndexcoordinates()));
        AggregatedPage<SearchHit<T>> page = SearchHitSupport.page(searchHits, pageable);
        return (Page<T>) SearchHitSupport.unwrapSearchHits(page);
    }

    @Override
    public <S extends T> S save(S entity) {
        Assert.notNull(entity, "Cannot save 'null' entity");
        return executeAndRefresh(operations -> operations.save(entity, getIndexcoordinates()));
    }

    public <S extends T> List<S> save(List<S> entities) {
        Assert.notNull(entities, "Cannot save 'null' as a list");
        return Streamable.of(saveAll(entities)).stream().collect(Collectors.toList());
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        Assert.notNull(entities, "Cannot save 'null' as a list");
        IndexCoordinates indexCoordinates = getIndexcoordinates();
        executeAndRefresh(operations -> operations.save(entities, indexCoordinates));
        return entities;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(execute(
                operations -> operations.get(stringIdRepresentation(id), entityClass, getIndexcoordinates())));
    }

    @Override
    public boolean existsById(ID id) {
        return execute(operations -> operations.exists(stringIdRepresentation(id), getIndexcoordinates()));
    }

    @Override
    public Iterable<T> findAll() {
        int itemCount = (int) this.count();

        if (itemCount == 0) {
            return new PageImpl<>(Collections.emptyList());
        }
        return this.findAll(PageRequest.of(0, Math.max(1, itemCount)));
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(matchAllQuery()).withPageable(pageable).build();
        SearchHits<T> searchHits = execute(operations -> operations.search(query, entityClass, getIndexcoordinates()));
        AggregatedPage<SearchHit<T>> page = SearchHitSupport.page(searchHits, query.getPageable());
        return (Page<T>) SearchHitSupport.unwrapSearchHits(page);
    }

    @Override
    public Iterable<T> findAll(Sort sort) {
        int itemCount = (int) this.count();
        if (itemCount == 0) {
            return new PageImpl<>(Collections.emptyList());
        }
        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(matchAllQuery()).withPageable(PageRequest.of(0, itemCount, sort)).build();
        List<SearchHit<T>> searchHitList = execute(operations -> operations.search(query, entityClass, getIndexcoordinates()).getSearchHits());
        return (List<T>) SearchHitSupport.unwrapSearchHits(searchHitList);
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        Assert.notNull(ids, "ids cannot be null");
        List<T> result = new ArrayList<>();
        List<String> stringIds = stringIdRepresentation(ids);

        if (stringIds.isEmpty()) {
            return result;
        }
        NativeSearchQuery query = new NativeSearchQueryBuilder().withIds(stringIds).build();
        List<T> multiGetEntities = execute(operations -> operations.multiGet(query, entityClass, getIndexcoordinates()));

        if (multiGetEntities != null) {
            multiGetEntities.forEach(entity -> {
                if (entity != null) {
                    result.add(entity);
                }
            });
        }

        return result;
    }

    @Override
    public long count() {
        NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(matchAllQuery()).build();
        return execute(operations -> operations.count(query, entityClass, getIndexcoordinates()));
    }

    @Override
    public void deleteById(ID id) {
        Assert.notNull(id, "Cannot delete 'null' id");
        doDelete(id, null, getIndexcoordinates());
    }

    @Override
    public void delete(T entity) {
        Assert.notNull(entity, "Cannot delete 'null' entity");
        doDelete(extractIdFromBean(entity), operations.getEntityRouting(entity), getIndexcoordinates());
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, "Cannot delete 'null' list");

        IndexCoordinates index = getIndexcoordinates();
        IdsQueryBuilder idsQueryBuilder = idsQuery();
        for (T entity : entities) {
            ID id = extractIdFromBean(entity);
            if (id != null) {
                idsQueryBuilder.addIds(stringIdRepresentation(id));
            }
        }

        if (idsQueryBuilder.ids().isEmpty()) {
            return;
        }

        Query query = new NativeSearchQueryBuilder().withQuery(idsQueryBuilder).build();
        executeAndRefresh((OperationsCallback<Void>) operations -> {
            operations.delete(query, entityClass, index);
            return null;
        });
    }

    private void doDelete(@Nullable ID id, @Nullable String routing, IndexCoordinates index) {
        if (id != null) {
            executeAndRefresh(operations -> operations.delete(stringIdRepresentation(id), routing, index));
        }
    }

    @Override
    public void deleteAll() {
        IndexCoordinates index = getIndexcoordinates();
        Query query = new NativeSearchQueryBuilder().withQuery(matchAllQuery()).build();

        executeAndRefresh((OperationsCallback<Void>) operations -> {
            operations.delete(query, entityClass, index);
            return null;
        });
    }

    public void refresh() {
        indexOperations.refresh();
    }

    @Nullable
    protected ID extractIdFromBean(T entity) {
        return entityInformation.getId(entity);
    }

    private List<String> stringIdRepresentation(Iterable<ID> ids) {
        Assert.notNull(ids, "ids cannot be null");
        return StreamUtils.createStreamFromIterator(ids.iterator()).map(id -> stringIdRepresentation(id)).collect(Collectors.toList());
    }

    @Nullable
    protected String stringIdRepresentation(@Nullable ID id) {
        return operations.stringIdRepresentation(id);
    }

    private IndexCoordinates getIndexcoordinates() {
        return operations.getIndexCoordinatesFor(entityClass);
    }

    @FunctionalInterface
    public interface OperationsCallback<R> {
        @Nullable
        R doWithOperations(ElasticsearchOperations operations);
    }

    @Nullable
    public <R> R execute(OperationsCallback<R> callback) {
        return callback.doWithOperations(operations);
    }

    @Nullable
    public <R> R executeAndRefresh(OperationsCallback<R> callback) {
        R result = callback.doWithOperations(operations);
        refresh();
        return result;
    }
}
