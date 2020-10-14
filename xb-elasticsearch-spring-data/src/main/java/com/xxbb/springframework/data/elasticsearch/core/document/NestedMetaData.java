package com.xxbb.springframework.data.elasticsearch.core.document;

import org.springframework.lang.Nullable;

public class NestedMetaData {
    private final String field;
    private final int offset;
    @Nullable private final NestedMetaData child;

    public static NestedMetaData of(String field, int offset, @Nullable NestedMetaData child) {
        return new NestedMetaData(field, offset, child);
    }

    public NestedMetaData(String field, int offset, @Nullable NestedMetaData child) {
        this.field = field;
        this.offset = offset;
        this.child = child;
    }

    public String getField() {
        return field;
    }

    public int getOffset() {
        return offset;
    }

    @Nullable
    public NestedMetaData getChild() {
        return child;
    }
}
