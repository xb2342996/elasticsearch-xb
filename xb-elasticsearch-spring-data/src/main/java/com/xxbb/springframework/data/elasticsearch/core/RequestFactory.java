package com.xxbb.springframework.data.elasticsearch.core;

import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.document.Document;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import com.xxbb.springframework.data.elasticsearch.core.query.*;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.*;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.wrapperQuery;

public class RequestFactory {
    static final Integer INDEX_MAX_RESULT_WINDOW = 10_000;

    private final ElasticsearchConverter elasticsearchConverter;

    public RequestFactory(ElasticsearchConverter elasticsearchConverter) {
        this.elasticsearchConverter = elasticsearchConverter;
    }
    // region bulk
    public BulkRequest bulkRequest(List<?> queries, BulkOptions bulkOptions, IndexCoordinates index) {
        BulkRequest bulkRequest = new BulkRequest();

        if (bulkOptions.getTimeout() != null) {
            bulkRequest.timeout(bulkOptions.getTimeout());
        }

        if (bulkOptions.getRefreshPolicy() != null) {
            bulkRequest.setRefreshPolicy(bulkOptions.getRefreshPolicy());
        }

        if (bulkOptions.getWaitForActiveShards() != null) {
            bulkRequest.waitForActiveShards(bulkOptions.getWaitForActiveShards());
        }

        if (bulkOptions.getPipeline() != null) {
            bulkRequest.pipeline(bulkOptions.getPipeline());
        }

        if (bulkOptions.getRoutingId() != null) {
            bulkRequest.routing(bulkOptions.getRoutingId());
        }

        queries.forEach(query -> {
            if (query instanceof IndexQuery) {
                bulkRequest.add(indexRequest((IndexQuery) query, index));
            } else if (query instanceof UpdateQuery) {
                bulkRequest.add(updateRequest((UpdateQuery) query, index));
            }
        });
        return bulkRequest;
    }

    public BulkRequestBuilder bulkRequestBuilder(Client client, List<?> queries, BulkOptions bulkOptions, IndexCoordinates index) {
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

        if (bulkOptions.getTimeout() != null) {
            bulkRequestBuilder.setTimeout(bulkOptions.getTimeout());
        }

        if (bulkOptions.getRefreshPolicy() != null) {
            bulkRequestBuilder.setRefreshPolicy(bulkOptions.getRefreshPolicy());
        }

        if (bulkOptions.getWaitForActiveShards() != null) {
            bulkRequestBuilder.setWaitForActiveShards(bulkOptions.getWaitForActiveShards());
        }

        if (bulkOptions.getPipeline() != null) {
            bulkRequestBuilder.pipeline(bulkOptions.getPipeline());
        }

        if (bulkOptions.getRoutingId() != null) {
            bulkRequestBuilder.routing(bulkOptions.getRoutingId());
        }

        queries.forEach(query -> {
            if (query instanceof IndexQuery) {
                bulkRequestBuilder.add(indexRequest((IndexQuery) query, index));
            } else if (query instanceof UpdateQuery) {
                bulkRequestBuilder.add(updateRequest((UpdateQuery) query, index));
            }
        });
        return bulkRequestBuilder;
    }
    // endregion

    // region index
    public IndexRequest indexRequest(IndexQuery query, IndexCoordinates index) {
        String indexName = index.getIndexName();

        IndexRequest indexRequest;
        if (query.getObject() != null) {
            String id = StringUtils.isEmpty(query.getId()) ? getPersistentEntityId(query.getObject()) : query.getId();

            if (id != null) {
                indexRequest = new IndexRequest(indexName).id(id);
            } else {
                indexRequest = new IndexRequest(indexName);
            }
            indexRequest.source(elasticsearchConverter.mapObject(query.getObject()).toJson(), Requests.INDEX_CONTENT_TYPE);
        } else if (query.getSource() != null) {
            indexRequest = new IndexRequest(indexName).id(query.getId()).source(query.getSource(), Requests.INDEX_CONTENT_TYPE);
        } else {
            throw new ElasticsearchException("object or source is null, failed to index the document [id: " + query.getId() + "]");
        }

        if (query.getVersion() != null) {
            indexRequest.version(query.getVersion());
            VersionType versionType = retrieveVersionTypeFromPersistentEntity(query.getObject().getClass());
            indexRequest.versionType(versionType);
        }
        if (query.getSeqNo() != null) {
            indexRequest.setIfPrimaryTerm(query.getSeqNo());
        }
        if (query.getPrimaryTerm() != null) {
            indexRequest.setIfPrimaryTerm(query.getPrimaryTerm());
        }
        return indexRequest;
    }

    public IndexRequestBuilder indexRequestBuilder(Client client, IndexQuery query, IndexCoordinates index) {
        String indexName = index.getIndexName();
        String type = IndexCoordinates.TYPE;

        IndexRequestBuilder indexRequestBuilder;

        if (query.getObject() != null) {
            String id = StringUtils.isEmpty(query.getId()) ? getPersistentEntityId(query.getObject()) : query.getId();

            if (id != null) {
                indexRequestBuilder = client.prepareIndex(indexName, type, id);
            } else {
                indexRequestBuilder = client.prepareIndex(indexName, type);
            }
            indexRequestBuilder.setSource(elasticsearchConverter.mapObject(query.getObject()).toJson(), Requests.INDEX_CONTENT_TYPE);
        } else if (query.getSource() != null) {
            indexRequestBuilder = client.prepareIndex(indexName, type, query.getId()).setSource(query.getSource(), Requests.INDEX_CONTENT_TYPE);
        } else {
            throw new ElasticsearchException("object or source is null, failed to index the document [id: " + query.getId() + "]");
        }

        if (query.getVersion() != null) {
            indexRequestBuilder.setVersion(query.getVersion());
            VersionType versionType = retrieveVersionTypeFromPersistentEntity(query.getObject().getClass());
            indexRequestBuilder.setVersionType(versionType);
        }
        if (query.getSeqNo() != null) {
            indexRequestBuilder.setIfPrimaryTerm(query.getSeqNo());
        }
        if (query.getPrimaryTerm() != null) {
            indexRequestBuilder.setIfPrimaryTerm(query.getPrimaryTerm());
        }
        if (query.getRouting() != null) {
            indexRequestBuilder.setRouting(query.getRouting());
        }
        return indexRequestBuilder;
    }
    // endregion

    // region update
    public UpdateRequest updateRequest(UpdateQuery query, IndexCoordinates index) {
        UpdateRequest updateRequest = new UpdateRequest(index.getIndexName(), query.getId());

        if (query.getScript() != null) {
            Map<String, Object> params = query.getParams();

            if (params == null) {
                params = new HashMap<>();
            }
            Script script = new Script(ScriptType.INLINE, query.getLang(), query.getScript(), params);
            updateRequest.script(script);
        }

        if (query.getDocument() != null) {
            updateRequest.doc(query.getDocument());
        }

        if (query.getUpsert() != null) {
            updateRequest.upsert(query.getUpsert());
        }

        if (query.getRouting() != null) {
            updateRequest.routing(query.getRouting());
        }

        if (query.getScriptedUpsert() != null) {
            updateRequest.scriptedUpsert(query.getScriptedUpsert());
        }

        if (query.getDocAsUpsert() != null) {
            updateRequest.docAsUpsert(query.getDocAsUpsert());
        }

        if (query.getFetchSource() != null) {
            updateRequest.fetchSource(query.getFetchSource());
        }

        if (query.getFetchSourceIncludes() != null || query.getFetchSourceExcludes() != null) {
            List<String> includes = query.getFetchSourceIncludes() != null ? query.getFetchSourceIncludes() : Collections.emptyList();
            List<String> excludes = query.getFetchSourceExcludes() != null ? query.getFetchSourceExcludes() : Collections.emptyList();
            updateRequest.fetchSource(includes.toArray(new String[0]), excludes.toArray(new String[0]));
        }

        if (query.getIfSeqNo() != null) {
            updateRequest.setIfSeqNo(query.getIfSeqNo());
        }

        if (query.getIfPrimaryTerm() != null) {
            updateRequest.setIfPrimaryTerm(query.getIfPrimaryTerm());
        }

        if (query.getRefresh() != null) {
            updateRequest.setRefreshPolicy(query.getRefresh().name().toLowerCase());
        }

        if (query.getRetryOnConfilict() != null) {
            updateRequest.retryOnConflict(query.getRetryOnConfilict());
        }

        if (query.getTimeout() != null) {
            updateRequest.timeout(query.getTimeout());
        }
        if (query.getWaitForActiveShards() != null) {
            updateRequest.waitForActiveShards(ActiveShardCount.parseString(query.getWaitForActiveShards()));
        }
        return updateRequest;
    }

    public UpdateRequestBuilder updateRequestBuilderFor(Client client, UpdateQuery query, IndexCoordinates index) {
        String indexName = index.getIndexName();
        UpdateRequestBuilder updateRequestBuilder = client.prepareUpdate(indexName, IndexCoordinates.TYPE, query.getId());

        if (query.getScript() != null) {
            Map<String, Object> params = query.getParams();

            if (params == null) {
                params = new HashMap<>();
            }
            Script script = new Script(ScriptType.INLINE, query.getLang(), query.getScript(), params);
            updateRequestBuilder.setScript(script);
        }

        if (query.getDocument() != null) {
            updateRequestBuilder.setDoc(query.getDocument());
        }

        if (query.getUpsert() != null) {
            updateRequestBuilder.setUpsert(query.getUpsert());
        }

        if (query.getRouting() != null) {
            updateRequestBuilder.setRouting(query.getRouting());
        }

        if (query.getScriptedUpsert() != null) {
            updateRequestBuilder.setScriptedUpsert(query.getScriptedUpsert());
        }

        if (query.getDocAsUpsert() != null) {
            updateRequestBuilder.setDocAsUpsert(query.getDocAsUpsert());
        }

        if (query.getFetchSource() != null) {
            updateRequestBuilder.setFetchSource(query.getFetchSource());
        }

        if (query.getFetchSourceIncludes() != null || query.getFetchSourceExcludes() != null) {
            List<String> includes = query.getFetchSourceIncludes() != null ? query.getFetchSourceIncludes() : Collections.emptyList();
            List<String> excludes = query.getFetchSourceExcludes() != null ? query.getFetchSourceExcludes() : Collections.emptyList();
            updateRequestBuilder.setFetchSource(includes.toArray(new String[0]), excludes.toArray(new String[0]));
        }

        if (query.getIfSeqNo() != null) {
            updateRequestBuilder.setIfSeqNo(query.getIfSeqNo());
        }

        if (query.getIfPrimaryTerm() != null) {
            updateRequestBuilder.setIfPrimaryTerm(query.getIfPrimaryTerm());
        }

        if (query.getRefresh() != null) {
            updateRequestBuilder.setRefreshPolicy(query.getRefresh().name().toLowerCase());
        }

        if (query.getRetryOnConfilict() != null) {
            updateRequestBuilder.setRetryOnConflict(query.getRetryOnConfilict());
        }

        if (query.getTimeout() != null) {
            updateRequestBuilder.setTimeout(query.getTimeout());
        }
        if (query.getWaitForActiveShards() != null) {
            updateRequestBuilder.setWaitForActiveShards(ActiveShardCount.parseString(query.getWaitForActiveShards()));
        }
        return updateRequestBuilder;
    }
    // endregion

    // region delete
    public DeleteRequest deleteRequest(String id, @Nullable String routing, IndexCoordinates index) {
        String indexName = index.getIndexName();
        DeleteRequest deleteRequest = new DeleteRequest(indexName, id);
        if (routing != null) {
            deleteRequest.routing(routing);
        }
        return deleteRequest;
    }

    public DeleteRequestBuilder deleteRequestBuilder(Client client, String id, @Nullable String routing, IndexCoordinates index) {
        String indexName = index.getIndexName();
        DeleteRequestBuilder deleteRequestBuilder = client.prepareDelete();
        deleteRequestBuilder.setId(id);
        deleteRequestBuilder.setIndex(indexName);

        if (routing != null) {
            deleteRequestBuilder.setRouting(routing);
        }
        return deleteRequestBuilder;
    }

    public DeleteByQueryRequestBuilder deleteByQueryRequestBuilder(Client client, Query query, Class<?> clazz, IndexCoordinates index) {
        SearchRequest searchRequest = searchRequest(query, clazz, index);
        DeleteByQueryRequestBuilder deleteByQueryRequestBuilder = new DeleteByQueryRequestBuilder(client, DeleteByQueryAction.INSTANCE)
                .source(index.getIndexName())
                .filter(searchRequest.source().query())
                .abortOnVersionConflict(false)
                .refresh(true);

        SearchRequestBuilder source = deleteByQueryRequestBuilder.source();

        if (query.isLimiting()) {
            source.setSize(query.getMaxResults());
        }

        if (query.hasScrollTime()) {
            source.setScroll(TimeValue.timeValueMillis(query.getScrollTime().toMillis()));
        }

        if (query.getRoute() != null) {
            source.setRouting(query.getRoute());
        }
        return deleteByQueryRequestBuilder;
    }

    public DeleteByQueryRequest deleteByQueryRequest(Query query, Class<?> clazz, IndexCoordinates index) {
        SearchRequest searchRequest = searchRequest(query, clazz, index);
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(index.getIndexName())
                .setQuery(searchRequest.source().query())
                .setAbortOnVersionConflict(false)
                .setRefresh(true);

        if (query.isLimiting()) {
            deleteByQueryRequest.setBatchSize(query.getMaxResults());
        }

        if (query.hasScrollTime()) {
            deleteByQueryRequest.setScroll(TimeValue.timeValueMillis(query.getScrollTime().toMillis()));
        }

        if (query.getRoute() != null) {
            deleteByQueryRequest.setRouting(query.getRoute());
        }
        return deleteByQueryRequest;
    }
    // endregion


    // region search
    @Nullable
    public HighlightBuilder highlightBuilder(Query query) {
        HighlightBuilder highlightBuilder = query.getHighlightQuery().map(HighlightQuery::getHighlightBuilder).orElse(null);

        if (highlightBuilder == null) {
            if (query instanceof NativeSearchQuery) {
                NativeSearchQuery searchQuery = (NativeSearchQuery) query;

                if (searchQuery.getHighlightFields() != null || searchQuery.getHighlightBuilder() != null) {
                    highlightBuilder = searchQuery.getHighlightBuilder();
                    if (highlightBuilder == null) {
                        highlightBuilder = new HighlightBuilder();
                    }

                    if (searchQuery.getHighlightFields() != null) {
                        for (HighlightBuilder.Field field : searchQuery.getHighlightFields()) {
                            highlightBuilder.field(field);
                        }
                    }
                }
            }
        }

        return highlightBuilder;
    }

    public MoreLikeThisQueryBuilder moreLikeThisQueryBuilder(MoreLikeThisQuery query, IndexCoordinates index) {
        String indexName = index.getIndexName();
        MoreLikeThisQueryBuilder.Item item = new MoreLikeThisQueryBuilder.Item(indexName, query.getId());

        String[] fields = query.getFields().toArray(new String[]{});

        MoreLikeThisQueryBuilder moreLikeThisQueryBuilder = QueryBuilders.moreLikeThisQuery(fields, null,
                new MoreLikeThisQueryBuilder.Item[] {item});

        if (query.getMinDocFreq() != null) {
            moreLikeThisQueryBuilder.minDocFreq(query.getMinDocFreq());
        }

        if (query.getMaxDocFreq() != null) {
            moreLikeThisQueryBuilder.maxDocFreq(query.getMaxDocFreq());
        }

        if (query.getMinTermFreq() != null) {
            moreLikeThisQueryBuilder.minTermFreq(query.getMinTermFreq());
        }

        if (query.getMaxQueryTerm() != null) {
            moreLikeThisQueryBuilder.maxQueryTerms(query.getMaxQueryTerm());
        }

        if (!CollectionUtils.isEmpty(query.getStopWords())) {
            moreLikeThisQueryBuilder.stopWords(query.getStopWords());
        }

        if (query.getMinWordLen() != null) {
            moreLikeThisQueryBuilder.minWordLength(query.getMinWordLen());
        }

        if (query.getMaxWordLen() != null) {
            moreLikeThisQueryBuilder.maxWordLength(query.getMaxWordLen());
        }

        if (query.getBoostTerm() != null) {
            moreLikeThisQueryBuilder.boostTerms(query.getBoostTerm());
        }
        return moreLikeThisQueryBuilder;
    }

    public SearchRequest searchRequest(SuggestBuilder suggestion, IndexCoordinates index) {
        String[] indexNames = index.getIndexNames();
        SearchRequest searchRequest = new SearchRequest(indexNames);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.suggest(suggestion);
        searchRequest.source(sourceBuilder);
        return searchRequest;
    }

    public SearchRequestBuilder searchSourceBuilder(Client client, SuggestBuilder suggestion, IndexCoordinates index) {
        String[] indexNames = index.getIndexNames();
        return client.prepareSearch(indexNames).suggest(suggestion);
    }

    public SearchRequest searchRequest(Query query, @Nullable Class<?> clazz, IndexCoordinates index) {
        elasticsearchConverter.updateQuery(query, clazz);
        SearchRequest searchRequest = prepareSearchRequest(query, clazz, index);
        QueryBuilder elasticsearchQuery = getQuery(query);
        QueryBuilder elasticsearchFilter = getFilter(query);

        searchRequest.source().query(elasticsearchQuery);

        if (elasticsearchFilter != null) {
            searchRequest.source().postFilter(elasticsearchFilter);
        }
        return searchRequest;
    }

    public SearchRequestBuilder searchRequestBuilder(Client client, Query query, @Nullable Class<?> clazz, IndexCoordinates index) {
        elasticsearchConverter.updateQuery(query, clazz);
        SearchRequestBuilder searchRequestBuilder = prepareSearchRequestBuilder(query, client, clazz, index);
        QueryBuilder elasticsearchQuery = getQuery(query);
        QueryBuilder elasticsearchFilter = getFilter(query);

        searchRequestBuilder.setQuery(elasticsearchQuery);

        if (elasticsearchFilter != null) {
            searchRequestBuilder.setPostFilter(elasticsearchFilter);
        }
        return searchRequestBuilder;
    }

    private SearchRequest prepareSearchRequest(Query query, @Nullable Class<?> clazz, IndexCoordinates index) {
        String[] indexNames = index.getIndexNames();
        Assert.notNull(indexNames, "indexnames must not be null");
        Assert.notEmpty(indexNames, "IndexNames must have at least one element");
        SearchRequest request = new SearchRequest(indexNames);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.version(true);
        sourceBuilder.trackScores(query.getTrackScores());
        if (hasSeqNoPrimaryTermProperty(clazz)) {
            sourceBuilder.seqNoAndPrimaryTerm(true);
        }

        if (query.getSourceFilter() != null) {
            SourceFilter filter = query.getSourceFilter();
            sourceBuilder.fetchSource(filter.getIncludes(), filter.getExcludes());
        }

        if (query.getPageable().isPaged()) {
            sourceBuilder.from((int) query.getPageable().getOffset());
            sourceBuilder.size(query.getPageable().getPageSize());
        } else {
            sourceBuilder.from(0);
            sourceBuilder.size(INDEX_MAX_RESULT_WINDOW);
        }

        if (!query.getFields().isEmpty()) {
            sourceBuilder.fetchSource(query.getFields().toArray(new String[0]), null);
        }

        if (query.getIndicesOptions() != null) {
            request.indicesOptions(query.getIndicesOptions());
        }

        if (query.isLimiting()) {
            // noinspection ConstantConditions
            sourceBuilder.size(query.getMaxResults());
        }

        if (query.getMinScore() > 0) {
            sourceBuilder.minScore(query.getMinScore());
        }

        if (query.getPreference() != null) {
            request.preference(query.getPreference());
        }

        request.searchType(query.getSearchType());

        prepareSort(query, sourceBuilder, getPersistentEntity(clazz));

        HighlightBuilder highlightBuilder = highlightBuilder(query);

        if (highlightBuilder != null) {
            sourceBuilder.highlighter(highlightBuilder);
        }

        if (query instanceof NativeSearchQuery) {
            prepareNativeSearch((NativeSearchQuery) query, sourceBuilder);
        }

        if (query.getTrackTotalHits() != null){
            sourceBuilder.trackTotalHits(query.getTrackTotalHits());
        } else if (query.getTrackTotalHitsUpTo() != null) {
            sourceBuilder.trackTotalHitsUpTo(query.getTrackTotalHitsUpTo());
        }

        if (StringUtils.hasLength(query.getRoute())) {
            request.routing(query.getRoute());
        }

        request.source(sourceBuilder);
        return request;
    }

    private SearchRequestBuilder prepareSearchRequestBuilder(Query query, Client client, @Nullable Class<?> clazz, IndexCoordinates index) {
        String[] indexNames = index.getIndexNames();
        Assert.notNull(indexNames, "indexnames must not be null");
        Assert.notEmpty(indexNames, "IndexNames must have at least one element");

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexNames)
                .setSearchType(query.getSearchType())
                .setVersion(true)
                .setTrackScores(query.getTrackScores());

        if (hasSeqNoPrimaryTermProperty(clazz)) {
            searchRequestBuilder.seqNoAndPrimaryTerm(true);
        }

        if (query.getSourceFilter() != null) {
            SourceFilter filter = query.getSourceFilter();
            searchRequestBuilder.setFetchSource(filter.getIncludes(), filter.getExcludes());
        }

        if (query.getPageable().isPaged()) {
            searchRequestBuilder.setFrom((int) query.getPageable().getOffset());
            searchRequestBuilder.setSize(query.getPageable().getPageSize());
        } else {
            searchRequestBuilder.setFrom(0);
            searchRequestBuilder.setSize(INDEX_MAX_RESULT_WINDOW);
        }

        if (!query.getFields().isEmpty()) {
            searchRequestBuilder.setFetchSource(query.getFields().toArray(new String[0]), null);
        }

        if (query.getIndicesOptions() != null) {
            searchRequestBuilder.setIndicesOptions(query.getIndicesOptions());
        }

        if (query.isLimiting()) {
            // noinspection ConstantConditions
            searchRequestBuilder.setSize(query.getMaxResults());
        }

        if (query.getMinScore() > 0) {
            searchRequestBuilder.setMinScore(query.getMinScore());
        }

        if (query.getPreference() != null) {
            searchRequestBuilder.setPreference(query.getPreference());
        }

        prepareSort(query, searchRequestBuilder, getPersistentEntity(clazz));

        HighlightBuilder highlightBuilder = highlightBuilder(query);

        if (highlightBuilder != null) {
            searchRequestBuilder.highlighter(highlightBuilder);
        }

        if (query instanceof NativeSearchQuery) {
            prepareNativeSearch(searchRequestBuilder, (NativeSearchQuery) query);
        }

        if (query.getTrackTotalHits() != null){
            searchRequestBuilder.setTrackTotalHits(query.getTrackTotalHits());
        } else if (query.getTrackTotalHitsUpTo() != null) {
            searchRequestBuilder.setTrackTotalHitsUpTo(query.getTrackTotalHitsUpTo());
        }

        if (StringUtils.hasLength(query.getRoute())) {
            searchRequestBuilder.setRouting(query.getRoute());
        }

        return searchRequestBuilder;
    }

    private void prepareNativeSearch(NativeSearchQuery query, SearchSourceBuilder builder) {
        if (!query.getScriptFields().isEmpty()) {
            for (ScriptField scriptField : query.getScriptFields()) {
                builder.scriptField(scriptField.getFieldName(), scriptField.getScript());
            }
        }

        if (query.getCollapseBuilder() != null) {
            builder.collapse(query.getCollapseBuilder());
        }

        if (!CollectionUtils.isEmpty(query.getIndicesBoost())) {
            for (IndexBoost indexBoost : query.getIndicesBoost()) {
                builder.indexBoost(indexBoost.getIndexName(), indexBoost.getBoost());
            }
        }

        if (!CollectionUtils.isEmpty(query.getAggregations())) {
            for (AbstractAggregationBuilder<?> aggregationBuilder: query.getAggregations()){
                builder.aggregation(aggregationBuilder);
            }
        }
    }

    private void prepareNativeSearch(SearchRequestBuilder searchRequestBuilder, NativeSearchQuery query) {
        if (!query.getScriptFields().isEmpty()) {
            for (ScriptField scriptField : query.getScriptFields()) {
                searchRequestBuilder.addScriptField(scriptField.getFieldName(), scriptField.getScript());
            }
        }

        if (query.getCollapseBuilder() != null) {
            searchRequestBuilder.setCollapse(query.getCollapseBuilder());
        }

        if (!CollectionUtils.isEmpty(query.getIndicesBoost())) {
            for (IndexBoost indexBoost : query.getIndicesBoost()) {
                searchRequestBuilder.addIndexBoost(indexBoost.getIndexName(), indexBoost.getBoost());
            }
        }

        if (!CollectionUtils.isEmpty(query.getAggregations())) {
            for (AbstractAggregationBuilder<?> aggregationBuilder: query.getAggregations()){
                searchRequestBuilder.addAggregation(aggregationBuilder);
            }
        }
    }

    private void prepareSort(Query query, SearchSourceBuilder sourceBuilder, @Nullable ElasticsearchPersistentEntity<?> entity) {

        if (query.getSort() != null) {
            query.getSort().forEach(order -> sourceBuilder.sort(getSortBuilder(order, entity)));
        }

        if (query instanceof NativeSearchQuery) {
            NativeSearchQuery searchQuery = (NativeSearchQuery) query;
            List<SortBuilder> sorts = searchQuery.getElasticsearchSorts();
            if (sorts != null) {
                sorts.forEach(sourceBuilder::sort);
            }
        }
    }

    private void prepareSort(Query query, SearchRequestBuilder searchRequestBuilder, @Nullable ElasticsearchPersistentEntity<?> entity) {
        if (query.getSort() != null) {
            query.getSort().forEach(order -> searchRequestBuilder.addSort(getSortBuilder(order, entity)));
        }

        if (query instanceof NativeSearchQuery) {
            NativeSearchQuery searchQuery = (NativeSearchQuery) query;
            List<SortBuilder> sorts = searchQuery.getElasticsearchSorts();
            if (sorts != null) {
                sorts.forEach(searchRequestBuilder::addSort);
            }
        }
    }

    private SortBuilder<?> getSortBuilder(Sort.Order order, @Nullable ElasticsearchPersistentEntity<?> entity) {
        SortOrder sortOrder = order.getDirection().isDescending() ? SortOrder.DESC : SortOrder.ASC;

        if (ScoreSortBuilder.NAME.equals(order.getProperty())) {
            return SortBuilders.scoreSort().order(sortOrder);
        } else {
            ElasticsearchPersistentProperty property = (entity != null) ?entity.getPersistentProperty(order.getProperty()) : null;
            String fieldName = property != null ? property.getFieldName() : order.getProperty();

            FieldSortBuilder sort = SortBuilders.fieldSort(fieldName).order(sortOrder);
            if (order.getNullHandling() == Sort.NullHandling.NULLS_FIRST) {
                sort.missing("_first");
            } else if (order.getNullHandling() == Sort.NullHandling.NULLS_LAST) {
                sort.missing("_last");
            }
            return sort;
        }
    }
    // endregion

    // region index management
    public CreateIndexRequest createIndexRequest(String indexName, @Nullable Document settings) {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        if (settings != null) {
            request.settings(settings);
        }
        return request;
    }

    public CreateIndexRequestBuilder createIndexRequestBuilder(Client client, IndexCoordinates index, @Nullable Document settings) {
        String indexName = index.getIndexName();
        CreateIndexRequestBuilder builder = client.admin().indices().prepareCreate(indexName);
        if (settings != null) {
            builder.setSettings(settings);
        }
        return builder;
    }

    public GetIndexRequest getIndexRequest(IndexCoordinates index) {
        return new GetIndexRequest(index.getIndexName());
    }

    public IndicesExistsRequest indicesExistsRequest(IndexCoordinates index) {
        String[] indexNames = index.getIndexNames();
        return new IndicesExistsRequest(indexNames);
    }

    public DeleteIndexRequest deleteIndexRequest(IndexCoordinates index) {
        String[] indexNames = index.getIndexNames();
        return new DeleteIndexRequest(indexNames);
    }

    public RefreshRequest refreshRequest(IndexCoordinates index) {
        String[] indexNames = index.getIndexNames();
        return new RefreshRequest(indexNames);
    }

    public GetSettingsRequest getSettingsRequest(IndexCoordinates index, boolean includeDefaults) {
        String[] indexNames = index.getIndexNames();
        return new GetSettingsRequest().indices(indexNames).includeDefaults(includeDefaults);
    }

    public PutMappingRequest putMappingRequest(IndexCoordinates index, Document mapping) {
        PutMappingRequest request = new PutMappingRequest(index.getIndexNames());
        request.source(mapping);
        return request;
    }

    public PutMappingRequestBuilder putMappingRequestBuilder(Client client, IndexCoordinates index, Document mapping) {
        PutMappingRequestBuilder builder = client.admin().indices().preparePutMapping(index.getIndexName()).setType(IndexCoordinates.TYPE);
        builder.setSource(mapping);
        return builder;
    }

    public GetSettingsRequest getSettingsRequest(String indexName, boolean includeDefaults) {
        return new GetSettingsRequest().indices(indexName).includeDefaults(includeDefaults);
    }

    public GetMappingsRequest getMappingsRequest(Client client, IndexCoordinates index) {
        String[] indexNames = index.getIndexNames();
        return new GetMappingsRequest().indices(indexNames);
    }

    public GetMappingsRequest getMappingsRequest(IndexCoordinates index) {
        String[] indexNames = index.getIndexNames();
        return new GetMappingsRequest().indices(indexNames);
    }
    // endregion

    // region get
    public GetRequest getRequest(String id, IndexCoordinates index) {
        return new GetRequest(index.getIndexName(), id);
    }

    public GetRequestBuilder getRequestBuilder(Client client, String id, IndexCoordinates index) {
        return client.prepareGet(index.getIndexName(), null, id);
    }

    public MultiGetRequest multiGetRequest(Query query, Class<?> clazz, IndexCoordinates index) {
        MultiGetRequest multiGetRequest = new MultiGetRequest();
        getMultiRequestItem(query, clazz, index).forEach(multiGetRequest::add);
        return multiGetRequest;
    }

    public MultiGetRequestBuilder multiGetRequestBuilder(Client client, Query searchQuery, Class<?> clazz, IndexCoordinates index) {
        MultiGetRequestBuilder multiGetRequestBuilder = client.prepareMultiGet();
        getMultiRequestItem(searchQuery, clazz, index).forEach(multiGetRequestBuilder::add);
        return multiGetRequestBuilder;
    }

    private List<MultiGetRequest.Item> getMultiRequestItem(Query searchQuery, Class<?> clazz, IndexCoordinates index) {
        elasticsearchConverter.updateQuery(searchQuery, clazz);
        List<MultiGetRequest.Item> items = new ArrayList<>();

        if (!CollectionUtils.isEmpty(searchQuery.getFields())) {
            searchQuery.addSourceFilter(new FetchSourceFilter(toArray(searchQuery.getFields()), null));
        }

        if (!CollectionUtils.isEmpty(searchQuery.getIds())) {
            String indexName = index.getIndexName();
            for (String id : searchQuery.getIds()) {
                MultiGetRequest.Item item = new MultiGetRequest.Item(indexName, id);
                if (searchQuery.getRoute() != null) {
                    item = item.routing(searchQuery.getRoute());
                }
                items.add(item);
            }
        }

        return items;
    }
    // endregion


    // region helper
    private QueryBuilder getQuery(Query query) {
        QueryBuilder elasticsearchQuery;

        if (query instanceof NativeSearchQuery) {
            NativeSearchQuery nativeSearchQuery = (NativeSearchQuery) query;
            elasticsearchQuery = nativeSearchQuery.getQuery();
        } else if (query instanceof CriteriaQuery) {
            CriteriaQuery criteriaQuery = (CriteriaQuery) query;
            elasticsearchQuery = new CriteriaQueryProcessor().createQuery(criteriaQuery.getCriteria());
        } else if (query instanceof StringQuery) {
            StringQuery stringQuery = (StringQuery) query;
            elasticsearchQuery = wrapperQuery(stringQuery.getSource());
        } else {
            throw new IllegalArgumentException("unhandled Query implemention " + query.getClass().getName());
        }
        return elasticsearchQuery;
    }

    @Nullable
    private QueryBuilder getFilter(Query query) {
        QueryBuilder elasticsearchFilter;

        if (query instanceof NativeSearchQuery) {
            NativeSearchQuery nativeSearchQuery = (NativeSearchQuery) query;
            elasticsearchFilter = nativeSearchQuery.getFilter();
        } else if (query instanceof CriteriaQuery) {
            CriteriaQuery criteriaQuery = (CriteriaQuery) query;
            elasticsearchFilter = new CriteriaFilterProcessor().createFilter(criteriaQuery.getCriteria());
        } else if (query instanceof StringQuery) {
            elasticsearchFilter = null;
        } else {
            throw new IllegalArgumentException("unhandled Query implemention " + query.getClass().getName());
        }
        return elasticsearchFilter;
    }

    public Document fromSettingsResponse(GetSettingsResponse response, String indexName) {
        Document settings = Document.create();

        if (!response.getIndexToDefaultSettings().isEmpty()) {
            Settings defaultSettings = response.getIndexToDefaultSettings().get(indexName);
            for (String key : defaultSettings.keySet()) {
                settings.put(key, defaultSettings.get(key));
            }
        }

        if (!response.getIndexToSettings().isEmpty()) {
            Settings customSettings = response.getIndexToSettings().get(indexName);
            for (String key : customSettings.keySet()) {
                settings.put(key, customSettings.get(key));
            }
        }
        return settings;
    }

    @Nullable
    private ElasticsearchPersistentEntity<?> getPersistentEntity(@Nullable Class<?> clazz) {
        return clazz != null ? elasticsearchConverter.getMappingContext().getPersistentEntity(clazz) : null;
    }

    @Nullable
    private String getPersistentEntityId(Object entity) {
        Object identifier = elasticsearchConverter.getMappingContext()
                .getRequiredPersistentEntity(entity.getClass())
                .getIdentifierAccessor(entity).getIdentifier();

        if (identifier != null) {
            return identifier.toString();
        }
        return null;
    }

    private VersionType retrieveVersionTypeFromPersistentEntity(Class<?> clazz) {
        if (clazz != null) {
            return elasticsearchConverter.getMappingContext().getRequiredPersistentEntity(clazz).getVersionType();
        }
        return VersionType.EXTERNAL;
    }

    private String[] toArray(List<String> values) {
        String[] valuesAsArray = new String[values.size()];
        return values.toArray(valuesAsArray);
    }

    private boolean hasSeqNoPrimaryTermProperty(@Nullable Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        if (!elasticsearchConverter.getMappingContext().hasPersistentEntityFor(clazz)) {
            return false;
        }

        ElasticsearchPersistentEntity<?> entity = elasticsearchConverter.getMappingContext().getRequiredPersistentEntity(clazz);
        return entity.hasSeqNoPrimaryTermProperty();
    }
    // endregion
}
