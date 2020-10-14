package com.xxbb.springframework.data.elasticsearch.core.query;

public class IndexBoost {
    private String indexName;
    private float boost;;

    public IndexBoost(String indexName, float boost) {
        this.indexName = indexName;
        this.boost = boost;
    }

    public String getIndexName() {
        return indexName;
    }

    public float getBoost() {
        return boost;
    }
}
