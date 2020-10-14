package com.xxbb.springframework.data.elasticsearch.core.query;

import com.xxbb.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.lang.Nullable;

public interface Field {
    @Nullable
    FieldType getFieldType();
    void setFieldType(FieldType fieldType);
    void setName(String name);
    String getName();
}
