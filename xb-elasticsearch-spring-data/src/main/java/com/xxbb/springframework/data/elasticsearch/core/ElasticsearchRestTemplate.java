package com.xxbb.springframework.data.elasticsearch.core;

import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.document.DocumentAdapters;
import com.xxbb.springframework.data.elasticsearch.core.document.SearchDocumentResponse;
import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import com.xxbb.springframework.data.elasticsearch.core.query.*;
import com.xxbb.springframework.data.elasticsearch.support.SearchHitsUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ElasticsearchRestTemplate extends AbstractElasticsearchTemplate {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchRestTemplate.class);

    private RestHighLevelClient client;
    private ElasticsearchExceptionTranslator exceptionTranslator;

    // region Initialization
    public ElasticsearchRestTemplate(RestHighLevelClient client) {
        Assert.notNull(client, "Client must not be null");
        this.client = client;
        this.exceptionTranslator = new ElasticsearchExceptionTranslator();
        initialize(createElasticsearchConverter());
    }

    public ElasticsearchRestTemplate(RestHighLevelClient client, ElasticsearchConverter elasticsearchConverter) {
        Assert.notNull(client, "Client must not be null");
        this.client = client;
        this.exceptionTranslator = new ElasticsearchExceptionTranslator();
        initialize(elasticsearchConverter);
    }
    // endregion

    // region helper methods
    @Override
    protected String getClusterVersion() {
        try {
            return execute(client -> client.info(RequestOptions.DEFAULT).getVersion().toString());
        } catch (Exception ignored) {}
        return null;
    }
    // endregion

    // region index operations
    @Override
    public IndexOperations indexOps(Class<?> clazz) {
        Assert.notNull(clazz, "clazz must not be null");
        return new DefaultIndexOperations(this, clazz);
    }

    @Override
    public IndexOperations indexOps(IndexCoordinates index) {
        Assert.notNull(index, "index must not be null");
        return new DefaultIndexOperations(this, index);
    }

    @Override
    public String getEntityRouting(Object entity) {
        return null;
    }
    // endregion

    // region document operations
    @Override
    public String index(IndexQuery query, IndexCoordinates index) {
        maybeCallbackBeforeConvertWithQuery(query, index);
        IndexRequest request = requestFactory.indexRequest(query, index);
        IndexResponse response = execute(client -> client.index(request, RequestOptions.DEFAULT));

        Object queryObject = query.getObject();
        if (queryObject != null) {
            updateIndexObject(queryObject, IndexObjectInformation.of(response.getId(), response.getSeqNo(), response.getPrimaryTerm(), response.getVersion()));
        }
        maybeCallbackAfterSaveWithQuery(query, index);
        return response.getId();
    }

    @Override
    public <T> T get(String id, Class<T> clazz, IndexCoordinates index) {
        GetRequest request = requestFactory.getRequest(id, index);
        GetResponse response = execute(client -> client.get(request, RequestOptions.DEFAULT));

        DocumentCallback<T> callback = new ReadDocumentCallback<>(elasticsearchConverter, clazz, index);
        return callback.doWith(DocumentAdapters.from(response));
    }

    @Override
    public <T> List<T> multiGet(Query query, Class<T> clazz, IndexCoordinates index) {
        Assert.notNull(index, "index must not be null");
        Assert.notNull(query.getIds(), "No id defined for query");

        MultiGetRequest request = requestFactory.multiGetRequest(query, clazz, index);
        MultiGetResponse result = execute(client -> client.mget(request, RequestOptions.DEFAULT));

        DocumentCallback<T> callback = new ReadDocumentCallback<>(elasticsearchConverter, clazz, index);
        return DocumentAdapters.from(result).stream().map(callback::doWith).collect(Collectors.toList());
    }

    @Override
    public List<IndexObjectInformation> bulkIndex(List<IndexQuery> queries, BulkOptions bulkOptions, IndexCoordinates index) {
        Assert.notNull(queries, "List of IndexQuery must not be null");
        Assert.notNull(bulkOptions, "BulkOptions must not be null");

        return doBulkOperation(queries, bulkOptions, index);
    }

    @Override
    public void bulkUpdate(List<UpdateQuery> queries, BulkOptions bulkOptions, IndexCoordinates index) {
        Assert.notNull(queries, "List of IndexQuery must not be null");
        Assert.notNull(bulkOptions, "BulkOptions must not be null");

        doBulkOperation(queries, bulkOptions, index);
    }

    @Override
    public String delete(String id, @Nullable String routing, IndexCoordinates index) {
        Assert.notNull(id, "id must not be null");
        Assert.notNull(index, "index must not be null");

        DeleteRequest request = requestFactory.deleteRequest(elasticsearchConverter.convertId(id), routing, index);
        return execute(client -> client.delete(request, RequestOptions.DEFAULT).getId());
    }


    @Override
    public void delete(Query query, Class<?> clazz, IndexCoordinates index) {
        DeleteByQueryRequest request = requestFactory.deleteByQueryRequest(query, clazz, index);
        execute(client -> client.deleteByQuery(request, RequestOptions.DEFAULT));
    }

    @Override
    public UpdateResponse update(UpdateQuery query, IndexCoordinates index) {
        UpdateRequest request = requestFactory.updateRequest(query, index);
        UpdateResponse.Result result = UpdateResponse.Result.valueOf(execute(client -> client.update(request, RequestOptions.DEFAULT)).getResult().name());
        return new UpdateResponse(result);
    }

    private List<IndexObjectInformation> doBulkOperation(List<?> queries, BulkOptions bulkOptions, IndexCoordinates index) {
        maybeCallbackBeforeConvertWithQueries(queries, index);
        BulkRequest bulkRequest = requestFactory.bulkRequest(queries, bulkOptions, index);
        List<IndexObjectInformation> indexObjectInformationList = checkForBulkOperationFailure(
                execute(client -> client.bulk(bulkRequest, RequestOptions.DEFAULT)));
        updateIndexedObjectsWithQueries(queries, indexObjectInformationList);
        maybeCallbackAfterSaveWithQueries(queries, index);
        return indexObjectInformationList;
    }

    @Override
    protected boolean doExists(String id, IndexCoordinates index) {
        GetRequest request = requestFactory.getRequest(id, index);
        request.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);
        return execute(client -> client.get(request, RequestOptions.DEFAULT).isExists());
    }
    // endregion

    // region search operation
    @Override
    public <T> SearchScrollHits<T> searchScrollStart(long scrollTimeInMillis, Query query, Class<T> clazz, IndexCoordinates index) {
        Assert.notNull(query.getPageable(), "pageable of query must not be null");
        SearchRequest request = requestFactory.searchRequest(query, clazz, index);
        request.scroll(TimeValue.timeValueMillis(scrollTimeInMillis));

        SearchResponse response = execute(client -> client.search(request, RequestOptions.DEFAULT));
        SearchDocumentResponseCallback<SearchScrollHits<T>> callback = new ReadSearchScrollDocumentResponseCallback<>(clazz, index);
        return callback.doWith(SearchDocumentResponse.from(response));
    }

    @Override
    public  <T> SearchScrollHits<T> searchScrollContinue(@Nullable String scrollId, long scrollTimeInMillis, Class<T> clazz, IndexCoordinates index) {
        SearchScrollRequest request = new SearchScrollRequest(scrollId);
        request.scroll(TimeValue.timeValueMillis(scrollTimeInMillis));

        SearchResponse response = execute(client -> client.scroll(request, RequestOptions.DEFAULT));
        SearchDocumentResponseCallback<SearchScrollHits<T>> callback = new ReadSearchScrollDocumentResponseCallback<>(clazz, index);
        return callback.doWith(SearchDocumentResponse.from(response));
    }

    @Override
    public void searchScrollClear(List<String> scrollIds) {
        try {
            ClearScrollRequest request = new ClearScrollRequest();
            request.scrollIds(scrollIds);
            execute(client -> client.clearScroll(request, RequestOptions.DEFAULT));
        } catch (Exception e) {
            logger.warn("Could not clear scroll: {}", e.getMessage());
        }
    }

    @Override
    protected MultiSearchResponse.Item[] getMultiSearchResult(MultiSearchRequest request) {
        MultiSearchResponse response = execute(client -> client.multiSearch(request, RequestOptions.DEFAULT));
        MultiSearchResponse.Item[] items = response.getResponses();
        Assert.isTrue(items.length == request.requests().size(), "Response should has same length with queries");
        return items;
    }

    @Override
    public long count(Query query, @Nullable Class<?> clazz, IndexCoordinates index) {
        Assert.notNull(query, "query must not be null");
        Assert.notNull(index, "index must not be null");

        final Boolean trackTotalHits = query.getTrackTotalHits();
        query.setTrackTotalHits(true);
        SearchRequest searchRequest = requestFactory.searchRequest(query, clazz, index);
        query.setTrackTotalHits(trackTotalHits);
        searchRequest.source().size(0);
        return SearchHitsUtil.getTotalCount(execute(client -> client.search(searchRequest, RequestOptions.DEFAULT).getHits()));
    }

    @Override
    public SearchResponse suggest(SuggestBuilder suggestion, IndexCoordinates index) {
        SearchRequest searchRequest = requestFactory.searchRequest(suggestion, index);
        return execute(client -> client.search(searchRequest, RequestOptions.DEFAULT));
    }

    @Override
    public <T> SearchHits<T> search(Query query, Class<T> clazz, IndexCoordinates index) {
        SearchRequest request = requestFactory.searchRequest(query, clazz, index);
        SearchResponse response = execute(client -> client.search(request, RequestOptions.DEFAULT));
        SearchDocumentResponseCallback<SearchHits<T>> callback = new ReadSearchDocumentResponseCallback<>(clazz, index);
        return callback.doWith(SearchDocumentResponse.from(response));
    }
    // endregion

    // region clientCallback
    @FunctionalInterface
    public interface ClientCallback<T> {
        T doWithClient(RestHighLevelClient client) throws IOException;
    }

    public <T> T execute(ClientCallback<T> callback) {
        Assert.notNull(callback, "callback must not be null");
        try {
            return callback.doWithClient(client);
        } catch (IOException | RuntimeException e) {
            throw translateException(e);
        }
    }

    private RuntimeException translateException(Exception e) {
        RuntimeException runtimeException = e instanceof RuntimeException ? (RuntimeException) e
                : new RuntimeException(e.getMessage(), e);
        RuntimeException potentialTranslatedException = exceptionTranslator.translateExceptionIfPossible(runtimeException);
        return potentialTranslatedException != null ? potentialTranslatedException : runtimeException;
    }
    // endregion
}
