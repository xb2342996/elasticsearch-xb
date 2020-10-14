package com.xxbb.springframework.data.elasticsearch.core.query;

import com.xxbb.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class SimpleField implements Field{

    private String name;
    @Nullable private FieldType fieldType;

    public SimpleField(String name) {
        Assert.notNull(name, "name must not be null");
        this.name = name;
    }

    @Nullable
    @Override
    public FieldType getFieldType() {
        return fieldType;
    }

    @Override
    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public void setName(String name) {
        Assert.notNull(name, "name must not be null");
        this.name = name;
    }

    @Override
    public String getName() {
        Assert.notNull(name, "name must not be null");
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
