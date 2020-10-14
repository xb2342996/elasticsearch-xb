package com.xxbb.springframework.data.elasticsearch.annotations;

public enum Similarity {
    Default("default"), BM25("BM25"), classic("classic"), Boolean("boolean");

    private final String toStringName;

    Similarity(String name) {
        this.toStringName = name;
    }

    @Override
    public String toString() {
        return toStringName;
    }
}
