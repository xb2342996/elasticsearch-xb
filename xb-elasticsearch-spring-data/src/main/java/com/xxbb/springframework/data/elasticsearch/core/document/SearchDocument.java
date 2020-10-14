package com.xxbb.springframework.data.elasticsearch.core.document;

import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

public interface SearchDocument extends Document {
    float getScore();

    Map<String, List<Object>> getFields();

    @Nullable
    default <V> V getFieldValue(final String name) {
        List<Object> values = getFields().get(name);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return (V) values.get(0);
    }

    @Nullable
    default Object[] getSortValues() {
        return null;
    }

    @Nullable
    default Map<String, List<String>> getHighlightFields() {
        return null;
    }

    @Nullable
    default Map<String, SearchDocumentResponse> getInnerHits() {
        return null;
    }

//    @Nullable
//    default NestedMetaData getNestedMetaData() {
//        return null;
//    }
}
