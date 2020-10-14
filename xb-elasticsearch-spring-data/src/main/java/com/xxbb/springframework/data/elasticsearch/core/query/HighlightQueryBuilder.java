package com.xxbb.springframework.data.elasticsearch.core.query;

import com.xxbb.springframework.data.elasticsearch.annotations.Highlight;
import com.xxbb.springframework.data.elasticsearch.annotations.HighlightField;
import com.xxbb.springframework.data.elasticsearch.annotations.HighlightParameters;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentEntity;
import com.xxbb.springframework.data.elasticsearch.core.mapping.ElasticsearchPersistentProperty;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.AbstractHighlighterBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class HighlightQueryBuilder {
    private final MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext;

    public HighlightQueryBuilder(MappingContext<? extends ElasticsearchPersistentEntity<?>, ElasticsearchPersistentProperty> mappingContext) {
        this.mappingContext = mappingContext;
    }

    public HighlightQuery getHighlightQuery(Highlight highlight, @Nullable Class<?> type) {
        Assert.notNull(highlight, "highlihgt must not be null");

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        addParameters(highlight.parameters(), highlightBuilder, type);
        for (HighlightField highlighField : highlight.fields()) {
            String mappedName = mapFieldName(highlighField.name(), type);
            HighlightBuilder.Field field = new HighlightBuilder.Field(mappedName);
            addParameters(highlighField.parameters(), field, type);
            highlightBuilder.field(field);
        }

        return new HighlightQuery(highlightBuilder);
    }

    private void addParameters(HighlightParameters parameters, AbstractHighlighterBuilder<?> builder, Class<?> type) {
        if (StringUtils.hasLength(parameters.boundaryChars())) {
            builder.boundaryChars(parameters.boundaryChars().toCharArray());
        }

        if (parameters.boundaryMaxScan() > -1) {
            builder.boundaryMaxScan(parameters.boundaryMaxScan());
        }

        if (StringUtils.hasLength(parameters.boundaryScanner())) {
            builder.boundaryScannerType(parameters.boundaryScanner());
        }

        if (StringUtils.hasLength(parameters.boundaryScannerLocale())) {
            builder.boundaryScannerLocale(parameters.boundaryScannerLocale());
        }

        if (parameters.forceSource()) {
            builder.forceSource(parameters.forceSource());
        }

        if (StringUtils.hasLength(parameters.fragmenter())) {
            builder.fragmenter(parameters.fragmenter());
        }

        if (parameters.fragmentSize() > -1) {
            builder.fragmentSize(parameters.fragmentSize());
        }

        if (parameters.noMatchSize() > -1) {
            builder.noMatchSize(parameters.noMatchSize());
        }

        if (parameters.numberOfFragment() > -1) {
            builder.numOfFragments(parameters.numberOfFragment());
        }

        if (StringUtils.hasLength(parameters.order())) {
            builder.order(parameters.order());
        }

        if (parameters.pharseLimit() > -1) {
            builder.phraseLimit(parameters.pharseLimit());
        }

        if (parameters.preTags().length > 0) {
            builder.preTags(parameters.preTags());
        }

        if (parameters.postTags().length > 0) {
            builder.postTags(parameters.postTags());
        }

        if (!parameters.requiredFieldMatch()) {
            builder.requireFieldMatch(parameters.requiredFieldMatch());
        }

        if (StringUtils.hasLength(parameters.type())) {
            builder.highlighterType(parameters.type());
        }

        if (builder instanceof HighlightBuilder) {
            HighlightBuilder highlightBuilder = (HighlightBuilder) builder;
            if (StringUtils.hasLength(parameters.encoder())) {
                highlightBuilder.encoder(parameters.encoder());
            }
            if (StringUtils.hasLength(parameters.tagsSchema())) {
                highlightBuilder.tagsSchema(parameters.tagsSchema());
            }
        }

        if (builder instanceof HighlightBuilder.Field) {
            HighlightBuilder.Field field = (HighlightBuilder.Field) builder;
            if (parameters.fragmentOffset() > -1) {
                field.fragmentOffset(parameters.fragmentOffset());
            }

            if (parameters.matchFields().length > 0) {
                field.matchedFields(Arrays.stream(parameters.matchFields()).map(fieldName -> mapFieldName(fieldName, type)).collect(Collectors.toList()).toArray(new String[] {}));
            }
        }
    }

    private String mapFieldName(String fieldName, @Nullable Class<?> type) {
        if (type != null) {
            ElasticsearchPersistentEntity<?> persistentEntity = mappingContext.getPersistentEntity(type);

            if (persistentEntity != null) {
                ElasticsearchPersistentProperty persistentProperty = persistentEntity.getPersistentProperty(fieldName);

                if (persistentProperty != null) {
                    return persistentProperty.getFieldName();
                }
            }
        }
        return fieldName;
    }
}
