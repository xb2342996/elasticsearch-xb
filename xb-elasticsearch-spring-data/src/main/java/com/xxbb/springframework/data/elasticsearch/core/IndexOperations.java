package com.xxbb.springframework.data.elasticsearch.core;

import com.xxbb.springframework.data.elasticsearch.core.document.Document;
import com.xxbb.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import java.util.Map;

public interface IndexOperations {
    boolean create();

    boolean create(Document settings);

    boolean delete();

    boolean exists();

    void refresh();

    Document createMapping();

    Document createMapping(Class<?> clazz);

    default boolean putMapping() {
        return putMapping(createMapping());
    }

    boolean putMapping(Document mapping);

    default boolean putMapping(Class<?> clazz) {
        return putMapping(createMapping(clazz));
    }

    Document createSettings();

    Document createSettings(Class<?> clazz);

    Map<String, Object> getMapping();

    Map<String, Object> getSettings();

    Map<String, Object> getSettings(boolean indcludeDefaults);

    IndexCoordinates getIndexCoordinates();
}
