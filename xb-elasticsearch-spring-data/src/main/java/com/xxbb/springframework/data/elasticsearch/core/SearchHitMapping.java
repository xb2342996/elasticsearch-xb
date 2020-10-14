package com.xxbb.springframework.data.elasticsearch.core;

import com.xxbb.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import com.xxbb.springframework.data.elasticsearch.core.document.SearchDocument;
import com.xxbb.springframework.data.elasticsearch.core.document.SearchDocumentResponse;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchHitMapping<T> {
    private final Class<T> type;
    private final MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;

    public SearchHitMapping(Class<T> type, ElasticsearchConverter converter) {
        Assert.notNull(type, "Type must be not null");
        Assert.notNull(converter, "converter must be not null");
        this.type = type;
        this.mappingContext = converter.getMappingContext();
    }

    static <T> SearchHitMapping<T> mappingFor(Class<T> entityClass, ElasticsearchConverter converter) {
        return new SearchHitMapping<>(entityClass, converter);
    }

    SearchHit<T> mapHit(SearchDocument searchDocument, T content) {
        Assert.notNull(searchDocument, "searchDocument must not be null");
        Assert.notNull(content, "content must not be null");

        String id = searchDocument.hasId() ? searchDocument.getId() : null;
        float score = searchDocument.getScore();
        Object[] sortValues = searchDocument.getSortValues();
        Map<String, List<String>> highlightFields = getHighlightsAndRemapFieldNames(searchDocument);

        return new SearchHit<>(id, score, sortValues, content, highlightFields);
    }

    SearchHits<T> mapHits(SearchDocumentResponse searchDocumentResponse, List<T> contents) {
        return mapHitsFromResponse(searchDocumentResponse, contents);
    }

    SearchScrollHits<T> mapScrollHits(SearchDocumentResponse searchDocumentResponse, List<T> contents) {
        return mapHitsFromResponse(searchDocumentResponse, contents);
    }

    private SearchHitsImpl<T> mapHitsFromResponse(SearchDocumentResponse searchDocumentResponse, List<T> contents) {
        Assert.notNull(searchDocumentResponse, "SearchDocumentResponse must not be null");
        Assert.notNull(contents, "content must not be null");
        Assert.isTrue(searchDocumentResponse.getSearchDocuments().size() == contents.size(), "Count of documents must match the count of entities");

        long totalHits = searchDocumentResponse.getTotalHits();
        float maxScore = searchDocumentResponse.getMaxScore();
        String scrollId = searchDocumentResponse.getScrollId();

        List<SearchHit<T>> searchHits = new ArrayList<>();
        List<SearchDocument> searchDocuments = searchDocumentResponse.getSearchDocuments();
        for (int i = 0; i < searchDocuments.size(); i++) {
            SearchDocument document = searchDocuments.get(i);
            T content = contents.get(i);
            SearchHit<T> hit = mapHit(document, content);
            searchHits.add(hit);
        }
        Aggregations aggregations = searchDocumentResponse.getAggregations();
        TotalHitsRelation totalHitsRelation = TotalHitsRelation.valueOf(searchDocumentResponse.getTotalHitsRelation());

        return new SearchHitsImpl<>(totalHits, totalHitsRelation, maxScore, scrollId, searchHits, aggregations);
    }


    @Nullable
    private Map<String, List<String>> getHighlightsAndRemapFieldNames(SearchDocument searchDocument) {
        Map<String, List<String>> highlightFields = searchDocument.getHighlightFields();

        if (highlightFields == null) {
            return null;
        }
        ElasticsearchPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(type);
        if (persistentEntity == null) {
            return highlightFields;
        }
        return highlightFields.entrySet().stream().collect(Collectors.toMap(entry -> {
            ElasticsearchPersistentProperty property = persistentEntity.getPersistentPropertyWithFieldName(entry.getKey());
            return property != null ? property.getName() : entry.getKey();
        }, Map.Entry::getValue));
    }
}
