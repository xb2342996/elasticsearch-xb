package com.xxbb.springframework.data.elasticsearch.repository.query;

import com.xxbb.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

public abstract class AbstractElasticsearchRepositoryQuery implements RepositoryQuery {
    protected static final int DEFAULT_STREAM_BATCH_SIZE = 500;
    protected ElasticsearchQueryMethod queryMethod;
    protected ElasticsearchOperations elasticsearchOperations;

    public AbstractElasticsearchRepositoryQuery(ElasticsearchQueryMethod queryMethod, ElasticsearchOperations elasticsearchOperations) {
        this.queryMethod = queryMethod;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }
}
