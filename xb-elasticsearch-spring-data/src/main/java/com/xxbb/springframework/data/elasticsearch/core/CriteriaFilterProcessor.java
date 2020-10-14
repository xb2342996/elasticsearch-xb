package com.xxbb.springframework.data.elasticsearch.core;

import static com.xxbb.springframework.data.elasticsearch.core.query.Criteria.*;

import com.xxbb.springframework.data.elasticsearch.core.query.Criteria;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CriteriaFilterProcessor {

    @Nullable
    QueryBuilder createFilter(Criteria criteria) {
        List<QueryBuilder> filterBuilders = new ArrayList<>();

        for (Criteria chainedCriteria : criteria.getCriteriaChain()) {
            if (chainedCriteria.isOr()) {
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                queriesForEntities(chainedCriteria).forEach(boolQuery::should);
                filterBuilders.add(boolQuery);
            } else if (chainedCriteria.isNegating()) {
                List<QueryBuilder> negationFilters = buildeNegationFilter(criteria.getField().getName(), criteria.getFilterCriteriaEntries().iterator());
                filterBuilders.addAll(negationFilters);
            } else {
                filterBuilders.addAll(queriesForEntities(chainedCriteria));
            }
        }

        QueryBuilder filter = null;
        if (!filterBuilders.isEmpty()) {
            if (filterBuilders.size() == 1) {
                filter = filterBuilders.get(0);
            } else {
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                filterBuilders.forEach(boolQuery::must);
                filter = boolQuery;
            }
        }
        return filter;
    }

    private List<QueryBuilder> queriesForEntities(Criteria criteria) {
        Assert.notNull(criteria.getField(), "criteria must have a field");
        String fieldName = criteria.getField().getName();
        Assert.notNull(fieldName, "unknownField");

        return criteria.getFilterCriteriaEntries().stream().map(entry -> queryFor(entry.getKey(), entry.getValue(), fieldName)).collect(Collectors.toList());
    }

    @Nullable
    private QueryBuilder queryFor(OperationKey key, Object value, String fieldName) {
        QueryBuilder filter = null;

        switch (key) {
            case WITHIN:
                throw new UnsupportedOperationException("Geo class is not support yet!");
            case BBOX:
                throw new UnsupportedOperationException("Geo class is not support yet!");
        }
        return filter;
    }

    private void extractDistanceString(Distance distance, StringBuilder sb) {
        sb.append((int) distance.getValue());

        Metrics metric = (Metrics) distance.getMetric();

        switch (metric) {
            case KILOMETERS:
                sb.append("km");
                break;
            case MILES:
                sb.append("mi");
                break;
        }
    }

    private void oneParameterBBox(GeoBoundingBoxQueryBuilder filter, Object value) {
        throw new UnsupportedOperationException("oneParameterBBox is not supported yet");
    }

    private static boolean isType(Object[] array, Class<?> clazz) {
        for (Object o : array) {
            if (!clazz.isInstance(o)) {
                return false;
            }
        }
        return true;
    }

    private void twoParameterBBox(GeoBoundingBoxQueryBuilder filter, Object[] values) {
        throw new UnsupportedOperationException("twoParameterBBox is not supported yet");
    }

    private List<QueryBuilder> buildeNegationFilter(String fieldName, Iterator<CriteriaEntry> it) {
        List<QueryBuilder> notFilterList = new LinkedList<>();
        while (it.hasNext()) {
            CriteriaEntry criteriaEntry = it.next();
            QueryBuilder notFilter = QueryBuilders.boolQuery().mustNot(queryFor(criteriaEntry.getKey(), criteriaEntry.getValue(), fieldName));
            notFilterList.add(notFilter);
        }

        return notFilterList;
    }
}
