package com.xxbb.springframework.data.elasticsearch.core.mapping;

import org.springframework.data.mapping.model.SimpleTypeHolder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ElasticsearchSimpleTypes {
    static final Set<Class<?>> AUTOGENERATED_ID_TYPES;

    static {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(String.class);
        AUTOGENERATED_ID_TYPES = Collections.unmodifiableSet(classes);

        Set<Class<?>> simpleTypes = new HashSet<>();
        ELASTICSEARCH_SIMPLE_TYPES = Collections.unmodifiableSet(simpleTypes);
    }

    private static final Set<Class<?>> ELASTICSEARCH_SIMPLE_TYPES;
    public static final SimpleTypeHolder HOLDER = new SimpleTypeHolder(ELASTICSEARCH_SIMPLE_TYPES, true);
    private ElasticsearchSimpleTypes() {}
}
