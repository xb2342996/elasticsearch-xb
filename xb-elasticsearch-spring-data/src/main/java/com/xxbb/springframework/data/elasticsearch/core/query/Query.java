package com.xxbb.springframework.data.elasticsearch.core.query;

import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.IndicesOptions;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface Query {
    int DEFAULT_PAGE_SIZE = 10;
    Pageable DEFAULT_PAGE = PageRequest.of(0, DEFAULT_PAGE_SIZE);

//    static Query findAll() {
//    }

    <T extends Query> T setPageable(Pageable pageable);

    Pageable getPageable();

    <T extends Query> T addSort(Sort sort);

    @Nullable
    Sort getSort();

    void addFields(String... fields);

    List<String> getFields();

    void addSourceFilter(SourceFilter sourceFilter);

    @Nullable
    SourceFilter getSourceFilter();

    float getMinScore();

    boolean getTrackScores();

    @Nullable
    Collection<String> getIds();

    @Nullable
    String getRoute();

    SearchType getSearchType();

    @Nullable
    IndicesOptions getIndicesOptions();

    @Nullable
    String getPreference();

    void setPreference(String preference);

    default boolean isLimiting() {
        return false;
    }

    @Nullable
    default Integer getMaxResults() {
        return null;
    }

    void setHighlightQuery(HighlightQuery highlightQuery);

    default Optional<HighlightQuery> getHighlightQuery() {
        return Optional.empty();
    }

    void setTrackTotalHits(Boolean trackTtoalHits);

    @Nullable
    Boolean getTrackTotalHits();

    void setTrackTotalHitsUpTo(@Nullable Integer trackTotalHitsUpTo);

    @Nullable
    Integer getTrackTotalHitsUpTo();

    @Nullable
    Duration getScrollTime();

    void setScrollTime(@Nullable Duration scrollTime);

    default boolean hasScrollTime() {
        return getScrollTime() != null;
    }
}
