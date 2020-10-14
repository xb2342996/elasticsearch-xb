package com.xxbb.springframework.data.elasticsearch.core.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.ElasticsearchException;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

class MapDocument implements Document {
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final LinkedHashMap<String, Object> documentAsMap;

    private @Nullable String index;
    private @Nullable String id;
    private @Nullable Long version;
    private @Nullable Long seqNo;
    private @Nullable Long primaryTerm;

    MapDocument() {
        this(new LinkedHashMap<>());
    }

    MapDocument(Map<String, ? extends Object> documentAsMap) {
        this.documentAsMap = new LinkedHashMap<>(documentAsMap);
    }

    @Nullable
    @Override
    public String getIndex() {
        return index;
    }

    @Override
    public void setIndex(String index) {
        this.index = index;
    }

    @Override
    public boolean hasId() {
        return this.id != null;
    }

    @Override
    public String getId() {
        if (!hasId()) {
            throw new IllegalStateException("No Id associated with this Document");
        }
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean hasVersion() {
        return this.version != null;
    }

    @Override
    public long getVersion() {
        if (!hasVersion()) {
            throw new IllegalStateException("No Id associated with this Document");
        }
        return version;
    }

    @Override
    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public boolean hasSeqNo() {
        return this.seqNo != null;
    }

    @Override
    public long getSeqNo() {
        if (!hasSeqNo()) {
            throw new IllegalStateException("No Id associated with this Document");
        }
        return this.seqNo;
    }

    public void setSeqNo(long seqNo) {
        this.seqNo = seqNo;
    }

    @Override
    public boolean hasPrimaryTerm() {
        return this.primaryTerm != null;
    }

    @Override
    public long getPrimaryTerm() {
        if (!hasPrimaryTerm()) {
            throw new IllegalStateException("No Id associated with this Document");
        }
        return this.primaryTerm;
    }

    public void setPrimaryTerm(long primaryTerm) {
        this.primaryTerm = primaryTerm;
    }

    @Override
    public int size() {
        return documentAsMap.size();
    }

    @Override
    public boolean isEmpty() {
        return documentAsMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return documentAsMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return documentAsMap.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return documentAsMap.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return documentAsMap.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return documentAsMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        documentAsMap.putAll(m);
    }

    @Override
    public void clear() {
        documentAsMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return documentAsMap.keySet();
    }

    @Override
    public Collection<Object> values() {
        return documentAsMap.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return documentAsMap.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return documentAsMap.equals(o);
    }

    @Override
    public int hashCode() {
        return documentAsMap.hashCode();
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        documentAsMap.forEach(action);
    }

    @Override
    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new ElasticsearchException("Cannot render document to JSON", e);
        }
    }

    @Override
    public String toString() {
        String id = hasId() ? getId() : "?";
        String version = hasVersion() ? Long.toString(getVersion()) : "?";
        return getClass().getSimpleName() + "@" + id + "#" + version + ' ' + toJson();
    }
}
