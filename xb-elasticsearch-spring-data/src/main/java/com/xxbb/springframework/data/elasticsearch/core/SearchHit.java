package com.xxbb.springframework.data.elasticsearch.core;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

public class SearchHit<T> {
    private final String id;
    private final float score;
    private List<Object> sortValues;
    private final T content;
    private final Map<String, List<String>> highlightFields = new LinkedHashMap<>();

    public SearchHit(@Nullable String id, float score, @Nullable Object[] sortValues, @Nullable T content,
                     @Nullable Map<String, List<String>> highlightFields) {
        this.id = id;
        this.score = score;
        this.sortValues = sortValues != null ? Arrays.asList(sortValues) : new ArrayList<>();
        this.content = content;
        if (highlightFields != null) {
            this.highlightFields.putAll(highlightFields);
        }
    }

    public String getId() {
        return id;
    }

    public float getScore() {
        return score;
    }

    public List<Object> getSortValues() {
        return Collections.unmodifiableList(sortValues);
    }

    public T getContent() {
        return content;
    }

    public Map<String, List<String>> getHighlightFields() {
        return Collections.unmodifiableMap(highlightFields.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> Collections.unmodifiableList(entry.getValue()))));
    }

    public List<String> getHighlightField(String field) {
        Assert.notNull(field, "field must not be null");
        return Collections.unmodifiableList(highlightFields.getOrDefault(field, Collections.emptyList()));
    }

    @Override
    public String toString() {
        return "SearchHit{" +
                "id='" + id + '\'' +
                ", score=" + score +
                ", sortValues=" + sortValues +
                ", content=" + content +
                ", highlightFields=" + highlightFields +
                '}';
    }
}
