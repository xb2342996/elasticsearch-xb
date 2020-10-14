package com.xxbb.springframework.data.elasticsearch.core;

import com.sun.org.apache.bcel.internal.generic.RET;
import org.springframework.lang.Nullable;

public class IndexObjectInformation {
    private final String id;
    @Nullable private final Long seqNo;
    @Nullable private final Long primaryTerm;
    @Nullable private final Long version;

    public IndexObjectInformation(String id, @Nullable Long seqNo, @Nullable Long primaryTerm, @Nullable Long version) {
        this.id = id;
        this.seqNo = seqNo;
        this.primaryTerm = primaryTerm;
        this.version = version;
    }

    public static IndexObjectInformation of(String id, @Nullable Long seqNo, @Nullable Long primaryTerm, @Nullable Long version) {
        return new IndexObjectInformation(id, seqNo, primaryTerm, version);
    }

    public String getId() {
        return id;
    }

    @Nullable
    public Long getSeqNo() {
        return seqNo;
    }

    @Nullable
    public Long getPrimaryTerm() {
        return primaryTerm;
    }

    @Nullable
    public Long getVersion() {
        return version;
    }
}
