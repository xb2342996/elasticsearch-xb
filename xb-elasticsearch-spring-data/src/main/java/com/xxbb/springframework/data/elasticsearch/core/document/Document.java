package com.xxbb.springframework.data.elasticsearch.core.document;

import org.elasticsearch.ElasticsearchException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.*;

public interface Document extends Map<String, Object> {
    static Document create() {
        return new MapDocument();
    }

    static Document from(Map<String, ? extends Object> map) {
        Assert.notNull(map, "Map must not be null");

        if (map instanceof LinkedHashMap) {
            return new MapDocument(map);
        }
        return new MapDocument(new LinkedHashMap<>(map));
    }

    static Document parse(String json) {
        Assert.notNull(json, "JSON must not be null");

        try {
            return new MapDocument(MapDocument.OBJECT_MAPPER.readerFor(Map.class).readValue(json));
        } catch (IOException e) {
            throw new ElasticsearchException("Cannot parse JSON", e);
        }
    }

    default Document append(String key, Object value) {
        Assert.notNull(key, "key must not be null");
        put(key, value);
        return this;
    }

    default boolean hasId() {
        return false;
    }

    @Nullable
    default String getIndex() {
        return null;
    }

    default void setIndex(@Nullable String index) {
        throw new UnsupportedOperationException();
    }

    default String getId() {
        throw new UnsupportedOperationException();
    }

    default void setId(String id) {
        throw new UnsupportedOperationException();
    }

    default boolean hasVersion() {
        return false;
    }

    default long getVersion() {
        throw new UnsupportedOperationException();
    }

    default void setVersion(long version) {
        throw new UnsupportedOperationException();
    }

    default boolean hasSeqNo() {
        return false;
    }

    default long getSeqNo() {
        throw new UnsupportedOperationException();
    }

    default void setSeqNo(long seqNo) {
        throw new UnsupportedOperationException();
    }

    default boolean hasPrimaryTerm() {
        return false;
    }

    default long getPrimaryTerm() {
        throw new UnsupportedOperationException();
    }

    default void setPrimaryTerm(long primaryTerm) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    default <T> T get(Object key, Class<T> type) {
        Assert.notNull(key, "key must not be null");
        Assert.notNull(key, "key must not be null");
        return type.cast(get(key));
    }
    @Nullable
    default Boolean getBoolean(String key) {
        return get(key, Boolean.class);
    }

    default boolean getBooleanOrDefault(String key, boolean defaultValue) {
        return getBooleanOrDefault(key, () -> defaultValue);
    }

    default boolean getBooleanOrDefault(String key, BooleanSupplier defaultValue) {
        Boolean value = getBoolean(key);
        return value == null ? defaultValue.getAsBoolean() : value;
    }

    @Nullable
    default Integer getInt(String key) {
        return get(key, Integer.class);
    }

    default Integer getIntOrDefault(String key, int defaultValue) {
        return getIntOrDefault(key, () -> defaultValue);
    }

    default Integer getIntOrDefault(String key, IntSupplier defaultValue) {
        Integer value = getInt(key);
        return value == null ? defaultValue.getAsInt() : value;
    }

    @Nullable
    default Long getLong(String key) {
        return get(key, Long.class);
    }

    default Long getLongOrDefault(String key, long defaultValue) {
        return getLongOrDefault(key, () -> defaultValue);
    }

    default Long getLongOrDefault(String key, LongSupplier defaultValue) {
        Long value = getLong(key);
        return value == null ? defaultValue.getAsLong() : value;
    }

    @Nullable
    default String getString(String key) {
        return get(key, String.class);
    }

    default String getStringOrDefault(String key, String defaultValue) {
        return getStringOrDefault(key, () -> defaultValue);
    }

    default String getStringOrDefault(String key, Supplier<String> defaultValue) {
        String value = getString(key);
        return value == null ? defaultValue.get() : value;
    }

    default <R> R transform(Function<? super Document, ? extends R> transformer) {
        Assert.notNull(transformer, "transfomer must not be null");
        return transformer.apply(this);
    }

    String toJson();
}
