package com.xxbb.springframework.data.elasticsearch.core.mapping;

import org.springframework.util.Assert;

import java.util.Arrays;

public class IndexCoordinates {
    public static final String TYPE = "_doc";

    private final String[] indexNames;

    public static IndexCoordinates of(String... indexNames) {
        Assert.notNull(indexNames, "indexnames must not be null");
        return new IndexCoordinates(indexNames);
    }

    private IndexCoordinates(String[] indexNames) {
        Assert.notNull(indexNames, "indexName must not be null");
        this.indexNames = indexNames;
    }

    public String getIndexName() {
        return indexNames[0];
    }

    public String[] getIndexNames() {
        return indexNames;
    }

    @Override
    public String toString() {
        return "IndexCoordinates{" +
                "indexNames=" + Arrays.toString(indexNames) +
                '}';
    }
}
