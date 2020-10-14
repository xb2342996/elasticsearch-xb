package com.xxbb.springframework.data.elasticsearch.core.query;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NativeSearchQuery extends AbstractQuery{
    private QueryBuilder query;
    @Nullable private QueryBuilder filter;
    @Nullable private List<SortBuilder> sorts;
    private final List<ScriptField> scriptFields = new ArrayList<>();
    @Nullable private CollapseBuilder collapseBuilder;
    @Nullable private List<AbstractAggregationBuilder> aggregations;
    @Nullable private HighlightBuilder highlightBuilder;
    @Nullable private HighlightBuilder.Field[] highlightFields;
    @Nullable private List<IndexBoost> indicesBoost;

    public NativeSearchQuery(QueryBuilder query) {
        this.query = query;
    }

    public NativeSearchQuery(QueryBuilder query, @Nullable QueryBuilder filter) {
        this.query = query;
        this.filter = filter;
    }

    public NativeSearchQuery(QueryBuilder query, @Nullable QueryBuilder filter, @Nullable List<SortBuilder> sorts) {
        this.query = query;
        this.filter = filter;
        this.sorts = sorts;
    }

    public NativeSearchQuery(QueryBuilder query, @Nullable QueryBuilder filter, @Nullable List<SortBuilder> sorts, HighlightBuilder.Field[] highlightFields) {
        this.query = query;
        this.filter = filter;
        this.sorts = sorts;
        this.highlightFields = highlightFields;
    }

    public NativeSearchQuery(QueryBuilder query, @Nullable QueryBuilder filter, @Nullable List<SortBuilder> sorts, @Nullable HighlightBuilder highlightBuilder, @Nullable HighlightBuilder.Field[] highlightFields) {
        this.query = query;
        this.filter = filter;
        this.sorts = sorts;
        this.highlightBuilder = highlightBuilder;
        this.highlightFields = highlightFields;
    }

    public QueryBuilder getQuery() {
        return query;
    }

    @Nullable
    public QueryBuilder getFilter() {
        return filter;
    }

    @Nullable
    public List<SortBuilder> getElasticsearchSorts() {
        return sorts;
    }

    @Nullable
    public HighlightBuilder getHighlightBuilder() {
        return highlightBuilder;
    }

    @Nullable
    public HighlightBuilder.Field[] getHighlightFields() {
        return highlightFields;
    }


    public List<ScriptField> getScriptFields() {
        return scriptFields;
    }

    public void setScriptFields(List<ScriptField> scriptFields) {
        this.scriptFields.addAll(scriptFields);
    }

    public void addScriptFields(ScriptField... scriptFields) {
        this.scriptFields.addAll(Arrays.asList(scriptFields));
    }

    @Nullable
    public CollapseBuilder getCollapseBuilder() {
        return collapseBuilder;
    }

    public void setCollapseBuilder(@Nullable CollapseBuilder collapseBuilder) {
        this.collapseBuilder = collapseBuilder;
    }

    @Nullable
    public List<AbstractAggregationBuilder> getAggregations() {
        return aggregations;
    }

    public void addAggregation(AbstractAggregationBuilder aggregationBuilder) {
        if (aggregations == null) {
            this.aggregations = new ArrayList<>();
        }
        this.aggregations.add(aggregationBuilder);
    }

    public void setAggregations(@Nullable List<AbstractAggregationBuilder> aggregations) {
        this.aggregations = aggregations;
    }

    @Nullable
    public List<IndexBoost> getIndicesBoost() {
        return indicesBoost;
    }

    public void setIndicesBoost(@Nullable List<IndexBoost> indicesBoost) {
        this.indicesBoost = indicesBoost;
    }
}

