package com.xxbb.springframework.data.elasticsearch.utils;

import com.xxbb.springframework.data.elasticsearch.core.IndexOperations;

public class IndexInitializer {
    private IndexInitializer() {}

    public static void init(IndexOperations indexOperations) {
        indexOperations.delete();
        indexOperations.create();
        indexOperations.putMapping(indexOperations.createMapping());
        indexOperations.refresh();
    }
}
