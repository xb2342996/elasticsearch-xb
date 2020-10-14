package com.xxbb.springframework.data.elasticsearch.core.query;

import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.xxbb.springframework.data.elasticsearch.core.query.Query.DEFAULT_PAGE;

public class MoreLikeThisQuery {
    @Nullable
    private String id;
    private final List<String> searchIndices = new ArrayList<>();
    private final List<String> searchTypes = new ArrayList<>();
    private final List<String> fields = new ArrayList<>();
    @Nullable
    private String routing;
    @Nullable
    private Float percentTermToMatch;
    @Nullable
    private Integer minTermFreq;
    @Nullable
    private Integer maxQueryTerm;
    private final List<String> stopWords = new ArrayList<>();
    @Nullable private Integer minDocFreq;
    @Nullable private Integer maxDocFreq;
    @Nullable private Integer minWordLen;
    @Nullable private Integer maxWordLen;
    @Nullable private Float boostTerm;
    private Pageable pageable = DEFAULT_PAGE;

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public List<String> getSearchIndices() {
        return searchIndices;
    }

    public void addSearchIndices(String... searchIndices) {
        Collections.addAll(this.searchIndices, searchIndices);
    }

    public List<String> getSearchTypes() {
        return searchTypes;
    }

    public void addSearchTypes(String... searchTypes) {
        Collections.addAll(this.searchTypes, searchTypes);
    }

    public List<String> getFields() {
        return fields;
    }

    public void addFields(String... fields) {
        Collections.addAll(this.fields, fields);
    }

    @Nullable
    public String getRouting() {
        return routing;
    }

    public void setRouting(@Nullable String routing) {
        this.routing = routing;
    }

    @Nullable
    public Float getPercentTermToMatch() {
        return percentTermToMatch;
    }

    public void setPercentTermToMatch(@Nullable Float percentTermToMatch) {
        this.percentTermToMatch = percentTermToMatch;
    }

    @Nullable
    public Integer getMinTermFreq() {
        return minTermFreq;
    }

    public void setMinTermFreq(@Nullable Integer minTermFreq) {
        this.minTermFreq = minTermFreq;
    }

    @Nullable
    public Integer getMaxQueryTerm() {
        return maxQueryTerm;
    }

    public void setMaxQueryTerm(@Nullable Integer maxQueryTerm) {
        this.maxQueryTerm = maxQueryTerm;
    }

    public List<String> getStopWords() {
        return stopWords;
    }

    public void addStopWords(String... stopWords) {
        Collections.addAll(this.stopWords, stopWords);
    }

    @Nullable
    public Integer getMinDocFreq() {
        return minDocFreq;
    }

    public void setMinDocFreq(@Nullable Integer minDocFreq) {
        this.minDocFreq = minDocFreq;
    }

    @Nullable
    public Integer getMaxDocFreq() {
        return maxDocFreq;
    }

    public void setMaxDocFreq(@Nullable Integer maxDocFreq) {
        this.maxDocFreq = maxDocFreq;
    }

    @Nullable
    public Integer getMinWordLen() {
        return minWordLen;
    }

    public void setMinWordLen(@Nullable Integer minWordLen) {
        this.minWordLen = minWordLen;
    }

    @Nullable
    public Integer getMaxWordLen() {
        return maxWordLen;
    }

    public void setMaxWordLen(@Nullable Integer maxWordLen) {
        this.maxWordLen = maxWordLen;
    }

    @Nullable
    public Float getBoostTerm() {
        return boostTerm;
    }

    public void setBoostTerm(@Nullable Float boostTerm) {
        this.boostTerm = boostTerm;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public void setPageable(Pageable pageable) {
        this.pageable = pageable;
    }
}
