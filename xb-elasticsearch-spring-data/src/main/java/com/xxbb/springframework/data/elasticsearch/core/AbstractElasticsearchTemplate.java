package com.xxbb.springframework.data.elasticsearch.core;

import com.xxbb.springframework.data.elasticsearch.BulkFailureException;
import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.document.Document;
import com.xxbb.springframework.data.elasticsearch.core.document.SearchDocumentResponse;
import com.xxbb.springframework.data.elasticsearch.core.event.AfterConvertCallback;
import com.xxbb.springframework.data.elasticsearch.core.event.AfterSaveCallback;
import com.xxbb.springframework.data.elasticsearch.core.event.BeforeConvertCallback;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import com.xxbb.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import com.xxbb.springframework.data.elasticsearch.core.query.*;
import com.xxbb.springframework.data.elasticsearch.support.VersionInfo;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.convert.EntityReader;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.callback.EntityCallbacks;
import org.springframework.data.util.Streamable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.awt.print.Pageable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractElasticsearchTemplate implements ApplicationContextAware, ElasticsearchOperations {
    @Nullable
    protected ElasticsearchConverter elasticsearchConverter;
    @Nullable
    protected RequestFactory requestFactory;
    @Nullable
    private EntityCallbacks entityCallbacks;
//    @Nullable
//    private EntityOperations entityOperations;

    // region initialization
    protected void initialize(ElasticsearchConverter elasticsearchConverter) {
        Assert.notNull(elasticsearchConverter, "elasticsearchConverter must not be null.");

        this.elasticsearchConverter = elasticsearchConverter;
        requestFactory = new RequestFactory(elasticsearchConverter);
        VersionInfo.logVersions(getClusterVersion());
    }

    protected ElasticsearchConverter createElasticsearchConverter() {
        MappingElasticsearchConverter mappingElasticsearchConverter = new MappingElasticsearchConverter(
                new SimpleElasticsearchMappingContext());
        mappingElasticsearchConverter.afterPropertiesSet();
        return mappingElasticsearchConverter;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (entityCallbacks == null) {
            setEntityCallbacks(EntityCallbacks.create(applicationContext));
        }
        if (elasticsearchConverter instanceof ApplicationContextAware) {
            ((ApplicationContextAware) elasticsearchConverter).setApplicationContext(applicationContext);
        }
    }

    public void setEntityCallbacks(EntityCallbacks entityCallbacks) {
        Assert.notNull(entityCallbacks, "entityCallback must not be null");
        this.entityCallbacks = entityCallbacks;
    }
    // endregion

    // region Document operations
    @Override
    public <T> T save(T entity) {
        Assert.notNull(entity, "entity must not be null");
        return save(entity, getIndexCoordinatesFor(entity.getClass()));
    }

    @Override
    public <T> T save(T entity, IndexCoordinates index) {
        Assert.notNull(entity, "entity must not be null");
        Assert.notNull(index, "index must not be null");

        IndexQuery query = getIndexQuery(entity);
        index(query, index);

        @SuppressWarnings("unchecked")
        T castResult = (T) query.getObject();
        return castResult;
    }

    @Override
    public <T> Iterable<T> save(Iterable<T> entities) {
        Assert.notNull(entities, "entities must not be null");
        Iterator<T> iterator = entities.iterator();
        if (iterator.hasNext()) {
            return save(entities, getIndexCoordinatesFor(iterator.next().getClass()));
        }
        return entities;
    }

    @Override
    public <T> Iterable<T> save(Iterable<T> entities, IndexCoordinates index) {
        Assert.notNull(entities, "entities must not be null");
        Assert.notNull(index, "index must not be null");

        List<IndexQuery> indexQueries = Streamable.of(entities).stream().map(this::getIndexQuery).collect(Collectors.toList());

        if (!indexQueries.isEmpty()) {
            List<IndexObjectInformation> ids = bulkIndex(indexQueries, index);
            Iterator<IndexObjectInformation> idIterator = ids.iterator();
            entities.forEach(entity -> {
                updateIndexObject(entity, idIterator.next());
            });
        }

        return indexQueries.stream().map(IndexQuery::getObject).map(entity -> (T) entity).collect(Collectors.toList());
    }

    @Override
    public <T> Iterable<T> save(T... entities) {
        return save(Arrays.asList(entities));
    }



    @Nullable
    @Override
    public <T> T get(String id, Class<T> clazz) {
        return get(id, clazz, getIndexCoordinatesFor(clazz));
    }

    @Override
    public <T> List<T> multiGet(Query query, Class<T> clazz) {
        return multiGet(query, clazz, getIndexCoordinatesFor(clazz));
    }

    @Override
    public boolean exists(String id, Class<?> clazz) {
        return exists(id, getIndexCoordinatesFor(clazz));
    }

    @Override
    public boolean exists(String id, IndexCoordinates index) {
        return doExists(id, index);
    }

    abstract protected boolean doExists(String id, IndexCoordinates index);

    @Override
    public String delete(String id, Class<?> entityType) {
        Assert.notNull(id, "id must not be null");
        Assert.notNull(entityType, "entity Type must not be null");
        return delete(id, getIndexCoordinatesFor(entityType));
    }

    @Override
    public void delete(Query query, Class<?> clazz) {
        delete(query, clazz, getIndexCoordinatesFor(clazz));
    }

    @Override
    public String delete(Object entity) {
        return delete(entity, getIndexCoordinatesFor(entity.getClass()));
    }

    @Override
    public String delete(Object entity, IndexCoordinates index) {
        return delete(getEntityId(entity), index);
    }

    @Override
    public List<IndexObjectInformation> bulkIndex(List<IndexQuery> queries, Class<?> clazz) {
        return bulkIndex(queries, getIndexCoordinatesFor(clazz));
    }

    @Override
    public List<IndexObjectInformation> bulkIndex(List<IndexQuery> queries, BulkOptions bulkOptions, Class<?> clazz) {
        return bulkIndex(queries, bulkOptions, getIndexCoordinatesFor(clazz));
    }

    @Override
    public void bulkUpdate(List<UpdateQuery> queries, Class<?> clazz) {
        bulkUpdate(queries, getIndexCoordinatesFor(clazz));
    }
    // endregion

    // region search operation

    @Override
    public long count(Query query, Class<?> clazz) {
        return count(query, clazz, getIndexCoordinatesFor(clazz));
    }

    @Override
    public <T> SearchHitsIterator<T> searchForStream(Query query, Class<T> clazz) {
        return searchForStream(query, clazz, getIndexCoordinatesFor(clazz));
    }

    @Override
    public <T> SearchHitsIterator<T> searchForStream(Query query, Class<T> clazz, IndexCoordinates index) {
        long scrollTimeInMillis = TimeValue.timeValueMillis(1).millis();

        // noinspection ConstantConditions
        int maxCount = query.isLimiting() ? query.getMaxResults() : 0;
        return StreamQueries.streamResults(
                maxCount,
                searchScrollStart(scrollTimeInMillis, query, clazz, index),
                scrollId -> searchScrollContinue(scrollId, scrollTimeInMillis, clazz, index),
                this::searchScrollClear);
    }

    @Override
    public <T> SearchHits<T> search(MoreLikeThisQuery query, Class<T> clazz) {
        return search(query, clazz, getIndexCoordinatesFor(clazz));
    }

    @Override
    public <T> SearchHits<T> search(MoreLikeThisQuery query, Class<T> clazz, IndexCoordinates index) {
        Assert.notNull(query.getId(), "No document ID defined for MoreLikeThisQuery");
        MoreLikeThisQueryBuilder moreLikeThisQueryBuilder = requestFactory.moreLikeThisQueryBuilder(query, index);
        return search(new NativeSearchQueryBuilder().withQuery(moreLikeThisQueryBuilder).build(), clazz, index);
    }

    @Override
    public <T> List<SearchHits<T>> multiSearch(List<? extends Query> queries, Class<T> clazz) {
        return multiSearch(queries, clazz, getIndexCoordinatesFor(clazz));
    }

    @Override
    public <T> List<SearchHits<T>> multiSearch(List<? extends Query> queries, Class<T> clazz, IndexCoordinates index) {
        MultiSearchRequest request = new MultiSearchRequest();
        for (Query query : queries) {
            request.add(requestFactory.searchRequest(query, clazz, index));
        }

        MultiSearchResponse.Item[] items = getMultiSearchResult(request);

        SearchDocumentResponseCallback<SearchHits<T>> callback = new ReadSearchDocumentResponseCallback<T>(clazz, index);
        List<SearchHits<T>> res = new ArrayList<>(queries.size());
        int c = 0;
        for (Query query : queries) {
            res.add(callback.doWith(SearchDocumentResponse.from(items[c++].getResponse())));
        }
        return res;
    }

    @Override
    public List<SearchHits<?>> multiSearch(List<? extends Query> queries, List<Class<?>> classes) {
        Assert.notNull(queries, "queries must not be null");
        Assert.notNull(classes, "classes must not be null");
        Assert.isTrue(queries.size() == classes.size(), "queries and classes must have same size");

        MultiSearchRequest request = new MultiSearchRequest();
        Iterator<Class<?>> it = classes.iterator();
        for (Query query : queries) {
            Class<?> clazz = it.next();
            request.add(requestFactory.searchRequest(query, clazz, getIndexCoordinatesFor(clazz)));
        }

        MultiSearchResponse.Item[] items = getMultiSearchResult(request);

        List<SearchHits<?>> res = new ArrayList<>(queries.size());
        int c = 0;
        Iterator<Class<?>> it1 = classes.iterator();
        for (Query query : queries) {
            Class entityClass = it1.next();
            SearchDocumentResponseCallback<SearchHits<?>> callback = new ReadSearchDocumentResponseCallback<>(entityClass, getIndexCoordinatesFor(entityClass));

            SearchResponse response = items[c++].getResponse();
            res.add(callback.doWith(SearchDocumentResponse.from(response)));
        }
        return res;
    }

    @Override
    public List<SearchHits<?>> multiSearch(List<? extends Query> queries, List<Class<?>> classes, IndexCoordinates index) {
        Assert.notNull(queries, "queries must not be null");
        Assert.notNull(classes, "classes must not be null");
        Assert.notNull(index, "index must not be null");
        Assert.isTrue(queries.size() == classes.size(), "queries and classes must have same size");

        MultiSearchRequest request = new MultiSearchRequest();
        Iterator<Class<?>> it = classes.iterator();
        for (Query query : queries) {
            request.add(requestFactory.searchRequest(query, it.next(), index));
        }

        MultiSearchResponse.Item[] items = getMultiSearchResult(request);

        List<SearchHits<?>> res = new ArrayList<>(queries.size());
        int c = 0;
        Iterator<Class<?>> it1 = classes.iterator();
        for (Query query : queries) {
            Class entityClass = it1.next();
            SearchDocumentResponseCallback<SearchHits<?>> callback = new ReadSearchDocumentResponseCallback<>(entityClass, index);

            SearchResponse response = items[c++].getResponse();
            res.add(callback.doWith(SearchDocumentResponse.from(response)));
        }
        return res;
    }

    @Override
    public <T> SearchHits<T> search(Query query, Class<T> clazz) {
        return search(query, clazz, getIndexCoordinatesFor(clazz));
    }

    @Override
    public SearchResponse suggest(SuggestBuilder suggestion, Class<?> clazz) {
        return suggest(suggestion, getIndexCoordinatesFor(clazz));
    }

    abstract protected <T> SearchScrollHits<T> searchScrollStart(long scrollTimeInMillis, Query query, Class<T> clazz, IndexCoordinates index);

    abstract protected <T> SearchScrollHits<T> searchScrollContinue(@Nullable String scrollId, long scrollTimeInMillis, Class<T> clazz, IndexCoordinates index);

    protected void searchScrollClear(String scrollId) {
        searchScrollClear(Collections.singletonList(scrollId));
    }

    abstract protected void searchScrollClear(List<String> scrollIds);

    abstract protected MultiSearchResponse.Item[] getMultiSearchResult(MultiSearchRequest request);

    // endregion

    // region helper method
    @Override
    public ElasticsearchConverter getElasticsearchConverter() {
        Assert.notNull(elasticsearchConverter, "elasticsearchConverter is not initialized");
        return elasticsearchConverter;
    }

    public RequestFactory getRequestFactory() {
        Assert.notNull(requestFactory, "requestfactory not initialized");
        return requestFactory;
    }

    protected static String[] toArray(List<String> values) {
        String[] valuesAsArray = new String[values.size()];
        return values.toArray(valuesAsArray);
    }

    @Override
    public IndexCoordinates getIndexCoordinatesFor(Class<?> clazz) {
        return getRequiredPersistentEntity(clazz).getIndexCoordinates();
    }

    protected List<IndexObjectInformation> checkForBulkOperationFailure(BulkResponse bulkResponse) {
        if (bulkResponse.hasFailures()) {
            Map<String, String> failedDocuments = new HashMap<>();
            for (BulkItemResponse item : bulkResponse.getItems()) {
                if (item.isFailed()) {
                    failedDocuments.put(item.getId(), item.getFailureMessage());
                }
                throw new BulkFailureException(
                        "Bulk operation has failures. Use ElasticsearchException.getFailureDocuments() for deetailed messages [" + failedDocuments + ']', failedDocuments);
            }
        }
        return Stream.of(bulkResponse.getItems()).map(bulkItemResponse -> {
            DocWriteResponse response = bulkItemResponse.getResponse();
            if (response != null) {
                return IndexObjectInformation.of(response.getId(), response.getSeqNo(), response.getPrimaryTerm(), response.getVersion());
            } else {
                return IndexObjectInformation.of(bulkItemResponse.getId(), null, null, null);
            }
        }).collect(Collectors.toList());
    }

    protected void updateIndexObject(Object entity, IndexObjectInformation indexObjectInformation) {
        ElasticsearchPersistentEntity<?> persistentEntity = getRequiredPersistentEntity(entity.getClass());
        PersistentPropertyAccessor<Object> propertyAccessor = persistentEntity.getPropertyAccessor(entity);
        ElasticsearchPersistentProperty idProperty = persistentEntity.getIdProperty();

        if (idProperty != null && idProperty.getType().isAssignableFrom(String.class)) {
            persistentEntity.getPropertyAccessor(entity).setProperty(idProperty, indexObjectInformation.getId());
        }

        if (indexObjectInformation.getSeqNo() != null && indexObjectInformation.getPrimaryTerm() != null && persistentEntity.hasSeqNoPrimaryTermProperty()) {
            ElasticsearchPersistentProperty seqNoPrimaryTermProperty = persistentEntity.getSeqNoPrimaryTermProperty();
            propertyAccessor.setProperty(seqNoPrimaryTermProperty, new SeqNoPrimaryTerm(indexObjectInformation.getSeqNo(), indexObjectInformation.getPrimaryTerm()));
        }

        if (indexObjectInformation.getVersion() != null && persistentEntity.hasVersionProperty()) {
            ElasticsearchPersistentProperty versionProperty = persistentEntity.getVersionProperty();
            propertyAccessor.setProperty(versionProperty, indexObjectInformation.getVersion());
        }
    }

    ElasticsearchPersistentEntity<?> getRequiredPersistentEntity(Class<?> clazz) {
        return elasticsearchConverter.getMappingContext().getRequiredPersistentEntity(clazz);
    }

    @Nullable
    private String getEntityId(Object entity) {
        ElasticsearchPersistentEntity<?> persistentEntity = getRequiredPersistentEntity(entity.getClass());
        ElasticsearchPersistentProperty idProperty = persistentEntity.getIdProperty();
        if (idProperty != null) {
            return stringIdRepresentation(persistentEntity.getPropertyAccessor(entity).getProperty(idProperty));
        }
        return null;
    }

    @Nullable
    private Long getEntityVersion(Object entity) {
        ElasticsearchPersistentEntity<?> persistentEntity = getRequiredPersistentEntity(entity.getClass());
        ElasticsearchPersistentProperty versionProperty = persistentEntity.getVersionProperty();

        if (versionProperty != null) {
            Object version = persistentEntity.getPropertyAccessor(entity).getProperty(versionProperty);
            if (version != null && Long.class.isAssignableFrom(version.getClass())) {
                return (Long)version;
            }
        }
        return null;
    }

    @Nullable
    private SeqNoPrimaryTerm getEntitySeqNoPrimaryTerm(Object entity) {
        ElasticsearchPersistentEntity<?> persistentEntity = getRequiredPersistentEntity(entity.getClass());
        ElasticsearchPersistentProperty property = persistentEntity.getSeqNoPrimaryTermProperty();

        if (property != null) {
            Object seqNoPrimaryTerm = persistentEntity.getPropertyAccessor(entity).getProperty(property);

            if (seqNoPrimaryTerm != null && SeqNoPrimaryTerm.class.isAssignableFrom(seqNoPrimaryTerm.getClass())) {
                return (SeqNoPrimaryTerm) seqNoPrimaryTerm;
            }
        }
        return null;
    }

    private <T> IndexQuery getIndexQuery(T entity) {
        String id = getEntityId(entity);
        if (id != null) {
            id = elasticsearchConverter.convertId(id);
        }

        IndexQueryBuilder builder = new IndexQueryBuilder().withId(id).withObject(entity);
        SeqNoPrimaryTerm seqNoPrimaryTerm = getEntitySeqNoPrimaryTerm(entity);
        if (seqNoPrimaryTerm != null) {
            builder.withSeqNoPrimaryTerm(seqNoPrimaryTerm);
        } else {
            builder.withVersion(getEntityVersion(entity));
        }
        return builder.build();
    }
    // endregion

    @Nullable
    abstract protected String getClusterVersion();


    // region Entity callback
    protected <T> T maybeCallBackBeforeConvert(T entity, IndexCoordinates index) {
        if (entityCallbacks != null) {
            return entityCallbacks.callback(BeforeConvertCallback.class, entity, index);
        }
        return entity;
    }

    protected void maybeCallbackBeforeConvertWithQuery(Object query, IndexCoordinates index) {
        if (query instanceof IndexQuery) {
            IndexQuery indexQuery = (IndexQuery) query;
            Object queryObject = indexQuery.getObject();

            if (queryObject != null) {
                queryObject = maybeCallBackBeforeConvert(queryObject, index);
                indexQuery.setObject(queryObject);
            }
        }
    }

    protected void maybeCallbackBeforeConvertWithQueries(List<?> queries, IndexCoordinates index) {
        queries.forEach(query -> maybeCallbackBeforeConvertWithQuery(query, index));
    }

    protected <T> T maybeCallbackAfterSave(T entity, IndexCoordinates index) {
        if (entityCallbacks != null) {
            return entityCallbacks.callback(AfterSaveCallback.class, entity, index);
        }
        return entity;
    }

    protected void maybeCallbackAfterSaveWithQuery(Object query, IndexCoordinates index) {
        if (query instanceof IndexQuery) {
            IndexQuery indexQuery = (IndexQuery) query;
            Object queryObject = indexQuery.getObject();

            if (queryObject != null) {
                queryObject = maybeCallbackAfterSave(queryObject, index);
                indexQuery.setObject(queryObject);
            }
        }
    }

    protected void maybeCallbackAfterSaveWithQueries(List<?> queries, IndexCoordinates index) {
        queries.forEach(query -> maybeCallbackAfterSaveWithQuery(query, index));
    }

    protected <T> T maybeCallbackAfterConvert(T entity, Document document, IndexCoordinates index) {
        if (entityCallbacks != null) {
            return entityCallbacks.callback(AfterConvertCallback.class, entity, document, index);
        }
        return entity;
    }
    // endregion

    protected void updateIndexedObjectsWithQueries(List<?> queries, List<IndexObjectInformation> indexObjectInformations) {
        for (int i = 0; i < queries.size(); i++) {
            Object query = queries.get(i);
            if (query instanceof IndexQuery) {
                IndexQuery indexQuery = (IndexQuery) query;
                Object queryObject = indexQuery.getObject();
                if (queryObject != null) {
                    updateIndexObject(queryObject, indexObjectInformations.get(i));
                }
            }
        }
    }

    // region Document callback
    protected interface DocumentCallback<T> {
        @Nullable
        T doWith(@Nullable Document document);
    }

    protected class ReadDocumentCallback<T> implements DocumentCallback<T> {
        private final EntityReader<? super T, Document> reader;
        private final Class<T> type;
        private final IndexCoordinates index;

        public ReadDocumentCallback(EntityReader<? super T, Document> reader, Class<T> type, IndexCoordinates index) {
            Assert.notNull(reader, "reader must not be null");
            Assert.notNull(type, "type must not be null");

            this.reader = reader;
            this.type = type;
            this.index = index;
        }

        @Nullable
        public T doWith(@Nullable Document document) {
            if (document == null) {
                return null;
            }
            T entity = reader.read(type, document);
            return maybeCallbackAfterConvert(entity, document, index);
        }
    }

    protected interface SearchDocumentResponseCallback<T> {
        @NonNull
        T doWith(@NonNull SearchDocumentResponse response);
    }

    protected class ReadSearchDocumentResponseCallback<T> implements SearchDocumentResponseCallback<SearchHits<T>> {
        private final DocumentCallback<T> delegate;
        private final Class<T> type;

        public ReadSearchDocumentResponseCallback(Class<T> type, IndexCoordinates index) {
            Assert.notNull(type, "type must not be null");
            this.delegate = new ReadDocumentCallback<>(elasticsearchConverter, type, index);
            this.type = type;
        }

        @Override
        public SearchHits<T> doWith(SearchDocumentResponse response) {
            List<T> entities = response.getSearchDocuments().stream().map(delegate::doWith).collect(Collectors.toList());
            return SearchHitMapping.mappingFor(type, elasticsearchConverter).mapHits(response, entities);
        }
    }

    protected class ReadSearchScrollDocumentResponseCallback<T> implements SearchDocumentResponseCallback<SearchScrollHits<T>> {
        private final DocumentCallback<T> delegate;
        private final Class<T> type;

        public ReadSearchScrollDocumentResponseCallback(Class<T> type, IndexCoordinates index) {
            Assert.notNull(type, "Type must not be null");
            this.delegate = new ReadDocumentCallback<>(elasticsearchConverter, type, index);
            this.type = type;
        }

        @Override
        public SearchScrollHits<T> doWith(SearchDocumentResponse response) {
            List<T> entities = response.getSearchDocuments().stream().map(delegate::doWith).collect(Collectors.toList());
            return SearchHitMapping.mappingFor(type, elasticsearchConverter).mapScrollHits(response, entities);
        }
    }
    // endregion
}
