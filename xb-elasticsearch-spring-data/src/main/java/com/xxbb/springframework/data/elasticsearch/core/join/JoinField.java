package com.xxbb.springframework.data.elasticsearch.core.join;

import org.springframework.data.annotation.Id;
import org.springframework.lang.Nullable;

public class JoinField<ID> {
    private final String name;

    @Nullable private ID parent;

    public JoinField() {
        this("default", null);
    }

    public JoinField(String name) {
        this(name, null);
    }

    public JoinField(String name, @Nullable ID parent) {
        this.name = name;
        this.parent = parent;
    }

    @Nullable
    public ID getParent() {
        return parent;
    }

    public void setParent(@Nullable ID parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }
}
