package com.xxbb.springframework.data.elasticsearch.core.mapping;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mapping.PersistentProperty;

public interface ElasticsearchPersistentProperty extends PersistentProperty<ElasticsearchPersistentProperty> {
    String getFieldName();

    boolean isScoreProperty();

    boolean isSeqNoPrimaryTermProperty();

    boolean hasPropertyConverter();

    ElasticsearchPersistentPropertyConverter getPropertyConverter();

    boolean isReadable();

    boolean storeNullValue();

    boolean isGeoShapeProperty();

    boolean isJoinFieldProperty();

    boolean isCompletionProperty();

    enum PropertyToFieldNameConverter implements Converter<ElasticsearchPersistentProperty, String> {
        INSTANCE;

        @Override
        public String convert(ElasticsearchPersistentProperty source) {
            return source.getFieldName();
        }
    }

    enum QueryPropertyToFieldNameConverter implements Converter<ElasticsearchPersistentProperty, String> {
        INSTANCE;

        @Override
        public String convert(ElasticsearchPersistentProperty source) {
            return source.getName();
        }
    }
}
