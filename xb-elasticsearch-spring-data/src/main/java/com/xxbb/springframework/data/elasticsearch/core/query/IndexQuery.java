package com.xxbb.springframework.data.elasticsearch.core.query;

import org.springframework.lang.Nullable;

public class IndexQuery {
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
    @Nullable
    private String routing;

    @Nullable
    public String getRouting() {
        return routing;
    }

    public void setRouting(@Nullable String routing) {
        this.routing = routing;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Nullable
    public Object getObject() {
        return object;
    }

    public void setObject(@Nullable Object object) {
        this.object = object;
    }

    @Nullable
    public Long getVersion() {
        return version;
    }

    public void setVersion(@Nullable Long version) {
        this.version = version;
    }

    @Nullable
    public String getSource() {
        return source;
    }

    public void setSource(@Nullable String source) {
        this.source = source;
    }

    @Nullable
    public String getParentId() {
        return parentId;
    }

    public void setParentId(@Nullable String parentId) {
        this.parentId = parentId;
    }

    @Nullable
    public Long getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(@Nullable Long seqNo) {
        this.seqNo = seqNo;
    }

    @Nullable
    public Long getPrimaryTerm() {
        return primaryTerm;
    }

    public void setPrimaryTerm(@Nullable Long primaryTerm) {
        this.primaryTerm = primaryTerm;
    }
}
