package com.xxbb.springframework.data.elasticsearch.core.query;

import org.springframework.lang.Nullable;

public class IndexQueryBuilder {
    @Nullable
    private String id;
    @Nullable
    private Object object;
    @Nullable
    private Long version;
    @Nullable
    private String source;
    @Nullable
    private String parentId;
    @Nullable
    private Long seqNo;
    @Nullable
    private Long primaryTerm;

    public IndexQueryBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public IndexQueryBuilder withObject(Object object) {
        this.object = object;
        return this;
    }
    public IndexQueryBuilder withVersion(Long version) {
        this.version = version;
        return this;
    }
    public IndexQueryBuilder withSource(String source) {
        this.source = source;
        return this;
    }
    public IndexQueryBuilder withParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }
    public IndexQueryBuilder withSeqNoPrimaryTerm(SeqNoPrimaryTerm seqNoPrimaryTerm) {
        this.seqNo = seqNoPrimaryTerm.getSequenceNumber();
        this.primaryTerm = seqNoPrimaryTerm.getPrimaryTerm();
        return this;
    }

    public IndexQuery build() {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId(id);
        indexQuery.setObject(object);
        indexQuery.setParentId(parentId);
        indexQuery.setSource(source);
        indexQuery.setVersion(version);
        indexQuery.setSeqNo(seqNo);
        indexQuery.setPrimaryTerm(primaryTerm);
        return indexQuery;
    }
}
