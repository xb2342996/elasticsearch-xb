package com.xxbb.springframework.data.elasticsearch.core.query;

import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.*;

abstract class AbstractQuery implements Query{
    protected Pageable pageable = DEFAULT_PAGE;
    @Nullable protected Sort sort;
    protected List<String> fields = new ArrayList<>();
    @Nullable protected SourceFilter sourceFilter;
    protected float minScore;
    @Nullable protected Collection<String> ids;
    @Nullable protected String route;
    protected SearchType searchType = SearchType.DFS_QUERY_THEN_FETCH;
    @Nullable protected IndicesOptions indicesOptions;
    protected boolean trackScores;
    @Nullable protected String preference;
    @Nullable protected Integer maxResults;
    @Nullable protected HighlightQuery highlightQuery;
    @Nullable private Boolean trackTotalHits;
    @Nullable private Integer trackTotalHitsUpTo;
    @Nullable private Duration scrollTime;

    @Override
    public Pageable getPageable() {
        return pageable;
    }

    @Override
    public final <T extends Query> T setPageable(Pageable pageable) {
        this.pageable = pageable;
        return (T) this.addSort(pageable.getSort());
    }

    @Override
    @Nullable
    public Sort getSort() {
        return sort;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Query> T addSort(Sort sort) {
        if (sort == null) {
            return (T) this;
        }
        if (this.sort == null) {
            this.sort = sort;
        } else {
            this.sort = this.sort.and(sort);
        }
        return (T) this;
    }

    public void setSort(@Nullable Sort sort) {
        this.sort = sort;
    }

    @Override
    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> field) {
        this.fields = field;
    }

    @Override
    @Nullable
    public SourceFilter getSourceFilter() {
        return sourceFilter;
    }

    @Override
    public float getMinScore() {
        return minScore;
    }

    public void setMinScore(float minScore) {
        this.minScore = minScore;
    }

    @Override
    @Nullable
    public Collection<String> getIds() {
        return ids;
    }

    public void setIds(@Nullable Collection<String> ids) {
        this.ids = ids;
    }

    @Override
    @Nullable
    public String getRoute() {
        return route;
    }

    public void setRoute(@Nullable String route) {
        this.route = route;
    }

    @Override
    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    @Override
    @Nullable
    public IndicesOptions getIndicesOptions() {
        return indicesOptions;
    }

    public void setIndicesOptions(@Nullable IndicesOptions indicesOptions) {
        this.indicesOptions = indicesOptions;
    }


    public void setTrackScores(boolean trackScores) {
        this.trackScores = trackScores;
    }

    @Override
    @Nullable
    public String getPreference() {
        return preference;
    }

    public void setPreference(@Nullable String preference) {
        this.preference = preference;
    }

    @Override
    @Nullable
    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(@Nullable Integer maxResults) {
        this.maxResults = maxResults;
    }


    public void setTrackTotalHits(@Nullable Boolean trackTotalHits) {
        this.trackTotalHits = trackTotalHits;
    }

    @Override
    @Nullable
    public Duration getScrollTime() {
        return scrollTime;
    }

    @Override
    public void setScrollTime(@Nullable Duration scrollTime) {
        this.scrollTime = scrollTime;
    }

    @Override
    public void setHighlightQuery(HighlightQuery highlightQuery) {
        this.highlightQuery = highlightQuery;
    }

    @Override
    public Optional<HighlightQuery> getHighlightQuery() {
        return Optional.ofNullable(highlightQuery);
    }

    @Override
    public void addFields(String... fields) {
        Collections.addAll(this.fields);
    }


    @Override
    public void addSourceFilter(SourceFilter sourceFilter) {
        this.sourceFilter = sourceFilter;
    }

    @Override
    public boolean getTrackScores() {
        return trackScores;
    }


    @Override
    public boolean isLimiting() {
        return maxResults != null;
    }


    @Override
    @Nullable
    public Boolean getTrackTotalHits() {
        return trackTotalHits;
    }

    @Override
    public void setTrackTotalHitsUpTo(@Nullable Integer trackTotalHitsUpTo) {
        this.trackTotalHitsUpTo = trackTotalHitsUpTo;
    }

    @Nullable
    @Override
    public Integer getTrackTotalHitsUpTo() {
        return trackTotalHitsUpTo;
    }
}
