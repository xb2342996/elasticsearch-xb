package com.xxbb.springframework.data.elasticsearch.core.document;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.crypto.spec.PSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class DocumentAdapters {

    @Nullable
    public static Document from(GetResponse source) {
        Assert.notNull(source, "GetResponse must not be null");

        if (!source.isExists()) {
            return null;
        }

        if (source.isSourceEmpty()) {
            return fromDocumentFields(source, source.getIndex(), source.getId(), source.getVersion(), source.getSeqNo(), source.getPrimaryTerm());
        }
        Document document = Document.from(source.getSource());
        document.setIndex(source.getIndex());
        document.setId(source.getId());
        document.setVersion(source.getVersion());
        document.setSeqNo(source.getSeqNo());
        document.setPrimaryTerm(source.getPrimaryTerm());
        return document;
    }

    @Nullable
    public static Document from(GetResult source) {
        Assert.notNull(source, "GetResponse must not be null");

        if (!source.isExists()) {
            return null;
        }

        if (source.isSourceEmpty()) {
            return fromDocumentFields(source, source.getIndex(), source.getId(), source.getVersion(), source.getSeqNo(), source.getPrimaryTerm());
        }
        Document document = Document.from(source.getSource());
        document.setIndex(source.getIndex());
        document.setId(source.getId());
        document.setVersion(source.getVersion());
        document.setSeqNo(source.getSeqNo());
        document.setPrimaryTerm(source.getPrimaryTerm());
        return document;
    }

    public static List<Document> from(MultiGetResponse sources) {
        Assert.notNull(sources, "MultiGetResponse must not be null");
        return Arrays.stream(sources.getResponses()).map(itemResponse -> itemResponse.isFailed() ? null : DocumentAdapters.from(itemResponse.getResponse())).collect(Collectors.toList());
    }

    public static SearchDocument from(SearchHit source) {
        Assert.notNull(source, "SearchHit mut not be null");

        Map<String, List<String>> hightlightFields = new HashMap<>(source.getHighlightFields().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey
                , entry -> Arrays.stream(entry.getValue().getFragments()).map(Text::string).collect(Collectors.toList()))));

        Map<String, SearchDocumentResponse> innerHits = new LinkedHashMap<>();
        Map<String, SearchHits> sourceInnerHits = source.getInnerHits();

        if (sourceInnerHits != null) {
            sourceInnerHits.forEach((name, searchHits) -> {
                innerHits.put(name, SearchDocumentResponse.from(searchHits, null, null));
            });
        }

        NestedMetaData nestedMetaData = null;
        if (source.getNestedIdentity() != null) {
            nestedMetaData = from(source.getNestedIdentity());
        }

        BytesReference sourceRef = source.getSourceRef();

        if (sourceRef == null || sourceRef.length() == 0) {
            return new SearchDocumentAdapter(source.getScore(), source.getSortValues(), fromDocumentFields(source, source.getIndex(), source.getId(), source.getVersion(), source.getSeqNo(), source.getPrimaryTerm()), source.getFields(), hightlightFields, innerHits, nestedMetaData);
        }

        Document document = Document.from(source.getSourceAsMap());
        document.setIndex(source.getIndex());
        document.setId(source.getId());
        if (source.getVersion() >= 0) {
            document.setVersion(source.getVersion());
        }
        document.setSeqNo(source.getSeqNo());
        document.setPrimaryTerm(source.getPrimaryTerm());

        return new SearchDocumentAdapter(source.getScore(), source.getSortValues(), document, source.getFields(), hightlightFields, innerHits, nestedMetaData);
    }

    public static NestedMetaData from(SearchHit.NestedIdentity nestedIdentity) {
        NestedMetaData child = null;
        if (nestedIdentity.getChild() != null) {
            child = from(nestedIdentity.getChild());
        }
        return NestedMetaData.of(nestedIdentity.getField().toString(), nestedIdentity.getOffset(), child);
    }

    public static Document fromDocumentFields(Iterable<DocumentField> documentFields, String index, String id, long version, long seqNo, long primaryTerm) {
        if (documentFields instanceof Collection) {
            return new DocumentFieldAdapter((Collection<DocumentField>) documentFields, index, id, version, seqNo, primaryTerm);
        }
        List<DocumentField> fields = new ArrayList<>();
        for (DocumentField field : documentFields) {
            fields.add(field);
        }
        return new DocumentFieldAdapter(fields, index, id, version, seqNo, primaryTerm);
    }

    static class DocumentFieldAdapter implements Document {
        private final Collection<DocumentField> documentFields;
        private final String index;
        private final String id;
        private final long version;
        private final long seqNo;
        private final long primaryTerm;

        public DocumentFieldAdapter(Collection<DocumentField> documentFields, String index, String id, long version, long seqNo, long primaryTerm) {
            this.documentFields = documentFields;
            this.index = index;
            this.id = id;
            this.version = version;
            this.seqNo = seqNo;
            this.primaryTerm = primaryTerm;
        }

        @Override
        public int size() {
            return documentFields.size();
        }

        @Override
        public boolean isEmpty() {
            return documentFields.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            for (DocumentField field : documentFields) {
                if (field.getName().equals(key)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            for (DocumentField field : documentFields) {
                Object fieldValue = getValue(field  );
                if (fieldValue != null && fieldValue.equals(value) || value == fieldValue) {
                    return true;
                }
            }
            return false;
        }

        @Nullable
        @Override
        public Object get(Object key) {
            return documentFields.stream()
                    .filter(documentField -> documentField.getName().equals(key))
                    .map(DocumentField::getValue).findFirst()
                    .orElse(null);
        }

        @Override
        public Object put(String key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends String, ?> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> keySet() {
            return documentFields.stream().map(DocumentField::getName).collect(Collectors.toCollection(LinkedHashSet::new));
        }

        @Override
        public Collection<Object> values() {
            return documentFields.stream().map(DocumentFieldAdapter::getValue).collect(Collectors.toList());
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return documentFields.stream().collect(Collectors.toMap(DocumentField::getName, DocumentFieldAdapter::getValue)).entrySet();
        }

        @Override
        public void forEach(BiConsumer<? super String, ? super Object> action) {
            Objects.requireNonNull(action);
            documentFields.forEach(field -> action.accept(field.getName(), getValue(field)));
        }

        @Override
        public String getIndex() {
            return index;
        }

        @Override
        public boolean hasId() {
            return StringUtils.hasLength(id);
        }

        @Override
        public String getId() {
            return this.id;
        }


        @Override
        public boolean hasVersion() {
            return this.version >= 0;
        }

        @Override
        public long getVersion() {
            if (!hasVersion()) {
                throw new IllegalStateException("No version associated with this document");
            }
            return this.version;
        }

        @Override
        public boolean hasSeqNo() {
            return true;
        }

        @Override
        public long getSeqNo() {
            if (!hasSeqNo()) {
                throw new IllegalStateException("No seq_no associated with this document");
            }
            return this.seqNo;
        }

        @Override
        public boolean hasPrimaryTerm() {
            return true;
        }

        @Override
        public long getPrimaryTerm() {
            if (!hasPrimaryTerm()) {
                throw new IllegalStateException("No primary_term associated with this document");
            }
            return this.primaryTerm;
        }

        @Override
        public String toJson() {

            JsonFactory nodeFactory = new JsonFactory();
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                JsonGenerator generator = nodeFactory.createGenerator(stream, JsonEncoding.UTF8);
                generator.writeStartObject();
                for (DocumentField value : documentFields) {
                    if (value.getValues().size() > 1) {
                        generator.writeArrayFieldStart(value.getName());
                        for (Object val : value.getValues()) {
                            generator.writeObject(val);
                        }
                        generator.writeEndArray();
                    } else {
                        generator.writeObjectField(value.getName(), value.getValue());
                    }
                }
                generator.writeEndObject();
                generator.flush();
                return new String(stream.toByteArray(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new ElasticsearchException("Cannot render JSON", e);
            }
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + '@' + this.id + '#' + this.version + ' ' + toJson();
        }

        @Nullable
        private static Object getValue(DocumentField documentField) {
            if (documentField.getValues().isEmpty()) {
                return null;
            }
            if (documentField.getValues().size() == 1) {
                return documentField.getValue();
            }
            return documentField.getValues();
        }
    }

    static class SearchDocumentAdapter implements SearchDocument {
        private final float score;
        private final Object[] sortValues;
        private final Map<String, List<Object>> fields = new HashMap<>();
        private final Document delegate;
        private final Map<String, List<String>> highlightFields = new HashMap<>();
        private final Map<String, SearchDocumentResponse> innerHits = new HashMap<>();



        @Nullable private final NestedMetaData nestedMetaData;

        public SearchDocumentAdapter(float score, Object[] sortValues, Document delegate, Map<String, DocumentField> fields,
                                     Map<String, List<String>> highlightFields, Map<String, SearchDocumentResponse> innerHits,
                                     @Nullable NestedMetaData nestedMetaData) {
            this.score = score;
            this.sortValues = sortValues;
            this.delegate = delegate;
            fields.forEach((name, documentField) -> this.fields.put(name, documentField.getValues()));
            this.highlightFields.putAll(highlightFields);
            this.innerHits.putAll(innerHits);
            this.nestedMetaData = nestedMetaData;
        }

        @Override
        public SearchDocument append(String key, Object value) {
            delegate.append(key, value);
            return this;
        }

        @Override
        public String getIndex() {
            return delegate.getIndex();
        }

        @Override
        public boolean hasId() {
            return delegate.hasId();
        }

        @Override
        public String getId() {
            return delegate.getId();
        }

        @Override
        public void setId(String id) {
            delegate.setId(id);
        }

        @Override
        public boolean hasVersion() {
            return delegate.hasVersion();
        }

        @Override
        public long getVersion() {
            return delegate.getVersion();
        }

        @Override
        public void setVersion(long version) {
            delegate.setVersion(version);
        }

        @Override
        public boolean hasSeqNo() {
            return delegate.hasSeqNo();
        }

        @Override
        public long getSeqNo() {
            return delegate.getSeqNo();
        }

        @Override
        public void setSeqNo(long seqNo) {
            delegate.setSeqNo(seqNo);
        }

        @Override
        public boolean hasPrimaryTerm() {
            return delegate.hasPrimaryTerm();
        }

        @Override
        public long getPrimaryTerm() {
            return delegate.getPrimaryTerm();
        }

        @Override
        public void setPrimaryTerm(long primaryTerm) {
            delegate.setPrimaryTerm(primaryTerm);
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            return delegate.get(key, type);
        }

        @Override
        public String toJson() {
            return delegate.toJson();
        }

        @Override
        public float getScore() {
            return score;
        }

        @Override
        public Map<String, List<Object>> getFields() {
            return fields;
        }

        @Override
        public Object[] getSortValues() {
            return sortValues;
        }

        @Override
        public Map<String, List<String>> getHighlightFields() {
            return highlightFields;
        }

        @Override
        public Map<String, SearchDocumentResponse> getInnerHits() {
            return innerHits;
        }

        @Nullable
        public NestedMetaData getNestedMetaData() {
            return nestedMetaData;
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return delegate.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return delegate.containsValue(value);
        }

        @Override
        public Object get(Object key) {
            return delegate.get(key);
        }

        @Override
        public Object put(String key, Object value) {
            return delegate.put(key, value);
        }

        @Override
        public Object remove(Object key) {
            return delegate.remove(key);
        }

        @Override
        public void putAll(Map<? extends String, ?> m) {
            delegate.putAll(m);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public Set<String> keySet() {
            return delegate.keySet();
        }

        @Override
        public Collection<Object> values() {
            return delegate.values();
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return delegate.entrySet();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SearchDocumentAdapter)) return false;
            SearchDocumentAdapter that = (SearchDocumentAdapter) o;
            return Float.compare(that.score, score) == 0 &&
                    delegate.equals(that.delegate);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public void forEach(BiConsumer<? super String, ? super Object> action) {
            delegate.forEach(action);
        }

        @Override
        public boolean remove(Object key, Object value) {
            return delegate.remove(key, value);
        }

        @Override
        public String toString() {
            String id = hasId() ? getId() : "?";
            String version = hasVersion() ? Long.toString(getVersion()) : "?";
            return getClass().getSimpleName() + '@' + id + '#' + version + ' ' + toJson();
        }
    }
}
