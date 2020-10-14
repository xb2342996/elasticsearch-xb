package com.xxbb.springframework.data.elasticsearch.repository.query;

import com.xxbb.springframework.data.elasticsearch.core.*;
import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import com.xxbb.springframework.data.elasticsearch.core.query.CriteriaQuery;
import com.xxbb.springframework.data.elasticsearch.repository.query.parser.ElasticsearchQueryCreator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.data.util.StreamUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.Collections;

public class ElasticsearchPartQuery extends AbstractElasticsearchRepositoryQuery{

    private final PartTree tree;
    private final ElasticsearchConverter elasticsearchConverter;
    private final MappingContext<?, ElasticsearchPersistentProperty> mappingContext;

    public ElasticsearchPartQuery(ElasticsearchQueryMethod queryMethod, ElasticsearchOperations elasticsearchOperations) {
        super(queryMethod, elasticsearchOperations);
        this.tree = new PartTree(queryMethod.getName(), queryMethod.getEntityInformation().getJavaType());
        this.elasticsearchConverter = elasticsearchOperations.getElasticsearchConverter();
        this.mappingContext = elasticsearchConverter.getMappingContext();
    }

    @Override
    public Object execute(Object[] parameters) {
        Class<?> clazz = queryMethod.getEntityInformation().getJavaType();
        ParametersParameterAccessor accessor = new ParametersParameterAccessor(queryMethod.getParameters(), parameters);
        CriteriaQuery query = createQuery(accessor);

        Assert.notNull(query, "unsupported query");

        elasticsearchConverter.updateQuery(query, clazz);
        if (queryMethod.hasAnnotatedHighlight()) {
            query.setHighlightQuery(queryMethod.getAnnotatedHighlightQuery());
        }

        IndexCoordinates index = elasticsearchOperations.getIndexCoordinatesFor(clazz);

        Object result = null;

        if (tree.isLimiting()) {
            query.setMaxResults(tree.getMaxResults());
        }

        if (tree.isDelete()) {
            result = countOrGetDocumentsForDelete(query, accessor);
            elasticsearchOperations.delete(query, clazz, index);
            elasticsearchOperations.indexOps(clazz).refresh();
        } else if (queryMethod.isPageQuery()) {
            query.setPageable(accessor.getPageable());
            SearchHits<?> searchHits = elasticsearchOperations.search(query, clazz, index);
            if (queryMethod.isSearchPageMethod()) {
                result = SearchHitSupport.searchPageFor(searchHits, query.getPageable());
            } else {
                result = SearchHitSupport.page(searchHits, query.getPageable());
            }
        } else if (queryMethod.isStreamQuery()) {
            if (accessor.getPageable().isUnpaged()) {
                query.setPageable(PageRequest.of(0, DEFAULT_STREAM_BATCH_SIZE));
            } else {
                query.setPageable(accessor.getPageable());
            }
            result = StreamUtils.createStreamFromIterator(elasticsearchOperations.searchForStream(query, clazz, index));
        } else if (queryMethod.isCollectionQuery()) {
            if (accessor.getPageable().isUnpaged()) {
                int itemCount = (int) elasticsearchOperations.count(query, clazz, index);

                if (itemCount == 0) {
                    result = new SearchHitsImpl<>(0, TotalHitsRelation.EQUAL_TO, Float.NaN, null, Collections.emptyList(), null );
                } else {
                    query.setPageable(PageRequest.of(0, Math.max(1, itemCount)));
                }
            } else {
                query.setPageable(accessor.getPageable());
            }

            if (result == null) {
                result = elasticsearchOperations.search(query, clazz, index);
            }
        } else if (tree.isCountProjection()) {
            result = elasticsearchOperations.count(query, clazz, index);
        } else {
            result = elasticsearchOperations.searchOne(query, clazz, index);
        }
        return queryMethod.isNotSearchHitMethod() ? SearchHitSupport.unwrapSearchHits(result) : result;
    }

    @Nullable
    private Object countOrGetDocumentsForDelete(CriteriaQuery query, ParametersParameterAccessor accessor) {
        Object result = null;
        Class<?> clazz = queryMethod.getEntityInformation().getJavaType();
        IndexCoordinates index = elasticsearchOperations.getIndexCoordinatesFor(clazz);

        if (queryMethod.isCollectionQuery()) {
            if (accessor.getPageable().isUnpaged()) {
                int itemCount = (int) elasticsearchOperations.count(query, clazz, index);
                query.setPageable(PageRequest.of(0, Math.max(1, itemCount)));
            } else {
                query.setPageable(accessor.getPageable());
            }
            result = elasticsearchOperations.search(query, clazz, index);
        }

        if (ClassUtils.isAssignable(Number.class, queryMethod.getReturnedObjectType())) {
            result = elasticsearchOperations.count(query, clazz, index);
        }
        return result;
    }


    public CriteriaQuery createQuery(ParametersParameterAccessor accessor) {
        return new ElasticsearchQueryCreator(tree, accessor, mappingContext).createQuery();
    }
}
